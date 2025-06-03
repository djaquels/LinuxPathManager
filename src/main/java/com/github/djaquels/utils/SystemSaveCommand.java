package com.github.djaquels.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.ArrayList;
import javafx.collections.ObservableList;

public class SystemSaveCommand implements SavePathCommand {
    private BuildPathString pathClient = BuildPathString.getBuildPathClient();
    private String sudoPassword;
    private static final String SCRIPT_PATH = "/etc/profile.d/linuxpathmanager.sh";

    public SystemSaveCommand(String sudoPassword) {
	this.sudoPassword = sudoPassword;
    }

    // run a shell command as sudo
    private void runSudoCommand(String[] cmd) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("sudo", "-S");
	for(String c: cmd){
            processBuilder.command().add(c);
	}
	Process process = processBuilder.start();
	try(OutputStream os = process.getOutputStream()){
	    os.write((sudoPassword + "\n").getBytes(StandardCharsets.UTF_8));
	    os.flush();
	}
	int exitCode = process.waitFor();
	if (exitCode != 0) {
	    throw new IOException("Command failed with exit code " + exitCode);
	}
	
    }
    // ensure script exists and is executable
    private void ensureScriptExists() throws IOException, InterruptedException {
        Path scriptPath = Paths.get(SCRIPT_PATH);
	if(!Files.exists(scriptPath)){
            Path tmpPath = Paths.get("/tmp/linuxpathmanager.sh.tmp");
	    List<String> initialLInes = List.of("#!/bin/bash", "");
	    Files.write(tmpPath, initialLInes, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
	    // flytta filen med sudo
	    runSudoCommand(new String[]{"mv", tmpPath.toString(), scriptPath.toString()});
	    // Ge filen exekveringsrättigheter
	    // chmod +x /etc/profile.d/linuxpathmanager.sh
	    runSudoCommand(new String[]{"chmod", "+x", SCRIPT_PATH});
	}
    }

    @Override
    public void update(String newPath) throws IOException, InterruptedException {
	// read the current content if exists
	Path scriptPath = Paths.get(SCRIPT_PATH);
	List<String> lines;
	if(Files.exists(scriptPath)){
	  lines = Files.readAllLines(scriptPath, StandardCharsets.UTF_8);
	}else{
	  lines = new ArrayList<>();
          lines = List.of("#!/bin/bash");
	}    
	int pathIndex = -1;
	String currentPathString = "";
	for(int i=0; i < lines.size(); i++){
          if(lines.get(i).startsWith("export PATH=")){
	      pathIndex = i;
	      currentPathString = lines.get(i);
	      break;
	  }
	}

	if(pathIndex == -1) {
	    // If no PATH line exists, add it
	    lines.add("export PATH=" + newPath);
	} else {
	    // If it exists, update it
	  String newPathString = pathClient.buildPathString(currentPathString,newPath);
	  lines.set(pathIndex, newPathString);
	}
	// write to temp file
	Path tmpPath = Paths.get("/tmp/linuxpathmanager.sh.tmp");
	Files.write(tmpPath, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
	// move temp file to script path with sudo
	runSudoCommand(new String[]{"mv", tmpPath.toString(), SCRIPT_PATH});
	runSudoCommand(new String[]{"chmod", "+x", SCRIPT_PATH});

    } 

    @Override
    public void execute(ObservableList<String> valuesList) {
        String newPath = String.join(":", valuesList);
	try{
            ensureScriptExists();
	    update(newPath);

	} catch (IOException | InterruptedException e) {
	    e.printStackTrace();
    }
    }
}
