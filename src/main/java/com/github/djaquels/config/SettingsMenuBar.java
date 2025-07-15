package com.github.djaquels.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class SettingsMenuBar extends MenuBar {

    private static final Path CONFIG_PATH = Paths.get("/usr/share/linuxpathmanager/config.json");
    private final ObjectMapper objectMapper;

    // Default shells and themes
    private final Map<String, String> shellFiles = Map.of(
            "bash", ".bashrc",
            "zsh", ".zshrc",
            "fish", ".config/fish/config.fish",
            "ksh", ".kshrc",
            "csh", ".cshrc",
            "tcsh", ".tcshrc"
    );
    private final String[] themes = {"default", "neon-green", 
    "gnome-inspired", "kde-inspired", "gnome-inspired-dark", "kde-inspired-dark"};
    private Config config;

    private final Scene scene;

    public SettingsMenuBar(Scene scene) {
        this.scene = scene;
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

        // Load existing or default config
        config = loadConfig();

        // Create settings menu
        Menu settingsMenu = new Menu("Settings");

        // Theme selector
        Menu themeMenu = new Menu("Select Theme");
        ToggleGroup themeGroup = new ToggleGroup();
        for (String theme : themes) {
            RadioMenuItem themeItem = new RadioMenuItem(capitalize(theme));
            themeItem.getStyleClass().add("menu-item");
            themeItem.setSelected(theme.equals(config.getTheme()));
            themeItem.setOnAction(e -> {
                config.setTheme(theme);
                saveConfig();
                applyTheme(theme);
            });
            themeItem.setToggleGroup(themeGroup);
            themeMenu.getItems().add(themeItem);
            
        }
        
        // Shell selector
        Menu shellsMenu = new Menu("Select Shells to Configure");
        for (String shell : shellFiles.keySet()) {
            CheckMenuItem shellItem = new CheckMenuItem(shell);
            shellItem.setSelected(config.getEnabledShells().getOrDefault(shell, false));
            shellItem.setOnAction(e -> {
                config.getEnabledShells().put(shell, shellItem.isSelected());
                saveConfig();
            });
            shellsMenu.getItems().add(shellItem);
        }
        

        // Add menus to the bar
        settingsMenu.getItems().addAll(themeMenu, new SeparatorMenuItem(), shellsMenu);
        this.getMenus().add(settingsMenu);

        // Apply the current theme at startup
        applyTheme(config.getTheme());
    }

    private void applyTheme(String theme) {
        scene.getStylesheets().clear();
        String cssPath = "/css/" + theme + ".css";
        scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
    }

    private Config loadConfig() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                return objectMapper.readValue(CONFIG_PATH.toFile(), Config.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Return default config if file doesn’t exist
        Config defaultConfig = new Config();
        Map<String, Boolean> enabledShells = new HashMap<>();
        shellFiles.keySet().forEach(shell -> enabledShells.put(shell, true));
        defaultConfig.setTheme("default");
        defaultConfig.setEnabledShells(enabledShells);
        return defaultConfig;
    }

    private void saveConfig() {
        try {
            if (!Files.exists(CONFIG_PATH.getParent())) {
                Files.createDirectories(CONFIG_PATH.getParent());
            }
            objectMapper.writeValue(CONFIG_PATH.toFile(), config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public Map<String, String> getEnabledShellFiles() {
        Map<String, String> filteredShells = new HashMap<>();
        config.getEnabledShells().forEach((shell, enabled) -> {
            if (enabled) {
                filteredShells.put(shell, shellFiles.get(shell));
            }
        });
        return filteredShells;
    }

    public String getCurrentTheme(){
        return this.config.getTheme();
    }
}
