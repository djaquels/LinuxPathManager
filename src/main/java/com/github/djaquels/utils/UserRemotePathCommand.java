package com.github.djaquels.utils;

import com.jcraft.jsch.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class UserRemotePathCommand implements PathCommand {
    private final String username;
    private final String host;
    private final int port;
    private final String password;
    private List<String> pathValues;

    Map<String, String> shellFilters = Map.of(
			"bash", ":",
			"zsh", ":",
    		"fish", " ",
    		"ksh", ":",
			"csh", ":",
			"tcsh", ":"
		);

    public UserRemotePathCommand(String username, String host, int port, String password) {
        this.username = username;
        this.host = host;
        this.port = port;
        this.password = password;
    }

    private String shellType(Session session, String userName){
        ChannelExec channel = null;
        List<String> userInfo;
        try {
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand("cat /etc/passwd | grep " + userName);
            channel.setInputStream(null);
            InputStream input = channel.getInputStream();
            channel.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String output = reader.readLine();
            if (output != null && !output.isEmpty()) {
                // Use ':' as the path separator for Linux/Unix systems
                userInfo = Arrays.asList(output.split(":"));
            } else {
                userInfo = Collections.emptyList();
            }
            int userInfoSize = userInfo.size();
            if( userInfoSize == 0){
                return "bash";
            }else{
                List<String> shellInfo = Arrays.asList(userInfo.get(userInfoSize-1).split("/"));
                return shellInfo.get(shellInfo.size()-1);
            }

        } catch (Exception e) {
            e.printStackTrace();
            userInfo = Collections.emptyList();
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
        }
        return "bash";

    }

    @Override
    public void execute() {
        Session session = null;
        ChannelExec channel = null;
        try {
            session = SSHConnectionUtil.connect(this.username, this.host, this.port, this.password);
            String userShell = shellType(session, this.username);
            String separator = shellFilters.get(userShell);
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand("echo $PATH");
            channel.setInputStream(null);
            InputStream input = channel.getInputStream();
            channel.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String output = reader.readLine();
            if (output != null && !output.isEmpty()) {
                // Use ':' as the path separator for Linux/Unix systems
                pathValues = Arrays.asList(output.split(separator));
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