package view.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import model.player.PlayerView;
import model.powerup.PowerUp;
import model.weapon.Weapon;
import network.*;
import view.ViewClient;
import view.cli.CommandParsing;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

import static javafx.geometry.HPos.CENTER;

public class GUIViewJavaFX extends Application implements ViewClient {

    //controller and other stuffs
    private ClientController clientController;
    private CommandParsing commandParsing;
    private GUIListener guiListener;
    private String token;
    private boolean finalFrenzy;
    private boolean scoreBoardOpen;

    private Stage mainStage;
    private PlayingScene playingScene;
    private GridPane gridPaneLogin;
    private GridPane grid;
    private TextArea actiontarget;

    private static final String REMOTE_EXCEPTION = "Remote Exception";
    private static final String CARD_PATH = "/images/cards/";

    private List<PlayerView> endRank = new ArrayList<>();
    private List<Integer> pointsList = new ArrayList<>();
    private int startingPointsSize = -1;


    public void runGUIView(String[] args) {
        launch(args);
    }

    /**
     * Start method to run the main java fx thread, it creates the begin windows, with the selection of the type of connection,
     * @param primaryStage the main stage to which display graphic
     */
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Adrenaline");

        actiontarget = new TextArea();
        actiontarget.setEditable(false);
        actiontarget.setWrapText(true);
        actiontarget.setOpacity(0.7f);
        actiontarget.setMinWidth(480);

        grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(100, 100, 100, 100));

        Text scenetitle = new Text("Welcome to ADRENALINE");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 32));
        grid.add(scenetitle, 0, 0, 2, 1);

        Label connectionLabel = new Label("Type of connection: ");
        grid.add(connectionLabel, 0, 1);
        ChoiceBox connectionChoiceBox = new ChoiceBox(FXCollections.observableArrayList("rmi", new Separator(), "socket"));
        connectionChoiceBox.getSelectionModel().select(0);
        connectionChoiceBox.setTooltip(new Tooltip("Select the type of connection"));
        grid.add(connectionChoiceBox, 1, 1);

        Button submitConnection = new Button("Submit");
        HBox hBoxSumbit = new HBox(50);
        hBoxSumbit.setAlignment(Pos.BOTTOM_RIGHT);
        hBoxSumbit.getChildren().add(submitConnection);
        submitConnection.setTooltip(new Tooltip("Submit your connection choice"));
        grid.add(hBoxSumbit, 1, 4);

        submitConnection.setOnAction(e -> {
            grid.getChildren().remove(hBoxSumbit);
            if(connectionChoiceBox.getSelectionModel().getSelectedItem().toString().equals("socket")) {
                connectionLabel.setText("Socket connection: ");
                grid.getChildren().remove(connectionChoiceBox);
                chooseSocket();
            }
            else {
                connectionLabel.setText("RMI connection: ");
                grid.getChildren().remove(connectionChoiceBox);
                chooseRmi();
            }
        });

        Scene scene = new Scene(grid, 640, 350);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        mainStage = primaryStage;
        primaryStage.show();
    }

    /**
     * Create the window for socket with ip and port text field
     */
    private void chooseSocket() {
        Label labelIP = new Label("IP: ");
        HBox labelHBox = new HBox(50);
        TextField ipTextField = new TextField();
        ipTextField.setText("127.0.0.1");
        labelHBox.setAlignment(Pos.BOTTOM_RIGHT);
        labelHBox.getChildren().add(labelIP);
        labelHBox.getChildren().add(ipTextField);
        grid.add(labelHBox, 1, 3);

        Label labelPort = new Label("Port: ");
        TextField portTextField = new TextField();
        portTextField.setText("7200");
        HBox portHBox = new HBox(50);
        portHBox.setAlignment(Pos.BOTTOM_RIGHT);
        portHBox.getChildren().add(labelPort);
        portHBox.getChildren().add(portTextField);
        grid.add(portHBox, 1, 4);

        Button connectButton = new Button("Connect");
        HBox connectHBox = new HBox(50);
        connectHBox.setAlignment(Pos.BOTTOM_RIGHT);
        connectHBox.getChildren().add(connectButton);
        connectButton.setTooltip(new Tooltip("Try to connect with the selected parameters"));
        grid.add(connectHBox, 1, 5);

        grid.add(actiontarget, 0, 6);
        GridPane.setColumnSpan(actiontarget, 2);
        GridPane.setHalignment(actiontarget, CENTER);
        actiontarget.setId("actiontarget");

        connectButton.setOnAction(e -> {
            try{
                connectSocket(ipTextField.getText(), portTextField.getText());
                if(clientController != null) {
                    connectButton.setDisable(true);
                    clientController.run();
                }
            }
            catch (IOException ex) {
                actiontarget.appendText(REMOTE_EXCEPTION);
                actiontarget.appendText(System.getProperty("line.separator"));
            }
        });

    }

    /**
     * Given the parameters of the socket connection instantiate a new ClientScoket object with the associated listener and a command parser
     * @param ip the ip to which connect
     * @param textPort the porto to which connect
     * @throws IOException if server is not reachable
     */
    private void connectSocket(String ip, String textPort) throws IOException{
        if(ip.isEmpty() || textPort.isEmpty()) {
            actiontarget.appendText("Insert valid parameters");
            return;
        }
        int port;
        try {
            port = Integer.parseInt(textPort);

            ClientSocket clientSocket = new ClientSocket(ip, port);
            clientSocket.setViewClient(this);
            clientSocket.init();

            this.clientController = new ClientController(clientSocket, this);
            this.guiListener = new GUIListener(this, clientController);
            this.commandParsing = new CommandParsing(clientController);
            clientSocket.setClientController(this.clientController);
            ResponseDecoder responseDecoder = new ResponseDecoder(clientSocket, guiListener);
            clientSocket.setResponseDecoder(responseDecoder);
            actiontarget.appendText("Connecting");
            actiontarget.appendText(System.getProperty("line.separator"));
        } catch (NumberFormatException e) {
            actiontarget.appendText("Insert a valid port");
            actiontarget.appendText(System.getProperty("line.separator"));
        }
    }

    /**
     * Create the window for rmi with ip text field
     */
    private void chooseRmi(){
        Label labelIP = new Label("IP: ");
        HBox labelHBox = new HBox(50);
        TextField ipTextField = new TextField();
        ipTextField.setText("127.0.0.1");
        labelHBox.setAlignment(Pos.BOTTOM_RIGHT);
        labelHBox.getChildren().add(labelIP);
        labelHBox.getChildren().add(ipTextField);
        grid.add(labelHBox, 1, 3);

        Button connectButton = new Button("Connect");
        HBox connectHBox = new HBox(50);
        connectHBox.setAlignment(Pos.BOTTOM_RIGHT);
        connectHBox.getChildren().add(connectButton);
        connectButton.setTooltip(new Tooltip("Try to connect with the selected parameters"));
        grid.add(connectHBox, 1, 5);

        grid.add(actiontarget, 0, 6);
        GridPane.setColumnSpan(actiontarget, 2);
        GridPane.setHalignment(actiontarget, CENTER);
        actiontarget.setId("actiontarget");

        connectButton.setOnAction(e -> {
            try{
                connectRmi(ipTextField.getText());
                if(clientController != null) {
                    connectButton.setDisable(true);
                    clientController.run();
                }
            }
            catch (IOException | NotBoundException ex) {
                actiontarget.appendText(REMOTE_EXCEPTION);
                actiontarget.appendText(System.getProperty("line.separator"));
            }
        });
    }

    /**
     * Given the parameters of the rmi connection instantiate a new ClientRmi object with the associated listener and a command parser
     * @param ip the ip to which connect
     * @throws IOException if server is not reachable
     * @throws NotBoundException
     */
    private void connectRmi(String ip) throws IOException, NotBoundException {
        if(ip.isEmpty()) {
            actiontarget.appendText("Insert valid parameters");
            return;
        }
        Registry registry = LocateRegistry.getRegistry(ip, 1200);
        RemoteController remoteController = (RemoteController) registry.lookup("controller");

        ClientRmi clientRmi = new ClientRmi(remoteController);
        clientRmi.setViewClient(this);

        this.clientController = new ClientController(clientRmi, this);
        this.guiListener = new GUIListener(this, clientController);
        this.commandParsing = new CommandParsing(clientController);

        actiontarget.appendText("Connecting");
        actiontarget.appendText(System.getProperty("line.separator"));
    }

    @Override
    public ViewListener getListener() throws RemoteException {
        return guiListener;
    }

    /**
     * Create a new Choose Game scene in which the user is instructed to create a new game or join one
     * @throws RemoteException
     */
    @Override
    public void chooseGame() throws RemoteException {
        ChooseGameScene chooseGameScene = new ChooseGameScene(new GridPane(), 800, 600, this, clientController);
        grid = chooseGameScene.getGridPane();
        actiontarget = chooseGameScene.getActiontarget();
        mainStage.setScene(chooseGameScene);
    }

    /**
     * Create a tutorial scene in which all the commands of the gui are explained in an image. This screen can be re called during the game by pressing H
     * @param firstTime boolean representing if this is the first time that the tutorial is shown. If so after the closure the game is started
     * @throws RemoteException
     */
    void startTutorial(boolean firstTime) throws RemoteException{
        int width = (int) Screen.getScreens().get(0).getBounds().getWidth();
        int height = (int) Screen.getScreens().get(0).getBounds().getHeight();
        Stage mapStage = new Stage();
        String url = getClass().getResource("/images/tutorial.png").toExternalForm();
        ImageView mapImageView = new ImageView(new Image(url));
        mapImageView.setPreserveRatio(true);
        mapImageView.setFitHeight(height);
        HBox hBoxMap = new HBox();
        hBoxMap.getChildren().add(mapImageView);
        hBoxMap.setAlignment(Pos.CENTER);
        hBoxMap.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        Scene mapScene = new Scene(hBoxMap);
        mapStage.setScene(mapScene);
        mapStage.setMinWidth(width);
        mapStage.setMaxWidth(width);
        mapStage.setMinHeight(height);
        mapStage.setMaxHeight(height);
        mapStage.setFullScreen(true);
        mapStage.setResizable(false);

        if(firstTime) {
            mapImageView.setOnMouseClicked(e -> {
                try {
                    playing();
                    mapStage.close();
                } catch (IOException ex) {
                    System.err.println("Error in tutorial phase");
                }
            });

            mapStage.setOnCloseRequest(event -> {
                try {
                    playing();
                    mapStage.close();
                } catch (IOException e) {
                    System.err.println("Error in tutorial phase");
                }
            });
        }
        else {
            mapImageView.setOnMouseClicked(e -> {
                if(playingScene != null) {
                    playingScene.sethPressed(false);
                    mapStage.close();
                }
            });

            mapStage.setOnCloseRequest(event -> {
                if(playingScene != null) {
                    playingScene.sethPressed(false);
                    mapStage.close();
                }
            });
        }
        mapStage.show();
    }

    /**
     * Create the main playing scene
     * @throws RemoteException
     */
    @Override
    public void playing() throws RemoteException {
        int width = (int)Screen.getScreens().get(0).getBounds().getWidth();
        int height = (int)Screen.getScreens().get(0).getBounds().getHeight();
        playingScene = new PlayingScene(new VBox(0), width, height, this);
        mainStage.setScene(playingScene);
        mainStage.setResizable(true);
        mainStage.setFullScreen(true);
        mainStage.setMinWidth(width);
        mainStage.setMaxWidth(width);
        mainStage.setMaxHeight(height);
        mainStage.setMinHeight(height);
        mainStage.setOnCloseRequest(e -> {
            if(ClientContext.get().isDisconnected()) {
                System.exit(0);
            }
            else {
                clientController.quit();
            }
        });

        if(playingScene != null && playingScene.getGuiLogger() != null) {
            playingScene.getGuiLogger().lookup(".viewport").setStyle("-fx-background-color: transparent;");
            playingScene.getGuiLogger().setStyle("-fx-background-color: transparent;");
        }

        playAudio("/sounds/join.wav");
    }

    void playAudio(String path) {
        String url = getClass().getResource(path).toExternalForm();
        AudioClip audioClip = new AudioClip(url);
        audioClip.play();
    }

    /**
     * Used when a client lose connection to redraw everything
     */
    private void redrawPlayingScene() {
        int width = (int)Screen.getScreens().get(0).getBounds().getWidth();
        int height = (int)Screen.getScreens().get(0).getBounds().getHeight();
        boolean fullScreen = mainStage.isFullScreen();
        playingScene = new PlayingScene(new VBox(0), width, height, this);
        mainStage.setScene(playingScene);
        mainStage.setResizable(true);
        mainStage.setFullScreen(fullScreen);
        mainStage.setMinWidth(width);
        mainStage.setMaxWidth(width);
        mainStage.setMaxHeight(height);
        mainStage.setMinHeight(height);
        if(playingScene != null && playingScene.getGuiLogger() != null) {
            playingScene.getGuiLogger().lookup(".viewport").setStyle("-fx-background-color: transparent;");
            playingScene.getGuiLogger().setStyle("-fx-background-color: transparent;");
        }
    }


    @Override
    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public void ack(String content) {
        actiontarget.appendText(content + System.getProperty("line.separator"));
        if(playingScene != null && playingScene.getGuiLogger() != null) {
            playingScene.getGuiLogger().addText(content);
        }
    }

    @Override
    public void errorAck(String content) {
        System.err.println(content);
    }

    ClientController getClientController() {
        return clientController;
    }

    CommandParsing getCommandParsing() {
        return commandParsing;
    }

    @Override
    public void refresh() throws RemoteException{
        Platform.runLater(
                new Runnable() {
                    @Override
                    public void run() {
                        if(ClientContext.get().isGameStarted()) {
                            redrawPlayingScene();
                        }
                    }
                }
        );
    }

    Stage getMainStage() {
        return mainStage;
    }

    List<String> getWeaponsListImages(List<Weapon> weaponsList) {
        List<String> res = new ArrayList<>();
        for(Weapon w : weaponsList) {
            res.add(getWeaponImage(w));
        }
        return res;
    }

    private String getWeaponImage(Weapon weapon) {
        return CARD_PATH + weapon.getIdName() + ".png";
    }

    List<String> getPowsListImages(List<PowerUp> powsList) {
        List<String> res = new ArrayList<>();
        for(PowerUp p : powsList) {
            res.add(getPowImage(p));
        }
        return res;
    }

    private String getPowImage(PowerUp powerUp) {
        return CARD_PATH + powerUp.getColor() + "_" + powerUp.getName() + ".png";
    }

    @Override
    public void setFinalFrenzy(boolean finalFrenzy) {
        this.finalFrenzy = finalFrenzy;
    }

    @Override
    public boolean isFinalFrenzy() {
        return finalFrenzy;
    }

    /**
     * This method is used to update a single or every pane in the gui sceen cause of a notify send by the server. Not used to update the logger
     * @param paneToUpdate the part of the screen to update, ALL if all the screen has to be updated
     */
    public void onUpdate(PaneToUpdate paneToUpdate) {
        Platform.runLater(
                new Runnable ( ) {
                    @Override
                    public void run() {
                        if(playingScene == null) {
                            return;
                        }
                        switch (paneToUpdate){
                            case WEAPONS:
                                playingScene.updateWeaponsPane();
                                if(playingScene.zoomPane.btnWeapon!=null){
                                    if(!ClientContext.get().getWeapons().contains(playingScene.zoomPane.btnWeapon.weapon)){
                                        playingScene.zoomPane.reset();
                                    }
                                }
                                break;
                            case POWERUPS:
                                playingScene.updatePowerupsPane();
                                if(playingScene.zoomPane.btnPowerUp!=null){
                                    if(!ClientContext.get().getPowerUps().contains(playingScene.zoomPane.btnPowerUp.powerUp)){
                                        playingScene.zoomPane.reset();
                                    }
                                }
                                break;
                            case PLAYER:
                                playingScene.updatePlayerPane();
                                playingScene.updatePlayerPointsPane();
                                break;
                            case GAMEBOARD:
                                playingScene.updateGameBoardPane();
                                break;
                            case ENEMYPLAYER:
                                playingScene.updateEnemyCardsPane();
                                playingScene.updateEnemyPointsPane();
                                playingScene.updateEnemyPlayerPane();
                                break;
                            case LOGGER:
                                break;
                            case ALL:
                                playingScene.updateWeaponsPane();
                                playingScene.updatePowerupsPane();
                                playingScene.updatePlayerPane();
                                playingScene.updateGameBoardPane();
                                playingScene.updateEnemyPlayerPane();
                                break;
                            default:
                        }
                    }
                }
        );
    }

    /**
     * Used to update only the logger
     * @param paneToUpdate the logger pane to update
     * @param string the string to add to the logger
     */
    void onUpdate(PaneToUpdate paneToUpdate, String string) {
        Platform.runLater(
                    new Runnable ( ) {
                        @Override
                        public void run() {
                            switch (paneToUpdate){
                                case LOGGER:
                                    if(playingScene!=null)
                                        playingScene.guiLogger.addText(string);
                                    break;
                            }
                        }
                    }
            );
    }

    /**
     * Function used to zoom in the corresponding area a weapon or a powerup card by pressing it with right mouse button
     * @param btnWeapon the weapon pressed to zoom
     */
    void onZoom(BtnWeapon btnWeapon) {
        Platform.runLater(
                new Runnable ( ) {
                    @Override
                    public void run() {
                        if(btnWeapon!= null){
                            if(playingScene.zoomPane.btnWeapon==null){
                                playingScene.zoomPane.addWeapon(btnWeapon);
                            }
                            else if(!btnWeapon.weapon.getIdName().equals(playingScene.zoomPane.btnWeapon.weapon.getIdName())){
                                playingScene.zoomPane.addWeapon(btnWeapon);
                            }
                            else {
                                playingScene.zoomPane.reset();
                            }
                        }
                    }
                }
        );
    }

    /**
     * Function used to zoom in the corresponding area a weapon or a powerup card by pressing it with right mouse button
     * @param btnPowerUp the powerup pressed to zoom
     */
    void onZoom(BtnPowerUp btnPowerUp) {
        Platform.runLater(
                new Runnable ( ) {
                    @Override
                    public void run() {
                        if(btnPowerUp!= null){
                            if(playingScene.zoomPane.btnPowerUp==null){
                                playingScene.zoomPane.addPowerUp(btnPowerUp);
                            }
                            else if(btnPowerUp.powerUp.getId()!= playingScene.zoomPane.btnPowerUp.powerUp.getId()){
                                playingScene.zoomPane.addPowerUp(btnPowerUp);
                            }
                            else {
                                playingScene.zoomPane.reset();
                            }
                        }
                    }
                }
        );
    }

    /**
     * Create the window in which the user can reconnect himself after he has been disconnected cause of inactivity
     */
    void createInactiveWindow() {
        Platform.runLater(
                () -> {
                    int width = 400;
                    int height = 400;
                    InactiveScene inactiveScene = new InactiveScene(new StackPane(), width, height, this);
                    mainStage.setMinWidth(width);
                    mainStage.setMinHeight(height);
                    mainStage.setMaxWidth(width);
                    mainStage.setMinHeight(height);
                    mainStage.setFullScreen(false);
                    mainStage.setResizable(false);
                    mainStage.setScene(inactiveScene);
                }
        );
    }

    PlayingScene getPlayingScene() {
        return playingScene;
    }

    TextArea getActiontarget() {
        return actiontarget;
    }

    /**
     * Create the window in which is displayed the leaderbord
     */
    void createScoreBoard() {
        Platform.runLater(() -> {
            if(!scoreBoardOpen) {
                Stage stage = new Stage();
                HBox hBox = new HBox();
                double width = 750.0f;
                double height = 600.0f;
                Scene mapScene = new ScoreBoardScene(hBox, this, width, height);
                stage.setScene(mapScene);
                stage.setWidth(width);
                stage.setHeight(height);
                stage.setResizable(false);
                stage.setTitle("Scoreboard");


                stage.setOnCloseRequest(e -> {
                    scoreBoardOpen = false;
                    clientController.close();
                });
                scoreBoardOpen = true;
                stage.show();
                mainStage.close();
            }
        });
    }

    void setEndRank(List<PlayerView> endRank) {
        this.endRank = endRank;
    }

    void setPointsList(List<Integer> points) {
        this.pointsList = points;
    }

    List<PlayerView> getEndRank() {
        return endRank;
    }

    List<Integer> getPointsList() {
        return pointsList;
    }

    int getStartingPointsSize() {
        return this.startingPointsSize;
    }

    void setStartingPointsSize(int startingPointsSize) {
        this.startingPointsSize = startingPointsSize;
    }

    @Override
    public void denyMove() {
        Platform.runLater(() -> playAudio("/sounds/denied.wav"));
    }
}
