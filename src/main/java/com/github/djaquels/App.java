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
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.HashMap;

public class App extends Application {
    private ReadPathInvoker invoker = new ReadPathInvoker();
    private ObservableList<String> userPathList = FXCollections.observableArrayList();
    private ObservableList<String> systemPathList = FXCollections.observableArrayList();
     private void updatePath(PathCommand command, ObservableList<String> pathList) {
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
        ListView<String> userListView = new ListView<>(userPathList);
        ListView<String> systemListView = new ListView<>(systemPathList);
        Label userPathLabel = new Label(mainWindow.getString("readUsers"));
        Label systemPathLabel = new Label(mainWindow.getString("readSystem"));
        updatePath(new UserPathCommand(), userPathList);
        updatePath(new GlobalPathCommand(), systemPathList);
        
        /* 
        * Controll panel buttons
        Add new to PATH 
        Update/Edit select item in view
        Save settings
        */
        // Add
        TextField pathField = new TextField();
        pathField.setPromptText(mainWindow.getString("add-label"));

        Button addButton = new Button(mainWindow.getString("add"));
        addButton.setOnAction(e -> updatePathAction(pathField.getText()));

        // Update
        Button updateButton = new Button(mainWindow.getString("update"));

        // Save
        Button saveButton = new Button(mainWindow.getString("save"));

        // Delete

        Button deleButton = new Button(mainWindow.getString("delete"));
        
        //Buttons layout
        HBox buttonBox = new HBox(10); // 10 är mellanrummet mellan knapparna
        buttonBox.getChildren().addAll(addButton, updateButton, saveButton, deleButton);

        VBox layout = new VBox(10);
        layout.getChildren().addAll(userPathLabel,userListView,systemPathLabel, systemListView,pathField, buttonBox);


        Scene scene = new Scene(layout, 600, 350);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void updatePathAction(String newPath) {
        // Implementera logik för att uppdatera PATH här
        System.out.println("Uppdaterar PATH till: " + newPath);
    }

    private void savePathAction(){
        // Implementera logik för att spara PATTH inställningar här
    }

    public static void main(String[] args) {
        launch(args);
    }
}

