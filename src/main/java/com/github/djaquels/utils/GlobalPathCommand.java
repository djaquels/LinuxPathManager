package com.github.djaquels.utils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.Collections;

public class GlobalPathCommand implements PathCommand{
    private List<String> pathValues;
    private static final String PROFILE_PATH = "/etc/profile.d/linuxpathmanager.sh";
    @Override
    public void execute() {
        pathValues = readPathFromProfile();
        if (pathValues == null || pathValues.isEmpty()) {
            // fallback to java.library.path as before
            String libPath = System.getProperty("java.library.path");
            if (libPath != null) {
                pathValues = Arrays.asList(libPath.split(File.pathSeparator));
            } else {
                pathValues = Collections.emptyList();
            }
        }
    }

    private List<String> readPathFromProfile() {
        File file = new File(PROFILE_PATH);
        if (!file.exists()) return null;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            boolean insideBlock = false;
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("export PATH=") || line.startsWith("PATH=")) {
                        String[] parts = line.split("=", 2);
                        if (parts.length == 2) {
                            String pathValue = parts[1].replaceAll("^['\"]|['\"]$", "");
                            return Arrays.asList(pathValue.split(File.pathSeparator));
                        }
                }
                
            }
        } catch (IOException e) {
            // Ignore and fallback
        }
        return null;
    }

    @Override
    public List<String> getResult(){
        return pathValues;
    }
    
}
