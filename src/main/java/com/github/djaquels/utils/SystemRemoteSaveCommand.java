package com.github.djaquels.utils;

import com.jcraft.jsch.*;
import javafx.collections.ObservableList;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SystemRemoteSaveCommand implements SavePathCommand {
    private final String username;
    private final String host;
    private final int port;
    private final String password; // Should be a sudoer or root for /etc/profile.d/
    private static final String REMOTE_SCRIPT_PATH = "/etc/profile.d/linuxpathmanager.sh";

    public SystemRemoteSaveCommand(String username, String host, int port, String password) {
        this.username = username;
        this.host = host;
        this.port = port;
        this.password = password;
    }

    private Session connect() throws JSchException {
        Session session = null;
        try{
        session = SSHConnectionUtil.connect(this.username, this.host, this.port, this.password);
        return session;
        }catch(Exception e){
            throw new JSchException("SSH Authentication failed. Tried public-key and password. ");
        }
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
        Session session = null;
        try {
            session = connect();
            List<String> lines = new ArrayList<>();
            lines.add("#!/bin/bash");
            lines.add("export PATH=\"" + newPath + "\"");
            // Write to temp file locally
            File temp = File.createTempFile("lpm_remotescript", null);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(temp))) {
                for (String line : lines) writer.write(line + "\n");
            }
            // SCP to remote /tmp
            String remoteTmp = "/tmp/linuxpathmanager.sh";
            scpToRemote(session, temp, remoteTmp);
            // Move to /etc/profile.d/ with sudo
            execRemoteCommand(session, "echo '" + password + "' | sudo -S mv " + remoteTmp + " " + REMOTE_SCRIPT_PATH);
            execRemoteCommand(session, "echo '" + password + "' | sudo -S chmod +x " + REMOTE_SCRIPT_PATH);
            temp.delete();
        } catch (Exception e) {
            System.out.println(e);
            throw new IOException("Remote update failed", e);
        } finally {
            if (session != null && session.isConnected()) session.disconnect();
        }
    }

    private void scpToRemote(Session session, File temp, String remotePath) throws Exception {
        // (see SCP logic in previous class, or use JSch's example)
        // Omitted for brevity—can share full SCP util if needed.
        try(FileInputStream fis = new FileInputStream(temp)){
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
        }
        finally{
             System.out.println("Temp filed transferred");
        }
    }

    private void execRemoteCommand(Session session, String command) throws Exception {
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        channel.setInputStream(null);
        channel.connect();
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
}