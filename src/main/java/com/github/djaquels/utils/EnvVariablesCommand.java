package com.github.djaquels.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.io.File;

public class EnvVariablesCommand implements PathCommand {

    private List<String> result = new ArrayList<>();
    
    private static final Map<String, String> SHELL_FILES = Map.of(
        "bash", ".bashrc",
        "zsh", ".zshrc",
        "fish", ".config/fish/config.fish",
        "ksh", ".kshrc",
        "csh", ".cshrc",
        "tcsh", ".tcshrc"
    );

    // Patterns to extract env var assignments for each shell
    private static final Map<String, Pattern> SHELL_PATTERNS = Map.of(
        "bash", Pattern.compile("^export\\s+([A-Za-z_][A-Za-z0-9_]*)=(.*)$"),
        "zsh", Pattern.compile("^export\\s+([A-Za-z_][A-Za-z0-9_]*)=(.*)$"),
        "ksh", Pattern.compile("^export\\s+([A-Za-z_][A-Za-z0-9_]*)=(.*)$"),
        "fish", Pattern.compile("^set\\s+-x\\s+([A-Za-z_][A-Za-z0-9_]*)\\s+(.+)$"),
        "csh", Pattern.compile("^setenv\\s+([A-Za-z_][A-Za-z0-9_]*)\\s+(.+)$"),
        "tcsh", Pattern.compile("^setenv\\s+([A-Za-z_][A-Za-z0-9_]*)\\s+(.+)$")
    );

    private static final String BEGIN_MARKER = "#BEGIN LinuxPathManagerENVS";
    private static final String END_MARKER = "#END LinuxPathManagerENVS";

     private List<String> readEnvFromShellConfigs() {
        List<String> envVars = new ArrayList<>();
        String home = System.getProperty("user.home");
        for (Map.Entry<String, String> entry : SHELL_FILES.entrySet()) {
            String shell = entry.getKey();
            String configFile = entry.getValue();
            File file = new File(home, configFile);
            if (!file.exists()) continue;

            Pattern pattern = SHELL_PATTERNS.get(shell);
            if (pattern == null) continue;

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                boolean insideBlock = false;
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().equals(BEGIN_MARKER)) {
                        insideBlock = true;
                        continue;
                    } else if (line.trim().equals(END_MARKER)) {
                        insideBlock = false;
                        continue;
                    }
                    if (insideBlock) {
                        // Try to match the shell-specific pattern
                        String cleanLine = line.trim();
                        java.util.regex.Matcher matcher = pattern.matcher(cleanLine);
                        if (matcher.matches()) {
                            String envKey = matcher.group(1);
                            String envValue = matcher.group(2).replaceAll("^['\"]|['\"]$", "");
                            envVars.add(envKey + "=" + envValue);
                        }
                    }
                }
                return envVars.isEmpty() ? null : envVars;
            } catch (IOException e) {
                // Ignore and try next file
            }
        }
        return envVars.isEmpty() ? null : envVars;
    }

    @Override
    public void execute() {
        result = readEnvFromShellConfigs();
        if (result == null || result.isEmpty()) {
            // fallback to current environment
            Map<String, String> envVars = System.getenv();
            result = new ArrayList<>();
            for (Map.Entry<String, String> entry : envVars.entrySet()) {
                result.add(entry.getKey() + "=" + entry.getValue());
            }
        }
        Collections.sort(result);
    }

    @Override
    public List<String> getResult() {
        this.execute();
        Collections.sort(result);
        return result;
    }
}