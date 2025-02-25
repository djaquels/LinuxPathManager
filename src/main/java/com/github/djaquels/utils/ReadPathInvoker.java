package com.github.djaquels.utils;

import java.util.List;

public class ReadPathInvoker {
    private PathCommand readCommand;

    public void setCommand(PathCommand command){
        this.readCommand = command;
    }

    public List<String> fetchPath(){
        this.readCommand.execute();
        return this.readCommand.getResult();
    }
}
