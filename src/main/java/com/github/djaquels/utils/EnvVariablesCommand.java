package com.github.djaquels.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EnvVariablesCommand implements PathCommand {

    private List<String> result = new ArrayList<>();

    @Override
    public void execute() {
        Map<String, String> envVars = System.getenv();
        result.clear(); // optional, in case you run it multiple times
        for (Map.Entry<String, String> entry : envVars.entrySet()) {
            result.add(entry.getKey() + "=" + entry.getValue());
        }
    }

    @Override
    public List<String> getResult() {
        if(result.size() == 0 ){
            this.execute();
        }
        return result;
    }
}