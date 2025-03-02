package com.github.djaquels;

import com.github.djaquels.ui.Labels;
import com.github.djaquels.utils.GlobalPathCommand;
import com.github.djaquels.utils.PathCommand;
import com.github.djaquels.utils.ReadPathInvoker;
import com.github.djaquels.utils.UserPathCommand;
import com.github.djaquels.utils.SavePathCommand;
import com.github.djaquels.utils.UserSaveCommand;
import com.github.djaquels.utils.StringUtils;
//import com.github.djaquels.utils.savePathCommand;

import java.util.Locale;

import org.json.JSONObject;

/**
 * GUI Main class
 */
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
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
    private SavePathCommand saveUserPathCommand = new UserSaveCommand();
    private String userPathAsString;
    private String userPathMD5;

    private void updatePath(PathCommand command, ObservableList<String> pathList) {
        invoker.setCommand(command);
        pathList.setAll(invoker.fetchPath());
    }

    private String getLocalLanguage(){
        Locale currentLocale = Locale.getDefault();
        String language = currentLocale.getLanguage();
        HashMap languagesMap = new HashMap<String, String>();
        languagesMap.put("en", "english");
        languagesMap.put("sv", "swedish");
        String appLanguage = (languagesMap.containsKey(language)) ? languagesMap.get(language).toString() : "english";
        return  appLanguage;
    } 
    @Override
    public void start(Stage primaryStage) {
        String appLanguage = getLocalLanguage();
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
        userPathAsString = String.join(":", userPathList);
        userPathMD5 = StringUtils.getMD5(userPathAsString);
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
        addButton.setOnAction(e -> addPathAction(pathField.getText()));

        // Update
        Button updateButton = new Button(mainWindow.getString("update"));

        // Save
        Button saveButton = new Button(mainWindow.getString("save"));
        saveButton.setOnAction(e -> savePathAction());

        // Delete

        Button deleButton = new Button(mainWindow.getString("delete"));
        
        // Env Vars Window
        Button toEnvVars = new Button(mainWindow.getString("to-env"));
        
        //Buttons layout
        HBox buttonBox = new HBox(10); // 10 är mellanrummet mellan knapparna
        buttonBox.getChildren().addAll(addButton, updateButton, saveButton, deleButton, toEnvVars);

        VBox layout = new VBox(10);
        layout.getChildren().addAll(userPathLabel,userListView,systemPathLabel, systemListView,pathField, buttonBox);


        Scene scene = new Scene(layout, 750, 350);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void addPathAction(String newPath) {
        // Implementera logik för att uppdatera PATH här
    String appLanguage = getLocalLanguage();
    Labels conf = Labels.getInstance(appLanguage);
    JSONObject addWindow = conf.getWindowLabels("add");
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle(addWindow.getString("header"));
    alert.setHeaderText(addWindow.getString("label"));
    alert.setContentText(addWindow.getString("action"));

    ButtonType userButton = new ButtonType(addWindow.getString("user-label"));
    ButtonType systemButton = new ButtonType(addWindow.getString("system-label"));
    ButtonType cancelButton = new ButtonType(addWindow.getString("cancel-label"), ButtonBar.ButtonData.CANCEL_CLOSE);

    alert.getButtonTypes().setAll(userButton, systemButton, cancelButton);

    alert.showAndWait().ifPresent(result -> {
        if (result == userButton) {
            addPathToList(userPathList, newPath);
        } else if (result == systemButton) {
            addPathToList(systemPathList, newPath);
        }
    });

    }

    private void addPathToList(ObservableList<String> list, String value) {
        String newPath = value.trim();
        if (!newPath.isEmpty() && !list.contains(newPath)) {
            list.add(newPath);
        }
    }

    private void savePathAction(){
        String onMemoryUserPath = String.join(":", userPathList);
        String onMemoryUserMd5 = StringUtils.getMD5(onMemoryUserPath);
        if(!onMemoryUserMd5.equals(userPathMD5)){
            saveUserPathCommand.execute(userPathList);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

