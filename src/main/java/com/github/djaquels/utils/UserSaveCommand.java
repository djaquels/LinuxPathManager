package com.github.djaquels.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import javafx.collections.ObservableList;

public class UserSaveCommand implements SavePathCommand {
    private BuildPathString pathClient = BuildPathString.getBuildPathClient();
    @Override
    public void updateBashrc(String newPath) {
    String homeDir = System.getProperty("user.home");
    Path bashrcPath = Paths.get(homeDir, ".bashrc");
    
    try {
        List<String> lines = Files.readAllLines(bashrcPath, StandardCharsets.UTF_8);
        int pathIndex = 0;
        int currentIndex = 0;
        String currentPathString = "";
        for(String current : lines) {
            if(current.contains("export PATH=")){
                pathIndex = currentIndex;
                currentPathString = current;
                break;
            }
            currentIndex++;
        }
        if(currentPathString.equals("")) {
            System.out.println("Modifying existing path");
            lines.add("export PATH="+ newPath);
        }else{
            String newPathString = pathClient.buildPathString(currentPathString, newPath);
            System.out.println("Updating current path with " + newPathString);
            lines.set(pathIndex, newPathString);
        }
        Files.write(bashrcPath, lines, StandardCharsets.UTF_8, StandardOpenOption.WRITE);
    } catch (IOException e) {
        e.printStackTrace();
    }
}

    @Override
    public void execute(ObservableList<String> valuesList) {
        String newPath = String.join(":", valuesList);
        updateBashrc(newPath);
        String currentPath = System.getenv("PATH");
        String updatedPath = currentPath + ":" + newPath;
        System.setProperty("java.library.path", updatedPath);
    }
}
