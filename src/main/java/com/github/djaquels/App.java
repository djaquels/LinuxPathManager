package com.github.djaquels;

import com.github.djaquels.ui.Labels;
import com.github.djaquels.utils.GlobalPathCommand;
import com.github.djaquels.utils.PathCommand;
import com.github.djaquels.utils.ReadPathInvoker;
import com.github.djaquels.utils.UserPathCommand;

import java.util.Locale;

import org.json.JSONObject;

/**
 * GUI Main class
 */
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.HashMap;

public class App extends Application {
    private ReadPathInvoker invoker = new ReadPathInvoker();
    private ObservableList<String> pathList = FXCollections.observableArrayList();

     private void updatePath(PathCommand command) {
        invoker.setCommand(command);
        pathList.setAll(invoker.fetchPath());
    }

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
        JSONObject mainWindow = conf.getWindowLabels("main");
        primaryStage.setTitle(header);

        /* Read Main UI */
        ListView<String> listView = new ListView<>(pathList);
        
        Button userPathButton = new Button(conf.getWindowValue(mainWindow, "readUsers"));
        userPathButton.setOnAction(e -> updatePath(new UserPathCommand()));
        
        Button systemPathButton = new Button(conf.getWindowValue(mainWindow, "readSystem"));
        systemPathButton.setOnAction(e -> updatePath(new GlobalPathCommand()));
        
        //VBox layoutRead = new VBox(10, userPathButton, systemPathButton, listView);
        //Scene scene = new Scene(layoutRead, 400, 300);

        /* Add new to PATH */

        TextField pathField = new TextField();
        pathField.setPromptText("Ange ny PATH");

        Button updateButton = new Button("Uppdatera PATH");
        updateButton.setOnAction(e -> updatePath(pathField.getText()));

        VBox layout = new VBox(10);
        layout.getChildren().addAll(userPathButton, systemPathButton, listView,pathField, updateButton);


        Scene scene = new Scene(layout, 500, 300);
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

