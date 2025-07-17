package com.github.djaquels.utils;

import com.jcraft.jsch.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class RemoteEnvVariablesCommand implements PathCommand {

    private final String username;
    private final String host;
    private final int port;
    private final String password; // Can be null for key authentication
    private List<String> result = new ArrayList<>();

    public RemoteEnvVariablesCommand(String username, String host, int port, String password) {
        this.username = username;
        this.host = host;
        this.port = port;
        this.password = password;
    }

    @Override
    public void execute() {
        result.clear();
        Session session = null;
        ChannelExec channel = null;
        try {
            session = SSHConnectionUtil.connect(this.username, this.host, this.port, this.password);
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand("printenv");
            channel.setInputStream(null);
            InputStream input = channel.getInputStream();
            channel.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line;
            while ((line = reader.readLine()) != null) {
                result.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Optionally, handle error state
        } finally {
            if (channel != null && channel.isConnected()) channel.disconnect();
            if (session != null && session.isConnected()) session.disconnect();
        }
    }

    @Override
    public List<String> getResult() {
        if (result.isEmpty()) {
            execute();
        }
        return result;
    }
}