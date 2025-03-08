package com.github.djaquels.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public  final   class BuildPathString {
    private static BuildPathString  client;
    private BuildPathString(){}

    public static BuildPathString getBuildPathClient(){
        if(client == null){
            client = new BuildPathString();
        }
        return client;
    }

    public  String buildPathString(String currentPath, String newPath){
        String result = "export PATH=";
        List<String> currentPathList = Arrays.asList(currentPath.split("="));
        currentPathList = currentPathList.subList(1, currentPathList.size());
        Set<String> newPathList = new HashSet<>(Arrays.asList(newPath.split(":")));
        for(String current: currentPathList){
            newPathList.remove(current);
        }
        result = result + String.join(":", newPathList);
        return result;
    }

}
