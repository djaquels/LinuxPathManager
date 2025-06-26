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
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, host, port);
        if (password != null) session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect(5000);
        return session;
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
            scpToRemote(session, temp.getAbsolutePath(), remoteTmp);
            // Move to /etc/profile.d/ with sudo
            execRemoteCommand(session, "echo '" + password + "' | sudo -S mv " + remoteTmp + " " + REMOTE_SCRIPT_PATH);
            execRemoteCommand(session, "echo '" + password + "' | sudo -S chmod +x " + REMOTE_SCRIPT_PATH);
            temp.delete();
        } catch (Exception e) {
            throw new IOException("Remote update failed", e);
        } finally {
            if (session != null && session.isConnected()) session.disconnect();
        }
    }

    private void scpToRemote(Session session, String local, String remote) throws Exception {
        // (see SCP logic in previous class, or use JSch's example)
        // Omitted for brevity—can share full SCP util if needed.
    }

    private void execRemoteCommand(Session session, String command) throws Exception {
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        channel.setInputStream(null);
        channel.connect();
        channel.disconnect();
    }
}