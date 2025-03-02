package com.github.djaquels.utils;
import javafx.collections.ObservableList;

public interface SavePathCommand {
    void execute(ObservableList<String> valuesList);
    void updateBashrc(String newPath);
}
