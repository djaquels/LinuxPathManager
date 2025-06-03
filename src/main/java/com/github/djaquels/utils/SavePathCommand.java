package com.github.djaquels.utils;
import javafx.collections.ObservableList;
import java.io.IOException;

public interface SavePathCommand {
    void execute(ObservableList<String> valuesList);
    void update(String newPath) throws IOException, InterruptedException;
}
