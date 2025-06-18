package com.github.djaquels.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

import javafx.collections.ObservableList;
import java.util.ArrayList;

public class UserSaveCommand implements SavePathCommand {
    private BuildPathString pathClient = BuildPathString.getBuildPathClient();
    @Override
    public void update(String newPath) throws IOException, InterruptedException {
		Map<String, String> shellFiles = Map.of(
    	"bash", ".bashrc",
    	"zsh", ".zshrc",
    	"fish", ".config/fish/config.fish",
    	"ksh", ".kshrc",
		"csh", ".cshrc",
		"tcsh", ".tcshrc"
		);
		Map<String, String> shellFilters = Map.of(
			"bash", "export PATH=",
			"zsh", "export PATH=",
    		"fish", "set -gx PATH",
    		"ksh", "export PATH=",
			"csh", "setenv PATH",
			"tcsh", "setenv PATH"

		);
		String homeDir = System.getProperty("user.home");
		for (Map.Entry<String, String> entry : shellFiles.entrySet()) {
			String shellConfigFile = entry.getValue();
    		Path path = Paths.get(homeDir, shellConfigFile);
    		if (Files.exists(path)) {
				String shellName = entry.getKey();
				Path bashrcPath = Paths.get(homeDir, shellConfigFile);
				String currentPath = "";
				try {
					List<String> lines = Files.readAllLines(bashrcPath, StandardCharsets.UTF_8);
					List<String> newLines = new ArrayList<>();
					String startMarker = "#BEGIN LinuxPathManager";
					String endMarker = "#END LinuxPathManager";
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
						if(inMarker){
						currentPath += line.trim();
						}
	    				        if(!inMarker){
							newLines.add(line);		
	    				        }
					}
					// add new PATH entry
					String newPathEntry = pathClient.buildPathString(currentPath,newPath, shellName);
					newLines.add(startMarker);
					newLines.add(newPathEntry.trim());
					newLines.add(endMarker);
					Files.write(bashrcPath, newLines, StandardCharsets.UTF_8, StandardOpenOption.WRITE);
    			} catch (IOException e) {
        			e.printStackTrace();
				throw new IOException("Error writing to .bashrc file", e);
    			}
    		}
		}
	}

    @Override
    public void execute(ObservableList<String> valuesList) {
        String newPath = String.join(":", valuesList);
        try {
	update(newPath);
        String currentPath = System.getenv("PATH");
        String updatedPath = currentPath + ":" + newPath;
        System.setProperty("java.library.path", updatedPath);
	} catch (IOException | InterruptedException e) {
	    e.printStackTrace();
	}
    }
}
