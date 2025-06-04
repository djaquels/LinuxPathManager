package com.github.djaquels.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import javafx.collections.ObservableList;
import java.util.ArrayList;

public class UserSaveCommand implements SavePathCommand {
    private BuildPathString pathClient = BuildPathString.getBuildPathClient();
    @Override
    public void update(String newPath) throws IOException, InterruptedException {
    String homeDir = System.getProperty("user.home");
    Path bashrcPath = Paths.get(homeDir, ".bashrc");
    String currentPath = "";
    try {
        List<String> lines = Files.readAllLines(bashrcPath, StandardCharsets.UTF_8);
        List<String> newLines = new ArrayList<>();
	// clean up existing PATH entries
	lines.removeIf(line -> line.startsWith("export PATH="));
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
	String newPathEntry = pathClient.buildPathString(currentPath,newPath, "bash");
	newLines.add(startMarker);
	newLines.add(newPathEntry);
	newLines.add(endMarker);
	Files.write(bashrcPath, newLines, StandardCharsets.UTF_8, StandardOpenOption.WRITE);
    } catch (IOException e) {
        e.printStackTrace();
	throw new IOException("Error writing to .bashrc file", e);
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
