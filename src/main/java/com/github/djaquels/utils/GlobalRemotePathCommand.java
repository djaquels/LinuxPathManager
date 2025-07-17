package com.github.djaquels.utils;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GlobalRemotePathCommand implements PathCommand {
    private final String username;
    private final String host;
    private final int port;
    private final String password;
    private List<String> pathValues;

    public GlobalRemotePathCommand(String username, String host, int port, String password) {
        this.username = username;
        this.host = host;
        this.port = port;
        this.password = password;
    }

    @Override
    public void execute() {
        ChannelExec channel = null;
        Session session = null;
        try {
            session = SSHConnectionUtil.connect(this.username, this.host, this.port, this.password);
            // Try to cat the custom profile file
            String[] pathLines = tryReadRemoteFile(session, "/etc/profile.d/linuxpathmanager.sh");
            if (pathLines != null) {
                // Find line(s) like: export PATH=...
                for (String line : pathLines) {
                    line = line.trim();
                    if (line.startsWith("export PATH=")) {
                        // Remove 'export PATH=' and possible surrounding quotes
                        String pathValue = line.substring("export PATH=".length()).replaceAll("^['\"]|['\"]$", "");
                        pathValues = Arrays.asList(pathValue.split(":"));
                        return;
                    }
                }
                // If file exists but no export PATH= line, fallback (can also return empty or error)
            }

            // Fallback: get the current PATH from the remote shell
            String remotePath = tryRunSingleLineCommand(session, "echo $PATH");
            if (remotePath != null && !remotePath.isEmpty()) {
                pathValues = Arrays.asList(remotePath.split(":"));
            } else {
                pathValues = Collections.emptyList();
            }

        } catch (Exception e) {
            e.printStackTrace();
            pathValues = Collections.emptyList();
        } 
        finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    private String[] tryReadRemoteFile(Session session, String filePath) {
        ChannelExec channel = null;
        try {
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand("cat " + filePath);
            channel.setInputStream(null);
            InputStream input = channel.getInputStream();
            InputStream err = channel.getErrStream();
            channel.connect();

            // Read stdout
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            // Read stderr to check for "No such file or directory"
            BufferedReader errReader = new BufferedReader(new InputStreamReader(err));
            StringBuilder errSb = new StringBuilder();
            String errLine;
            while ((errLine = errReader.readLine()) != null) {
                errSb.append(errLine);
            }

            channel.disconnect();

            if (errSb.toString().contains("No such file or directory")) {
                return null;
            }
            // If file is empty or not readable, return null
            if (sb.length() == 0) {
                return null;
            }
            return sb.toString().split("\n");
        } catch (Exception e) {
            // File not readable or does not exist
            return null;
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
        }
    }

    private String tryRunSingleLineCommand(Session session, String command) {
        ChannelExec channel = null;
        try {
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setInputStream(null);
            InputStream input = channel.getInputStream();
            channel.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String output = reader.readLine();

            channel.disconnect();

            return output;
        } catch (Exception e) {
            return null;
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
        }
    }

    @Override
    public List<String> getResult() {
        return pathValues;
    }
}