package view.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.enums.PlayerColor;
import model.player.PlayerView;
import model.utility.MapInfoView;
import network.ClientContext;
import network.ClientController;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import static javafx.geometry.HPos.*;

class ChooseGameScene extends Scene {

    private static final String REMOTE_EXCEPTION = "Remote Exception";
    private final GUIViewJavaFX view;
    private final GridPane gridPane;
    private final ClientController clientController;
    private final TextArea actiontarget;

    private int newButtonPressed = 0;
    private ChoiceBox endChoiceBox;
    private ChoiceBox mapChoiceBox;
    private boolean mapSceneOpened;

    ChooseGameScene(GridPane gridPane, double width, double height, GUIViewJavaFX view, ClientController clientController) {
        super(gridPane, width, height);
        this.gridPane = gridPane;
        this.view = view;
        this.clientController = clientController;
        createScene();
        actiontarget = new TextArea();
        actiontarget.setEditable(false);
        actiontarget.setWrapText(true);
        actiontarget.setOpacity(0.7f);
        actiontarget.setMinWidth(640);
        actiontarget.setMinHeight(100);
        gridPane.add(actiontarget, 0, 6);
        GridPane.setColumnSpan(actiontarget, 2);
        GridPane.setHalignment(actiontarget, CENTER);
        actiontarget.setId("actiontarget");
    }

    GridPane getGridPane() {
        return gridPane;
    }

    TextArea getActiontarget() {
        return actiontarget;
    }

    /**
     * Create the scene of the game selection adding "new game" and "join game" functions, moreover the user can see the maps that can be selected by pressing "show maps" function
     */
    private void createScene() {
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(100, 100, 100, 100));

        clientController.giveGamesStatus();
        clientController.getLobbiesStatus();

        Label title = new Label("Game selection");
        gridPane.add(title, 0, 0);

        ListView<String> lobbyList = new ListView<>();
        lobbyList.setItems(giveLobbies());
        lobbyList.setMinHeight(250);
        lobbyList.setMinWidth(640);
        if(!lobbyList.getItems().isEmpty()) {
            lobbyList.getSelectionModel().select(0);
        }

        if(lobbyList.getItems().size() == 1 && lobbyList.getItems().get(0).equals("No game to join found")) {
            lobbyList.setDisable(true);
        }

        gridPane.add(lobbyList, 0, 1);

        Button newButton = new Button("New");
        gridPane.add(newButton, 0, 2);
        GridPane.setHalignment(newButton, LEFT);

        Button joinButton = new Button("Join");
        gridPane.add(joinButton, 0, 2);
        GridPane.setHalignment(joinButton, RIGHT);
        if(! ClientContext.get().isValidJoin()) {
            joinButton.setDisable(true);
        }

        Button showMapButton = new Button("Show maps");
        gridPane.add(showMapButton, 0, 0);
        GridPane.setHalignment(showMapButton, RIGHT);

        showMapButton.setOnAction(e -> {
            if(!mapSceneOpened) {
                mapSceneOpened = true;
                Stage mapStage = new Stage();
                String url = getClass().getResource("/images/all_maps_num.png").toExternalForm();
                ImageView mapImageView = new ImageView(new Image(url));
                mapImageView.setPreserveRatio(true);
                mapImageView.setFitHeight(575);
                HBox hBoxMap = new HBox();
                hBoxMap.getChildren().add(mapImageView);
                Scene mapScene = new Scene(hBoxMap);
                mapStage.setScene(mapScene);
                mapStage.setWidth(750);
                mapStage.setHeight(600);
                mapStage.setResizable(false);
                mapStage.setOnCloseRequest(event -> {
                    mapSceneOpened = false;
                });
                mapStage.show();
            }
        });


        newButton.setOnAction(e -> {
            if(newButtonPressed == 0) {
                chooseNewGame();
                newButtonPressed ++;
                newButton.setText("Create");
                gridPane.getChildren().remove(joinButton);
            }
            else {
                try {
                    clientController.getMapInfo(-1, mapChoiceBox.getSelectionModel().getSelectedIndex() + 1);
                    clientController.createGame(mapChoiceBox.getSelectionModel().getSelectedIndex() + 1, endChoiceBox.getSelectionModel().getSelectedItem().toString());
                    chooseUsername(ClientContext.get().getValidGame());
                    mapChoiceBox.setDisable(true);
                    endChoiceBox.setDisable(true);
                    newButton.setDisable(true);
                } catch (RemoteException ex) {
                    actiontarget.appendText(REMOTE_EXCEPTION);
                }
            }
        });

        joinButton.setOnAction(e -> {
            try {
                int selectedGame = ClientContext.get().getPossibleGames().get(0).getActualGameId() + lobbyList.getSelectionModel().getSelectedIndex();
                chooseUsername(selectedGame);
                joinButton.setDisable(true);
                newButton.setDisable(true);
            } catch (RemoteException ex) {
                actiontarget.appendText(REMOTE_EXCEPTION);
            }
        });
    }

    /**
     * Method to insert all the possible games to join in a List View element.
     * @return the elements that will populate the list view
     */
    private ObservableList giveLobbies() {
        ObservableList lobbies = FXCollections.observableArrayList();
        List<MapInfoView> maps = ClientContext.get().getPossibleGames();

        StringBuilder stringBuilder = new StringBuilder();

        for (MapInfoView m : maps) {
            stringBuilder.append("Game [");
            stringBuilder.append(m.getActualGameId());
            stringBuilder.append("] available\tMap number ");
            stringBuilder.append(m.getNumber());
            stringBuilder.append(" selected  with end mode '");
            stringBuilder.append(m.getActualEndMode());
            stringBuilder.append("'. ");
            stringBuilder.append(m.getPlayerViews().size());
            stringBuilder.append(" out of ");
            stringBuilder.append(m.getMaxNumberOfPlayer());
            stringBuilder.append(" ( min ");
            stringBuilder.append(m.getMinNumberOfPlayer());
            stringBuilder.append(" to begin )");
            stringBuilder.append(System.getProperty("line.separator"));
            for(PlayerView p : m.getPlayerViews()) {
                stringBuilder.append("\t\t\t\tPlayer: ");
                stringBuilder.append(p.getPlayerID());
                stringBuilder.append(" ( Color: ");
                stringBuilder.append(p.getPlayerColor().name());
                stringBuilder.append(" )");
                if(m.getPlayerViews().indexOf(p) != m.getPlayerViews().size() - 1) {
                    stringBuilder.append(System.getProperty("line.separator"));
                }
            }
            lobbies.add(stringBuilder.toString());
            stringBuilder = new StringBuilder();
        }

        if(lobbies.isEmpty()) {
            lobbies.add("No game to join found");
        }

        return lobbies;
    }

    /**
     * Display the information to create a new game, so the maps available and the end modes
     */
    private void chooseNewGame() {
        int nOfMap = ClientContext.get().getNumberOfMaps();
        List<String> mapsChoices = new ArrayList<>();
        for(int i = 0; i < nOfMap; i++) {
            mapsChoices.add("Map " + (i+1));
        }

        endChoiceBox = new ChoiceBox(FXCollections.observableArrayList(ClientContext.get().getPossibleMaps().get(0).getAllowedEndModes()));
        endChoiceBox.getSelectionModel().select(0);
        endChoiceBox.setMinWidth(100);
        gridPane.add(endChoiceBox, 0, 2);
        GridPane.setHalignment(endChoiceBox, RIGHT);

        mapChoiceBox = new ChoiceBox(FXCollections.observableArrayList(mapsChoices));
        mapChoiceBox.getSelectionModel().select(0);
        mapChoiceBox.setOnAction(e -> {
            int index = mapChoiceBox.getSelectionModel().getSelectedIndex();
            List<String> updateModes = ClientContext.get().getPossibleMaps().get(index).getAllowedEndModes();
            endChoiceBox.setItems(FXCollections.observableArrayList(updateModes));
            endChoiceBox.getSelectionModel().select(0);
        });
        mapChoiceBox.setMinWidth(100);
        gridPane.add(mapChoiceBox, 0, 2);
        GridPane.setHalignment(mapChoiceBox, CENTER);
    }

    /**
     * As is CLI view create a player with the given username and color. The choice is validated by server and a message is shown.
     * @param givenId the id of the game to join
     * @throws RemoteException
     */
    private void chooseUsername (int givenId) throws RemoteException {
        clientController.getMapInfo(givenId, -1);
        MapInfoView selectedMap = ClientContext.get().getMap();
        if(selectedMap == null) {
            view.ack("MAP IS NULL");
            return;
        }

        Label usernameLabel = new Label("Username: ");
        gridPane.add(usernameLabel, 0, 3);
        GridPane.setHalignment(usernameLabel, LEFT);
        TextField usernameTextField = new TextField();
        usernameTextField.setMinWidth(100);
        usernameTextField.setMaxWidth(100);
        gridPane.add(usernameTextField, 0, 3);
        GridPane.setHalignment(usernameTextField, RIGHT);

        Label colorLabel = new Label("Color: ");
        gridPane.add(colorLabel, 0, 4);
        GridPane.setHalignment(colorLabel, LEFT);
        ChoiceBox colorChoiceBox = new ChoiceBox(FXCollections.observableArrayList(selectedMap.getAllowedPlayerColors()));
        colorChoiceBox.getSelectionModel().select(0);
        colorChoiceBox.setMinWidth(100);
        gridPane.add(colorChoiceBox, 0, 4);
        GridPane.setHalignment(colorChoiceBox, RIGHT);

        Button createPlayerButton = new Button("Create player");
        gridPane.add(createPlayerButton, 0, 5);

        createPlayerButton.setOnAction(e -> {
            String username = usernameTextField.getText();
            PlayerColor color = PlayerColor.valueOf(colorChoiceBox.getSelectionModel().getSelectedItem().toString());
            if(username.isEmpty()) {
                view.ack("Insert a valid username");
            }
            else {
                clientController.createPlayer(givenId, username, color);
            }
            if(ClientContext.get().getCurrentPlayer() != null) {
                createPlayerButton.setDisable(true);
                colorChoiceBox.setDisable(true);
                usernameTextField.setDisable(true);
                try {
                    view.startTutorial(true);
                } catch (IOException ex) {
                    actiontarget.appendText(REMOTE_EXCEPTION);
                }
            }
        });
    }
}
