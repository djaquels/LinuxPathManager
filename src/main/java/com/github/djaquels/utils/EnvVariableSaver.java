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
import java.util.regex.Pattern;


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
	private boolean validPathValue(String value) {
		String regex = ".*[;=\\[\\]\\.].*";
        // Vérifie si la chaîne contient des caractères interdits
        return !Pattern.matches(regex, value);
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
				List<String> newLines;
				System.out.println("============" + shellName + "============");
				try {
					List<String> lines = Files.readAllLines(bashrcPath, StandardCharsets.UTF_8);
					newLines = new ArrayList<>();
					List<String> pathLines= new ArrayList<>();
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
					pathLines.add(startMarker);
					for(String envVar: valuesList){
                        String[] envString = envVar.split("=");
                        String envKey = envString[0];
                        String envValue = envString.length == 2? envString[1]: "";
                        String separator = shellFilters.get(shellName)[1];
                        String command = shellFilters.get(shellName)[0];
						if(envKey.equals("PATH") || !validPathValue(envValue)){
							continue;
						}
                        pathLines.add(command+envKey + separator + envValue);
                    }
					pathLines.add(endMarker);
					pathLines.removeIf(item -> item.equals("END LinuxPathManagerENVS"));
					pathLines.removeIf(item -> item.equals("END LinuxPathManager"));
					newLines.removeIf(item -> item.equals("END LinuxPathManagerENVS"));
					newLines.removeIf(item -> item.equals("END LinuxPathManager"));
					pathLines.removeIf(item -> {
						return !item.contains("export") && !item.contains("#") && !item.contains("set") && !item.contains("setenv");
					});
					newLines.addAll(pathLines);
					Files.write(bashrcPath, newLines, StandardCharsets.UTF_8, StandardOpenOption.WRITE);
    			} catch (IOException e) {
        			e.printStackTrace();
					newLines = new ArrayList<>();
				    throw new IOException("Error writing to .env file", e);
    			}
    		}
		}

		for (Map.Entry<String, String> entry : shellFiles.entrySet()) {
			String shellConfigFile = entry.getValue();
			StringUtils.fixFile(shellConfigFile);
		}
        }catch (IOException e) {
	        e.printStackTrace();
	    }
    }

    @Override
    public void update(String newLine) throws IOException {
    }
}
