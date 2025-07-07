package com.github.djaquels.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.Collections;

public class UserPathCommand implements PathCommand {
    private String homeDir = System.getProperty("user.home");
    private List<String> pathValues;
    private static final String BEGIN_MARKER = "#BEGIN LinuxPathManager";
    private static final String END_MARKER = "#END LinuxPathManager";
    private Map<String, String> shellFiles = Map.of(
    	"bash", ".bashrc",
    	"zsh", ".zshrc",
    	"fish", ".config/fish/config.fish",
    	"ksh", ".kshrc",
		"csh", ".cshrc",
		"tcsh", ".tcshrc"
		);
    Map<String, Pattern> shellFilters = Map.of(
			"bash", Pattern.compile("^(export\\s+)?PATH=(.*)$"),
			"zsh", Pattern.compile("^(export\\s+)?PATH=(.*)$"),
    		"fish", Pattern.compile("^(set\\s+)-gx?PATH(.*)$"),
    		"ksh", Pattern.compile("^(export\\s+)?PATH=(.*)$"),
			"csh", Pattern.compile("^(setenv\\s+)?PATH(.*)$"),
			"tcsh", Pattern.compile("^(setenv\\s+)?PATH(.*)$")

	);
    
    Map<String, String> separators = Map.of(
        "bash",":",
        "zsh",":",
        "ksh", ".kshrc",
		"csh", ":",
		"tcsh", ":",
        "fish", " "
    );
    String home = System.getProperty("user.home");
    private List<String> readPathFromShellConfigs(){
        for(Map.Entry<String, String> entry: shellFiles.entrySet()){
            String shellConfigFile = entry.getValue();
            String shellName = entry.getKey();
    		Path path = Paths.get(homeDir, shellConfigFile);
            if(Files.exists(path)){
                File file = new File(home, shellConfigFile);
                try(BufferedReader br = new BufferedReader(new FileReader(file))){
                    boolean insideBlock = false;
                    String line;
                    while ((line = br.readLine()) != null) {
                    if (line.trim().equals(BEGIN_MARKER)) {
                        insideBlock = true;
                    } else if (line.trim().equals(END_MARKER)) {
                        insideBlock = false;
                    } else if (insideBlock) {
                        // Try to find a PATH assignment in block
                        Pattern PATH_PATTERN = shellFilters.get(shellName);
                        java.util.regex.Matcher matcher = PATH_PATTERN.matcher(line.trim());
                        if (matcher.find()) {
                            String pathValue = matcher.group(2);
                            // Remove possible quotes
                            pathValue = pathValue.replaceAll("^['\"]|['\"]$", "");
                            return Arrays.asList(pathValue.split(separators.get(shellName)));
                        }
                    }
                }
                }
                catch (IOException e) {
                // Ignore and try next 
                System.out.println(e);
                }
            }
        }
        return null;

    }

    @Override
    public void execute() {
        pathValues = readPathFromShellConfigs();
        if (pathValues == null || pathValues.isEmpty()) {
            String envPath = System.getenv("PATH");
            if (envPath != null) {
                pathValues = Arrays.asList(envPath.split(File.pathSeparator));
            } else {
                pathValues = Collections.emptyList();
            }
        }
    }

    @Override
    public List<String> getResult(){
        this.execute();
        return pathValues;
    }

}
