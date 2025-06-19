package com.github.djaquels.utils;

import javafx.collections.ObservableList;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class EnvVariableSaver implements SavePathCommand {
    private String formatEnvVar(String shell, String key, String value) {
        switch (shell.toLowerCase()) {
            case "bash":
            case "zsh":
            case "dash":
                return "export " + key + "=\"" + value + "\"";
            case "fish":
                return "set -x " + key + " \"" + value + "\"";
            case "csh":
            case "tcsh":
                return "setenv " + key + " \"" + value + "\"";
            default:
                throw new IllegalArgumentException("Unsupported shell: " + shell);
        }
    }

    @Override
    public void execute(ObservableList<String> valuesList) {
        try{
             Map<String, String> shellFiles = Map.of(
    	"bash", ".bashrc",
    	"zsh", ".zshrc",
    	"fish", ".config/fish/config.fish",
    	"ksh", ".kshrc",
		"csh", ".cshrc",
		"tcsh", ".tcshrc"
		);
		Map<String, String[]> shellFilters = Map.of(
			"bash", new String[] {"export ", "="},
			"zsh", new String[] {"export ", "="},
    		"fish", new String[] {"set -x ", " "},
    		"ksh", new String[] {"export ", "="},
			"csh", new String[] {"setenv ", "="},
			"tcsh", new String[] {"setenv ", "="}

		);
        String homeDir = System.getProperty("user.home");
		for (Map.Entry<String, String> entry : shellFiles.entrySet()) {
			String shellConfigFile = entry.getValue();
    		Path path = Paths.get(homeDir, shellConfigFile);
    		if (Files.exists(path)) {
				String shellName = entry.getKey();
				Path bashrcPath = Paths.get(homeDir, shellConfigFile);
				try {
					List<String> lines = Files.readAllLines(bashrcPath, StandardCharsets.UTF_8);
					List<String> newLines = new ArrayList<>();
					String startMarker = "#BEGIN LinuxPathManagerENVS";
					String endMarker = "#END LinuxPathManagerENVS";
					boolean inMarker = false;
					for(String line: lines){
						if(line.trim().equals(startMarker)){
							inMarker = true;
							continue;
						}
						if(line.trim().equals(endMarker)){
							inMarker = false;
							continue;
						}
	    				if(!inMarker){
						newLines.add(line);		
	    				}
					}
					// add new PATH entry
					newLines.add(startMarker);
					for(String envVar: valuesList){
                        String[] envString = envVar.split("=");
                        String envKey = envString[0];
                        String envValue = envString.length == 2? envString[1]: "";
                        String separator = shellFilters.get(shellName)[1];
                        String command = shellFilters.get(shellName)[0];
                        newLines.add(command+envKey + separator + envValue);
                    }
					newLines.add(endMarker);
					Files.write(bashrcPath, newLines, StandardCharsets.UTF_8, StandardOpenOption.WRITE);
    			} catch (IOException e) {
        			e.printStackTrace();
				    throw new IOException("Error writing to .env file", e);
    			}
    		}
		}
        }catch (IOException e) {
	        e.printStackTrace();
	    }
    }

    @Override
    public void update(String newLine) throws IOException {
    }
}
