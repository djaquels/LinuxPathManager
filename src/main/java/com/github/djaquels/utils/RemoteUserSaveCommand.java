package com.github.djaquels.utils;

import com.jcraft.jsch.*;
import javafx.collections.ObservableList;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class RemoteUserSaveCommand implements SavePathCommand {
    private final String username;
    private final String host;
    private final int port;
    private final String password; // Or use key-based auth

    public RemoteUserSaveCommand(String username, String host, int port, String password) {
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

    // Utility to write to a remote file (overwrites)
    private void writeRemoteFile(Session session, String remotePath, List<String> content) throws Exception {
        String tempFile = "/tmp/lpm_tempfile_" + System.currentTimeMillis();
        // 1. Create temp file locally
        File temp = File.createTempFile("lpm_tempfile", null);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(temp))) {
            for (String line : content) writer.write(line + "\n");
        }
        // 2. SCP temp file to remote
        try (FileInputStream fis = new FileInputStream(temp)) {
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand("scp -t " + remotePath);
            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();
            channel.connect();

            // Send "C0644 filesize filename", then file content, then 0
            long filesize = temp.length();
            String command = "C0644 " + filesize + " " + new File(remotePath).getName() + "\n";
            out.write(command.getBytes());
            out.flush();
            if (checkAck(in) != 0) throw new IOException("SCP error");

            byte[] buf = new byte[1024];
            int len;
            while ((len = fis.read(buf)) > 0) out.write(buf, 0, len);
            out.write(0); out.flush();
            if (checkAck(in) != 0) throw new IOException("SCP error");
            out.close();
            channel.disconnect();
        } finally {
            temp.delete();
        }
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

    @Override
    public void execute(ObservableList<String> valuesList) {
        String newPath = String.join(":", valuesList);
        try {
            update(newPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(String newPath) throws IOException, InterruptedException {
        Map<String, String> shellFiles = Map.of(
                "bash", ".bashrc",
                "zsh", ".zshrc",
                "fish", ".config/fish/config.fish",
                "ksh", ".kshrc",
                "csh", ".cshrc",
                "tcsh", ".tcshrc"
        );
        String homeDir = "/home/" + username;

        Session session = null;
        try {
            session = connect();
            for (Map.Entry<String, String> entry : shellFiles.entrySet()) {
                String shellConfigFile = entry.getValue();
                String remotePath = homeDir + "/" + shellConfigFile;
                // Read file (if exists)
                List<String> lines = readRemoteFile(session, remotePath);
                if(lines.size() == 0){
                    continue;
                }
                List<String> newLines = new ArrayList<>();
                String startMarker = "#BEGIN LinuxPathManager";
                String endMarker = "#END LinuxPathManager";
                boolean inMarker = false;
                String currentPath = "";
                for (String line : lines) {
                    if (line.trim().equals(startMarker)) { inMarker = true; continue; }
                    if (line.trim().equals(endMarker)) { inMarker = false; continue; }
                    if (inMarker) currentPath += line.trim();
                    if (!inMarker) newLines.add(line);
                }
                String newPathEntry = "export PATH=\"" + newPath + "\"";
                newLines.add(startMarker);
                newLines.add(newPathEntry);
                newLines.add(endMarker);
                writeRemoteFile(session, remotePath, newLines);
            }
        } catch (Exception e) {
            throw new IOException("Remote update failed", e);
        } finally {
            if (session != null && session.isConnected()) session.disconnect();
        }
    }

    private List<String> readRemoteFile(Session session, String remotePath) throws Exception {
        List<String> lines = new ArrayList<>();
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand("cat " + remotePath);
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
}