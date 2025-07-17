package com.github.djaquels.config;

import java.util.Map;

public class Config {
    private String theme;
    private Map<String, Boolean> enabledShells;

    public Config() {}

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public Map<String, Boolean> getEnabledShells() {
        return enabledShells;
    }

    public void setEnabledShells(Map<String, Boolean> enabledShells) {
        this.enabledShells = enabledShells;
    }
}
