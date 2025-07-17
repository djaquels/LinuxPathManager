package com.github.djaquels.utils;

import com.jcraft.jsch.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UserRemotePathCommand implements PathCommand {
    private final String username;
    private final String host;
    private final int port;
    private final String password;
    private List<String> pathValues;

    public UserRemotePathCommand(String username, String host, int port, String password) {
        this.username = username;
        this.host = host;
        this.port = port;
        this.password = password;
    }

    @Override
    public void execute() {
        Session session = null;
        ChannelExec channel = null;
        try {
            session = SSHConnectionUtil.connect(this.username, this.host, this.port, this.password);
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand("echo $PATH");
            channel.setInputStream(null);
            InputStream input = channel.getInputStream();
            channel.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String output = reader.readLine();
            if (output != null && !output.isEmpty()) {
                // Use ':' as the path separator for Linux/Unix systems
                pathValues = Arrays.asList(output.split(":"));
            } else {
                pathValues = Collections.emptyList();
            }
        } catch (Exception e) {
            e.printStackTrace();
            pathValues = Collections.emptyList();
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    @Override
    public List<String> getResult() {
        return pathValues;
    }
}