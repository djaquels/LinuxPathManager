package com.github.djaquels;

import com.github.djaquels.utils.PathCommand;
import com.github.djaquels.utils.LanguageUtils;
import com.github.djaquels.ui.Labels;
import com.github.djaquels.utils.SavePathCommand;
import org.json.JSONObject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;



public class EnvVars  {
     private final PathCommand envVarsCommand;
     private Labels conf;
     private final SavePathCommand saveCommand;
    // Remote controlers
    private Label remoteModeLabel;
    private Boolean remoteModeActive;
    private String theme;

    public EnvVars(PathCommand envVarsCommand, SavePathCommand envVarsSaveCommand, Boolean isRemote, String theme) {
        this.remoteModeActive = isRemote;
        this.envVarsCommand = envVarsCommand;
        this.conf = LanguageUtils.getWindowConfs();
        this.saveCommand = envVarsSaveCommand;
        this.theme = theme;
    }

    public void showWindow(Stage parentStage) {
        ObservableList<String> envVarsList = FXCollections.observableArrayList(envVarsCommand.getResult());

        ListView<String> listView = new ListView<>(envVarsList);
        JSONObject mainWindow = conf.getWindowLabels("envVars");
        TextField keyField = new TextField();
        keyField.setPromptText(mainWindow.getString("key"));
        TextField valueField = new TextField();
        valueField.setPromptText(mainWindow.getString("value"));
        remoteModeLabel = new Label("");
        remoteModeLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        if(remoteModeActive){
            remoteModeLabel.setText("Remote mode is active");
        }else{
            remoteModeLabel.setText("");
        }
        Button addButton = new Button(mainWindow.getString("add"));
        Button updateButton = new Button(mainWindow.getString("update"));
        Button deleteButton = new Button(mainWindow.getString("delete"));
        Button saveButton = new Button(mainWindow.getString("save"));

        addButton.setOnAction(e -> {
            String key = keyField.getText().trim();
            String value = valueField.getText().trim();
            if (!key.isEmpty()) {
                envVarsList.add(key + "=" + value);
                keyField.clear();
                valueField.clear();
            }
        });

        updateButton.setOnAction(e -> {
            int idx = listView.getSelectionModel().getSelectedIndex();
            if (idx != -1) {
                String key = keyField.getText().trim();
                String value = valueField.getText().trim();
                if (!key.isEmpty()) {
                    envVarsList.set(idx, key + "=" + value);
                }
            }
        });

        deleteButton.setOnAction(e -> {
            int idx = listView.getSelectionModel().getSelectedIndex();
            if (idx != -1) {
                envVarsList.remove(idx);
                keyField.clear();
                valueField.clear();
            }
        });

        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.contains("=")) {
                String[] parts = newVal.split("=", 2);
                keyField.setText(parts[0]);
                valueField.setText(parts.length > 1 ? parts[1] : "");
            }
        });

        saveButton.setOnAction(e -> {
            // Implement your save logic here, e.g., call a SaveEnvVarsCommand or similar
            // For example:
            this.saveCommand.execute(envVarsList);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(mainWindow.getString("success"));
            alert.setHeaderText(null);
            alert.setContentText(mainWindow.getString("success-message"));
            alert.getDialogPane().getStylesheets().clear();
            alert.getDialogPane().getStylesheets().add(
            getClass().getResource(this.theme).toExternalForm()
            );
            alert.showAndWait();
        });

        HBox inputBox = new HBox(5, keyField, valueField, addButton, updateButton, deleteButton, saveButton);
        inputBox.setAlignment(Pos.CENTER_RIGHT); 
        VBox layout = new VBox(10, listView, inputBox, remoteModeLabel);
        layout.setPadding(new Insets(5, 10, 5, 10));
        Stage dialog = new Stage();
        dialog.initOwner(parentStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(mainWindow.getString("header"));
        Scene scene = new Scene(layout, 650, 350);
        scene.getStylesheets().add(getClass().getResource(this.theme).toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }

}