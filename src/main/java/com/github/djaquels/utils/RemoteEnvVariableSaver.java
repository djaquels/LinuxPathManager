package com.github.djaquels.utils;

import com.jcraft.jsch.*;
import javafx.collections.ObservableList;

import java.io.*;
import java.util.*;

public class RemoteEnvVariableSaver implements SavePathCommand {
    private final String username;
    private final String host;
    private final int port;
    private final String password; // null if using key-based auth

    public RemoteEnvVariableSaver(String username, String host, int port, String password) {
        this.username = username;
        this.host = host;
        this.port = port;
        this.password = password;
    }

    private Session connect() throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, host, port);
        if (password != null) session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect(5000);
        return session;
    }

    private List<String> readRemoteFile(Session session, String remotePath) throws Exception {
        List<String> lines = new ArrayList<>();
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand("cat \"" + remotePath + "\"");
        channel.setInputStream(null);
        InputStream in = channel.getInputStream();
        channel.connect();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while((line = reader.readLine()) != null) lines.add(line);
        }
        channel.disconnect();
        return lines;
    }

    private void writeRemoteFile(Session session, String remotePath, List<String> content) throws Exception {
        // Use echo for small files, or SCP for larger ones. Here, we use echo for simplicity.
        String tempPath = "/tmp/lpm_envs_" + System.currentTimeMillis();
        // Write to a temp file locally
        File temp = File.createTempFile("lpm_envs", null);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(temp))) {
            for (String line : content) writer.write(line + "\n");
        }
        // SCP to remote /tmp
        scpToRemote(session, temp.getAbsolutePath(), tempPath);
        // Move temp file to user's home directory
        execRemoteCommand(session, "mv " + tempPath + " \"" + remotePath + "\"");
        temp.delete();
    }

    private void scpToRemote(Session session, String local, String remote) throws Exception {
        // SCP file to remote path
        FileInputStream fis = null;
        boolean ptimestamp = true;
        String command = "scp " + (ptimestamp ? "-p" : "") + " -t " + remote;
        Channel channel = session.openChannel("exec");
        ((ChannelExec) channel).setCommand(command);
        OutputStream out = channel.getOutputStream();
        InputStream in = channel.getInputStream();
        channel.connect();

        File _lfile = new File(local);

        if (ptimestamp) {
            command = "T" + (_lfile.lastModified() / 1000) + " 0";
            command += (" " + (_lfile.lastModified() / 1000) + " 0\n");
            out.write(command.getBytes());
            out.flush();
            checkAck(in);
        }

        // send "C0644 filesize filename", where filename should not include slashes
        long filesize = _lfile.length();
        command = "C0644 " + filesize + " ";
        if (local.lastIndexOf('/') > 0) {
            command += local.substring(local.lastIndexOf('/') + 1);
        } else {
            command += local;
        }
        command += "\n";
        out.write(command.getBytes());
        out.flush();
        checkAck(in);

        // send file content
        fis = new FileInputStream(local);
        byte[] buf = new byte[1024];
        while (true) {
            int len = fis.read(buf, 0, buf.length);
            if (len <= 0) break;
            out.write(buf, 0, len);
        }
        fis.close();
        // send '\0'
        out.write(0);
        out.flush();
        checkAck(in);
        out.close();
        channel.disconnect();
    }

    private int checkAck(InputStream in) throws IOException {
        int b = in.read();
        if (b == 0) return b;
        if (b == -1) return b;
        if (b == 1 || b == 2) {
            StringBuilder sb = new StringBuilder();
            int c;
            do { c = in.read(); sb.append((char) c); }
            while(c != '\n');
            throw new IOException(sb.toString());
        }
        return b;
    }

    private void execRemoteCommand(Session session, String command) throws Exception {
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        channel.setInputStream(null);
        channel.connect();
        channel.disconnect();
    }

    @Override
    public void execute(ObservableList<String> valuesList) {
        try {
            updateEnvVars(valuesList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(String newLine) throws IOException {
        // Not implemented; not needed for this use-case
    }

    private void updateEnvVars(ObservableList<String> valuesList) throws Exception {
        Map<String, String> shellFiles = Map.of(
                "bash", ".bashrc",
                "zsh", ".zshrc",
                "fish", ".config/fish/config.fish",
                "ksh", ".kshrc",
                "csh", ".cshrc",
                "tcsh", ".tcshrc"
        );
        Map<String, String[]> shellFilters = Map.of(
                "bash", new String[] {"export ", "="},
                "zsh", new String[] {"export ", "="},
                "fish", new String[] {"set -x ", " "},
                "ksh", new String[] {"export ", "="},
                "csh", new String[] {"setenv ", "="},
                "tcsh", new String[] {"setenv ", "="}
        );
        String homeDir = "/home/" + username;

        Session session = null;
        try {
            session = connect();
            for (Map.Entry<String, String> entry : shellFiles.entrySet()) {
                String shellConfigFile = entry.getValue();
                String remotePath = homeDir + "/" + shellConfigFile;
                List<String> lines;
                try {
                    lines = readRemoteFile(session, remotePath);
                } catch (Exception e) {
                    lines = new ArrayList<>();
                }
                List<String> newLines = new ArrayList<>();
                String startMarker = "#BEGIN LinuxPathManagerENVS";
                String endMarker = "#END LinuxPathManagerENVS";
                boolean inMarker = false;
                for(String line: lines){
                    if(line.trim().equals(startMarker)){
                        inMarker = true;
                        continue;
                    }
                    if(line.trim().equals(endMarker)){
                        inMarker = false;
                        continue;
                    }
                    if(!inMarker){
                        newLines.add(line);
                    }
                }
                // add new ENV entries
                newLines.add(startMarker);
                for(String envVar: valuesList){
                    String[] envString = envVar.split("=");
                    String envKey = envString[0];
                    String envValue = envString.length == 2? envString[1]: "";
                    String separator = shellFilters.get(entry.getKey())[1];
                    String command = shellFilters.get(entry.getKey())[0];
                    newLines.add(command + envKey + separator + envValue);
                }
                newLines.add(endMarker);
                writeRemoteFile(session, remotePath, newLines);
            }
        } finally {
            if (session != null && session.isConnected()) session.disconnect();
        }
    }
}