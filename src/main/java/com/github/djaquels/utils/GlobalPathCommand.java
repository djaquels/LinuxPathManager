package com.github.djaquels.utils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class GlobalPathCommand implements PathCommand{
    private List<String> pathValues;

    @Override
    public void execute(){
        pathValues = Arrays.asList(System.getProperty("java.library.path").split(File.pathSeparator));
    }

    @Override
    public List<String> getResult(){
        return pathValues;
    }
    
}
