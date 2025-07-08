package com.github.djaquels.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class StringUtils {

    public static String getMD5(String source) {
        try {
            byte[] bytesOfMessage = source.getBytes(StandardCharsets.UTF_8);
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(bytesOfMessage);
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            String hashValue = sb.toString();
            return hashValue;

        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error getting md5");
            return "";
        }

    }

    public static boolean validPath(String candiate){
        return candiate.contains("export") || candiate.contains("set") || candiate.contains("setenv") || candiate.startsWith("#");
    }
    public static void fixFile(String shellFile){
        String homeDir = System.getProperty("user.home");
        Path path = Paths.get(homeDir, shellFile);
        String BEGIN_MARKER = "#BEGIN LinuxPathManager";
        String END_MARKER = "#END LinuxPathManager";
        String BEGIN_MARKER_ENVS = "#BEGIN LinuxPathManagerENVS";
        String END_MARKER_ENVS = "#END LinuxPathManagerENVS";
        if(Files.exists(path)){
            Path bashrcPath = Paths.get(homeDir, shellFile);
            List<String> newLines = new ArrayList<>();
            try{
                String endMarkerEnvs = "#END LinuxPathManagerENVS";
                List<String> lines = Files.readAllLines(bashrcPath, StandardCharsets.UTF_8);
                boolean include = true;
                boolean isPath = false;
                boolean isEnv = false;
                for(String line: lines){
                    if(include){
                        if(line.trim().equals(BEGIN_MARKER)){
                            newLines.add(line);
                            isPath = true;
                        }
                        else if(line.trim().equals(END_MARKER)){
                            newLines.add(line);
                            isPath = false;
                        }
                        else if(line.trim().equals(BEGIN_MARKER_ENVS)){
                            newLines.add(line);
                            isEnv = true;
                        }
                        else if(line.trim().equals(END_MARKER_ENVS)){
                            include = false;
                            isEnv = false;
                            newLines.add(line);
                            break;
                        }else{
                            if(isPath){
                                if(validPath(line)){
                                    newLines.add(line);
                                }
                            }
                            else if(isEnv){
                                if(validPath(line)){
                                    newLines.add(line);
                                }
                            }
                            else{
                                newLines.add(line);
                            }
                        }
                    }
                }
                Files.write(bashrcPath, newLines, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            }catch(IOException e){
                e.printStackTrace();
            }
        }

    }
}
