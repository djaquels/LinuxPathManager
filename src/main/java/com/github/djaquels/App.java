package com.github.djaquels;

import com.github.djaquels.ui.Labels;
import com.github.djaquels.utils.GlobalPathCommand;
import com.github.djaquels.utils.GlobalRemotePathCommand;
import com.github.djaquels.utils.PathCommand;
import com.github.djaquels.utils.ReadPathInvoker;
import com.github.djaquels.utils.UserPathCommand;
import com.github.djaquels.utils.UserRemotePathCommand;
import com.github.djaquels.utils.SavePathCommand;
import com.github.djaquels.utils.UserSaveCommand;
import com.github.djaquels.utils.RemoteUserSaveCommand;
import com.github.djaquels.utils.SystemRemoteSaveCommand;
import com.github.djaquels.utils.StringUtils;
import com.github.djaquels.utils.SystemSaveCommand;
import com.github.djaquels.utils.EnvVariablesCommand;
import com.github.djaquels.utils.EnvVariableSaver;
import com.github.djaquels.utils.RemoteEnvVariableSaver;
import com.github.djaquels.utils.RemoteEnvVariablesCommand;
import com.github.djaquels.utils.LanguageUtils;
import com.github.djaquels.utils.PathCommandFactory;

//import com.github.djaquels.utils.savePathCommand;

import java.util.Locale;
import java.util.Optional;

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
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.scene.control.PasswordField;
import javafx.geometry.Insets;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.HashMap;

public class App extends Application {
    private ReadPathInvoker invoker = new ReadPathInvoker();
    private ObservableList<String> userPathList = FXCollections.observableArrayList();
    private ObservableList<String> systemPathList = FXCollections.observableArrayList();
    private SavePathCommand saveUserPathCommand;
    private String userPathAsString;
    private String userPathMD5;
    private String systemPathMD5;
    private Boolean isUserViewActive;
    private Label remoteModeLabel;
    private Boolean remoteModeActive;
    // Remote and local cmds
    private UserPathCommand userPathCommand;
    private UserRemotePathCommand userRemotePathCommand;
    private GlobalPathCommand globalPathCommand;
    private GlobalRemotePathCommand globalRemotePathCommand;
    // Remote creds
    private String remoteHost;
    private String remoteUsername;
    private String remotePassword;
    private int remotePort;

    private void updatePath(PathCommand command, ObservableList<String> pathList) {
        invoker.setCommand(command);
        pathList.setAll(invoker.fetchPath());
    }

    private void updateMD5Signatures(){
        userPathAsString = String.join(":", userPathList);
        userPathMD5 = StringUtils.getMD5(userPathAsString);
	    systemPathMD5 = StringUtils.getMD5(String.join(":", systemPathList));
    }

    private String getLocalLanguage() {
       return LanguageUtils.getLocalLanguage();
    }

    private Labels getWindowConfs() {
       return LanguageUtils.getWindowConfs();
    }

    @Override
    public void start(Stage primaryStage) {
        Labels conf = getWindowConfs();
        String header = conf.getValue("appName");
        JSONObject mainWindow = conf.getWindowLabels("main");
        primaryStage.setTitle(header);
        remoteModeActive = false;
        /* Read Main UI */
        ListView<String> userListView = new ListView<>(userPathList);
        ListView<String> systemListView = new ListView<>(systemPathList);
        Label userPathLabel = new Label(mainWindow.getString("readUsers"));
        Label systemPathLabel = new Label(mainWindow.getString("readSystem"));
        updatePath(new UserPathCommand(), userPathList);
        updatePath(new GlobalPathCommand(), systemPathList);
        userPathAsString = String.join(":", userPathList);
        userPathMD5 = StringUtils.getMD5(userPathAsString);
	    systemPathMD5 = StringUtils.getMD5(String.join(":", systemPathList));
        /*
         * Controll panel buttons
         * Add new to PATH
         * Update/Edit select item in view
         * Save settings
         */
        // View actions
        userListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                handleSelection(true);
            }
        });

        systemListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                handleSelection(false);
            }
        });
        remoteModeLabel = new Label("");
        remoteModeLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        // Add
        TextField pathField = new TextField();
        pathField.setPromptText(mainWindow.getString("add-label"));

        Button addButton = new Button(mainWindow.getString("add"));
        addButton.setOnAction(e -> addPathAction(pathField.getText()));

        // Update
        Button updateButton = new Button(mainWindow.getString("update"));
        updateButton.setOnAction(e -> updateButtonAction(userListView, systemListView, pathField.getText()));

        // Delete

        Button deleButton = new Button(mainWindow.getString("delete"));
        deleButton.setOnAction(e -> deleteButtonAction(userListView, systemListView, false));

        // Env Vars Window
        Button toEnvVars = new Button(mainWindow.getString("to-env"));
        toEnvVars.setOnAction(e -> {
        PathCommand cmd = (remoteModeActive)? new RemoteEnvVariablesCommand(remoteUsername, remoteHost, remotePort, remotePassword) : new EnvVariablesCommand();    
        SavePathCommand saveCMD = (remoteModeActive)? new RemoteEnvVariableSaver(remoteUsername, remoteHost, remotePort, remotePassword): new EnvVariableSaver();
        EnvVars envWindow = new EnvVars(cmd, saveCMD, remoteModeActive);
            envWindow.showWindow(primaryStage);
        });
        // Save
        Button saveButton = new Button(mainWindow.getString("save"));
        saveButton.setOnAction(e -> savePathAction());

        Button sshButton = new Button(mainWindow.getString("ssh"));
        sshButton.setOnAction(e -> openSshDialog(primaryStage));

        // Buttons layout
        HBox buttonBox = new HBox(10); // 10 är mellanrummet mellan knapparna
        buttonBox.getChildren().addAll(addButton, updateButton, deleButton, saveButton, toEnvVars, sshButton);

        VBox layout = new VBox(10);
        layout.getChildren().addAll(userPathLabel, userListView, systemPathLabel, systemListView, remoteModeLabel, pathField, buttonBox);

        Scene scene = new Scene(layout, 750, 350);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void addPathAction(String newPath) {
        // Implementera logik för att uppdatera PATH här
        Labels conf = getWindowConfs();
        JSONObject addWindow = conf.getWindowLabels("add");
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(addWindow.getString("header"));
        alert.setHeaderText(addWindow.getString("label"));
        alert.setContentText(addWindow.getString("action"));

        ButtonType userButton = new ButtonType(addWindow.getString("user-label"));
        ButtonType systemButton = new ButtonType(addWindow.getString("system-label"));
        ButtonType cancelButton = new ButtonType(addWindow.getString("cancel-label"),
                ButtonBar.ButtonData.CANCEL_CLOSE);

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

    private void savePathAction() {
        String onMemoryUserPath = String.join(":", userPathList);
        String onMemoryUserMd5 = StringUtils.getMD5(onMemoryUserPath);
	    String onMemorySystemPath = String.join(":", systemPathList);
	    String onMemorySystemMd5 = StringUtils.getMD5(onMemorySystemPath);
        Labels conf = getWindowConfs();
        String errorHeader = conf.getValue("error");
        String successHeader = conf.getValue("success");
        JSONObject windowLabels = conf.getWindowLabels("save");
        saveUserPathCommand = (remoteModeActive == true)? new RemoteUserSaveCommand(remoteUsername, remoteHost, remotePort, remotePassword): new UserSaveCommand();
        if (!onMemoryUserMd5.equals(userPathMD5)) {
            saveUserPathCommand.execute(userPathList);
            updateMD5Signatures();
            String message = windowLabels.getString("user-saved");
            showSuccessDialog(successHeader, message);
        }
	    if(!onMemorySystemMd5.equals(systemPathMD5)){
            String sudoPassword = promptForSudoPassword();
	    if (sudoPassword != null && !sudoPassword.isEmpty()) {
	       SavePathCommand saveSystemPathCommand = (remoteModeActive == true)? new SystemRemoteSaveCommand(remoteUsername, remoteHost, remotePort, remotePassword): new SystemSaveCommand(sudoPassword);
	       saveSystemPathCommand.execute(systemPathList);
           String message = windowLabels.getString("system-saved");
	       updateMD5Signatures();
           showSuccessDialog(successHeader, message);
	    } else {
           String message = windowLabels.getString("wrong-password");
	       showErrorDialog(errorHeader, message);
	        }
	    }

    }

    private void deleteButtonAction(ListView<String> user, ListView<String> system, Boolean autoConfirmed) {
        ListView<String> activeListView = getCurrentActiveListView(user, system);
        ObservableList<String> activeList = activeListView.getItems();
        int selectedIndex = activeListView.getSelectionModel().getSelectedIndex();
        Labels conf = getWindowConfs();
        JSONObject deleteWindow = conf.getWindowLabels("delete");
        if (selectedIndex != -1) {
            String itemToRemove = activeList.get(selectedIndex);
            if (autoConfirmed || userConfirmed(deleteWindow, itemToRemove)) {
                activeList.remove(selectedIndex);
            }
        } else {
            showErrorDialog(deleteWindow.getString("error-header"), deleteWindow.getString("error-label"));
        }
    }

    private void updateButtonAction(ListView<String> user, ListView<String> system, String newValue) {
        ListView<String> activeListView = getCurrentActiveListView(user, system);
        ObservableList<String> activeList = activeListView.getItems();
        int selectedIndex = activeListView.getSelectionModel().getSelectedIndex();
        Labels conf = getWindowConfs();
        JSONObject updateWindow = conf.getWindowLabels("update");
        if (selectedIndex != -1) {
            String itemToUpdate = activeList.get(selectedIndex);
            if (userConfirmed(updateWindow, itemToUpdate)) {
                deleteButtonAction(user, system, true);
                addPathAction(newValue);
            }
        } else {
            showErrorDialog(updateWindow.getString("error-header"), updateWindow.getString("error-label"));
        }
    }
    
    private String promptForSudoPassword(){
        Labels conf = getWindowConfs();
	JSONObject saveWindow = conf.getWindowLabels("save");
	Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
	alert.setTitle(saveWindow.getString("title"));
	alert.setHeaderText(saveWindow.getString("header"));
	alert.setContentText(saveWindow.getString("sudo-label"));

	PasswordField passwordField = new PasswordField();
	passwordField.setPromptText(saveWindow.getString("password-label"));
	VBox dialogPaneContent = new VBox(10);
	dialogPaneContent.getChildren().add(passwordField);
	alert.getDialogPane().setContent(dialogPaneContent);

	Optional<ButtonType> result = alert.showAndWait();
	if (result.isPresent() && result.get() == ButtonType.OK) {
	    return passwordField.getText();
	}
	return null; // Return null if the user cancels or doesn't provide a password

    }
    private ListView<String> getCurrentActiveListView(ListView<String> user, ListView<String> system) {
        // Anta att vi har en metod eller variabel som håller reda på vilken lista som
        // är aktiv
        return this.isUserViewActive ? user : system;
    }

    private void showErrorDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showSuccessDialog(String title, String content){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private Boolean userConfirmed(JSONObject window, String itemValue){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(window.getString("title"));
        alert.setHeaderText(window.getString("header"));
        alert.setContentText(window.getString("confirm-label") + itemValue);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    private void handleSelection(Boolean value) {
        this.isUserViewActive = value;
    }

    private void openSshDialog(Stage parentStage) {
        Stage dialog = new Stage();
        dialog.initOwner(parentStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("SSH Connection");

        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        Label hostLabel = new Label("Host:");
        TextField hostField = new TextField();
        Label portLabel = new Label("Port:");
        TextField portField = new TextField("22");
        Label passwordLabel = new Label("Password (optional):");
        PasswordField passwordField = new PasswordField();

        Button connectButton = new Button("Connect");
        Button cancelButton = new Button("Cancel");
        Label statusLabel = new Label();

        connectButton.setDefaultButton(true);
        cancelButton.setCancelButton(true);

        connectButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String host = hostField.getText().trim();
            String portStr = portField.getText().trim();
            String password = passwordField.getText();

            if (username.isEmpty() || host.isEmpty() || portStr.isEmpty()) {
                statusLabel.setText("All fields except password are required.");
                return;
            }
            int port;
            try {
                port = Integer.parseInt(portStr);
            } catch (NumberFormatException ex) {
                statusLabel.setText("Port must be a number.");
                return;
            }

            // Try connecting
            PathCommand remoteCmd = PathCommandFactory.createUserRemotePathCommand(username, host, port, password.isEmpty() ? null : password);
            try {
                updatePath(remoteCmd, userPathList);
                updateMD5Signatures();
                remoteModeLabel.setText("Remote mode: " + username + "@" + host + ":" + port);
                remoteHost = host;
                remoteUsername = username;
                remotePort = port;
                remotePassword = password;
                remoteModeActive = true;
                dialog.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                statusLabel.setText("Connection failed. Using local PATH.");
                remoteModeLabel.setText("");
            }

            PathCommand globalRemoteCmd = PathCommandFactory.createGlobalRemotePathCommand(username, host, port, password.isEmpty() ? null : password);
            try {
                updatePath(globalRemoteCmd, systemPathList);
                updateMD5Signatures();
                remoteModeLabel.setText("Remote mode: " + username + "@" + host + ":" + port);
                dialog.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                statusLabel.setText("Connection failed. Using system PATH.");
                remoteModeLabel.setText("");
            }
        });

        cancelButton.setOnAction(e -> dialog.close());

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(16));
        grid.add(usernameLabel, 0, 0); grid.add(usernameField, 1, 0);
        grid.add(hostLabel, 0, 1); grid.add(hostField, 1, 1);
        grid.add(portLabel, 0, 2); grid.add(portField, 1, 2);
        grid.add(passwordLabel, 0, 3); grid.add(passwordField, 1, 3);
        HBox buttons = new HBox(10, connectButton, cancelButton);
        grid.add(buttons, 1, 4);
        grid.add(statusLabel, 1, 5);

        Scene dialogScene = new Scene(grid, 350, 250);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
