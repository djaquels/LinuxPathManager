package com.github.djaquels;

import com.github.djaquels.ui.Labels;

import java.util.Locale;

/**
 * GUI Main class
 */
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.HashMap;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        Locale currentLocale = Locale.getDefault();
        String language = currentLocale.getLanguage();
        HashMap languagesMap = new HashMap<String, String>();
        languagesMap.put("en", "english");
        languagesMap.put("sv", "swedish");
        String appLanguage = (languagesMap.containsKey(language)) ? languagesMap.get(language).toString() : "english";
        
        Labels conf = Labels.getInstance(appLanguage);
        String header = conf.getValue("appName");
        primaryStage.setTitle(header);

        TextField pathField = new TextField();
        pathField.setPromptText("Ange ny PATH");

        Button updateButton = new Button("Uppdatera PATH");
        updateButton.setOnAction(e -> updatePath(pathField.getText()));

        VBox layout = new VBox(10);
        layout.getChildren().addAll(pathField, updateButton);

        Scene scene = new Scene(layout, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void updatePath(String newPath) {
        // Implementera logik för att uppdatera PATH här
        System.out.println("Uppdaterar PATH till: " + newPath);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

