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

	public static void removeAfterSecondOccurrence(List<String> lines, String filter) {
        // Liste pour stocker les indices des occurrences du filtre
        List<Integer> indices = new ArrayList<>();

        // Parcourir la liste pour trouver les indices des occurrences du filtre
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).equals(filter)) {
                indices.add(i);
                if (indices.size() == 2) {
                    break; // On a trouvé les deux premières occurrences
                }
            }
        }

        // Si deux occurrences sont trouvées, supprimer les lignes après la deuxième occurrence
        int lastIndexToKeep = indices.get(0);
        // Supprimer toutes les lignes après le deuxième indice
        lines.subList(lastIndexToKeep + 1, lines.size()).clear();
        
    }

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
				List<String> newLines = new ArrayList<>();
				List<String> newLinesEnvs = new ArrayList<>(); 
				try {
					List<String> lines = Files.readAllLines(bashrcPath, StandardCharsets.UTF_8);
					newLines = new ArrayList<>();
					newLinesEnvs = new ArrayList<>(); 
					String startMarker = "#BEGIN LinuxPathManager";
					String endMarker = "#END LinuxPathManager";
					String startMarkerEnvs = "#BEGIN LinuxPathManagerENVS";
					String endMarkerEnvs = "#END LinuxPathManagerENVS";
					boolean inMarker = false;
					boolean inEnvsMarker = false;
					for(String line: lines){
						if(line.trim().equals(startMarkerEnvs)){
							inEnvsMarker = true;
						}
						if(line.trim().equals(endMarkerEnvs)){
							inEnvsMarker = false;
							newLinesEnvs.add(endMarkerEnvs);
							continue;
						}
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
							if(inEnvsMarker){
								newLinesEnvs.add(line);
							}else{
								newLines.add(line);		
							}
	    				}
					}
					// add new PATH entry
					String newPathEntry = pathClient.buildPathString(currentPath,newPath, shellName);
					newLines.add(startMarker);
					newLines.add(newPathEntry.trim());
					newLines.add(endMarker);
					newLinesEnvs.removeIf(item -> {
						return !item.contains("export") && !item.contains("#") && !item.contains("set") && !item.contains("setenv");
					});
					newLines.addAll(newLinesEnvs);
					List<String> filters = new ArrayList<>();
					filters.add(startMarker);
					filters.add(startMarkerEnvs);
					filters.add(endMarker);
					filters.add(endMarkerEnvs);
					List<String> cleanLines = BuildPathString.filterIncompleteSubstrings(newLines, filters);
					cleanLines.removeIf(item -> {
						String f = "END LinuxPathManagerENVS";
						for(int i=0; i <= f.length(); i++){
							String substring = f.substring(i,f.length());
							if(item.equals(substring)){
								return true;
							}
						}
						return false;
					});
					removeAfterSecondOccurrence(cleanLines, endMarkerEnvs);
					Files.write(bashrcPath, cleanLines, StandardCharsets.UTF_8, StandardOpenOption.WRITE);
				} catch (IOException e) {
        			e.printStackTrace();
					newLines = new ArrayList<>();
					newLinesEnvs = new ArrayList<>(); 
					throw new IOException("Error writing to .bashrc file", e);
    			}
    		}
		}

		for (Map.Entry<String, String> entry : shellFiles.entrySet()) {
			String shellConfigFile = entry.getValue();
			StringUtils.fixFile(shellConfigFile);
			
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
