package com.github.djaquels.utils;

import com.jcraft.jsch.*;

public class SSHConnectionUtil {
    public static Session connect(String username, String host, int port, String password) throws Exception {
        JSch jsch = new JSch();
        Session session = null;
        // Try public key auth first
        try {
            session = jsch.getSession(username, host, port);
            // Will try public-key from default locations (~/.ssh/id_rsa, etc.)
            session.setConfig("PreferredAuthentications", "publickey");
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(5000);
            return session;
        } catch (JSchException pubEx) {
            // If public-key auth fails, try password
            try {
                session = jsch.getSession(username, host, port);
                if (password != null) session.setPassword(password);
                session.setConfig("PreferredAuthentications", "password");
                session.setConfig("StrictHostKeyChecking", "no");
                session.connect(5000);
                return session;
            } catch (JSchException passEx) {
                // Both failed: show error message
                throw new Exception("SSH Authentication failed. Tried public-key and password. " +
                        "Public-key error: " + pubEx.getMessage() +
                        " Password error: " + passEx.getMessage());
            }
        }
    }
}