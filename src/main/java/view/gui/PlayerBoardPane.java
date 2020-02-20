package view.gui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TextField;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.enums.PlayerColor;
import model.enums.PlayerState;
import model.enums.TypeOfAction;
import model.player.PlayerView;
import network.ClientContext;
import view.cli.CommandParsing;



class PlayerBoardPane extends HBox {
    private final GUIViewJavaFX view;
    private  PlayerView playerView;
    private PlayerMarksPane playerMarksPane;
    private PlayerDamagePane playerDamagePane;
    private PlayerAmmoPane playerAmmoPane;
    private VBox vBoxNameAndNothing;
    private TextField textField;
    private Button empty = new Button("empty");
    private Button run = new Button("run");
    private Button grab = new Button("grab");
    private Button shoot = new Button("shoot");
    private Button frenzyEmpty = new Button("empty");
    private Button frenzyShoot = new Button("shoot");
    private Button frenzyGrab = new Button("grab");
    private Button nothingButton = new Button("nothing");

    PlayerBoardPane(PlayerView playerView, double width, GUIViewJavaFX viewJavaFX) {
        super();
        this.view = viewJavaFX;
        this.playerView = playerView;
        double height = width *270/1120;
        setPrefSize(width, height);

        if(playerView!=null){
            try{
                final String string = getImageFromColor(playerView.getPlayerColor());
                String url = getClass().getResource(string).toExternalForm();
                Image mapImage = new Image(url);
                BackgroundSize backgroundSize = new BackgroundSize(width, height, false, false, false, false);
                BackgroundImage backgroundImage = new BackgroundImage(mapImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, backgroundSize);
                Background background = new Background(backgroundImage);
                this.setBackground(background);
            }
            catch (Exception e){
                //Dont set the background
                viewJavaFX.ack("Exception on setting background");
            }
        }

        //set the action (run grab shoot) buttons
        setActionButtons(width, height);
        if(playerView != null && playerView.getPlayerColor() != null) {
            setEffect(new DropShadow(10, getColorFromPlayer(playerView.getPlayerColor())));
        }
        setCentralPane(width, height);


        final boolean areYou = playerView != null && playerView.getPlayerColor() == ClientContext.get().getCurrentPlayer().getPlayerColor();

        vBoxNameAndNothing = new VBox();
        vBoxNameAndNothing.setPrefSize(width/5, height);
        vBoxNameAndNothing.setMinSize(width/5, height);
        vBoxNameAndNothing.setMaxSize(width/5, height);
        setSizeButton(nothingButton, width/5, height*4/5);
        textField = (playerView != null && playerView.getPlayerID() != null) ? new TextField(playerView.getPlayerID()) : new TextField("");
        vBoxNameAndNothing.setPrefSize(width/5, height/5);
        vBoxNameAndNothing.setMinSize(width/5, height/5);
        vBoxNameAndNothing.setMaxSize(width/5, height/5);
        textField.setAlignment(Pos.CENTER);
        textField.setFont(Font.font("Verdana", FontWeight.BOLD, 20.0*height/270.0));
        textField.setStyle("-fx-text-inner-color: white; -fx-border-color: transparent; -fx-border-width: 0; -fx-background-radius: 0; -fx-background-color: transparent;");
        textField.setEditable(false);
        setButtonProperties(nothingButton);
        nothingButton.setOnAction(e -> {
            if(areYou) {
                view.getCommandParsing().initExecutionCommand(CommandParsing.getNothingCommand());
            }
            else {

                int choice = ClientContext.get().indexOfSelectableTargets(playerView);
                if(choice != -1) {
                    view.getCommandParsing().initExecutionCommand(CommandParsing.getChooseTargetCommand() + " " + choice);
                }
                else {
                    viewJavaFX.playAudio("/sounds/denied.wav");
                    view.ack("Cannot do the target action");
                }
            }
        });
        vBoxNameAndNothing.getChildren().add(0, textField);
        vBoxNameAndNothing.getChildren().add(1, nothingButton);

        VoidSpaceButton voidSpaceButton = new VoidSpaceButton("", width*53.0/1120.0, height, viewJavaFX);
        getChildren().add(2, voidSpaceButton);
        getChildren().add(3, vBoxNameAndNothing);
    }

    private void setCentralPane(double width, double height) {
        VBox centralSpace = new VBox();
        double centralSpaceRatio = (852.0-97.0)/1120;
        if(view.isFinalFrenzy()) {
            if(!(playerView.getBoard().getPoints().size() == view.getStartingPointsSize())){
                centralSpaceRatio = (852.0-113.0)/1120;
            }
        }
        centralSpace.setMinSize(width*centralSpaceRatio, height);
        centralSpace.setMaxSize(width*centralSpaceRatio, height);

        double overDamagePaneRatio = 92.0/270;
        double damagePaneRatio = (177.0-92.0)/270;
        HBox hBoxTop = OverDamagePaneSpace(centralSpace.getMinWidth(), centralSpace.getMinHeight()*overDamagePaneRatio);
        playerDamagePane = new PlayerDamagePane(playerView,  centralSpace.getMinWidth(), centralSpace.getMinHeight()*damagePaneRatio, view);
        HBox hBoxBottom = UnderDamagePaneSpace(centralSpace.getMinWidth(), centralSpace.getMinHeight()*(1.0-overDamagePaneRatio-damagePaneRatio));

        centralSpace.getChildren().add(0, hBoxTop);
        centralSpace.getChildren().add(1, playerDamagePane);
        centralSpace.getChildren().add(2, hBoxBottom);
        getChildren().add(1, centralSpace);
    }

    private HBox OverDamagePaneSpace(double width, double height) {
        HBox hBox = new HBox();
        hBox.setMinSize(width, height);
        hBox.setMaxSize(width, height);
        double firstEmptySpaceRatio = (543.0-97.0)/(852.0-97.0);
        double marksPaneWidthRatio = (852.0-591.0)/(852.0-97.0);
        double voidSpaceWidthRatio = (591.0-543.0)/(852.0-97.0);
        if(view.isFinalFrenzy()) {
            if(!(playerView.getBoard().getPoints().size() == view.getStartingPointsSize())){
                firstEmptySpaceRatio = (543.0-113.0)/(852.0-113.0);
                marksPaneWidthRatio = (852.0-591.0)/(852.0-113.0);
                voidSpaceWidthRatio = (591.0-543.0)/(852.0-113.0);
            }
        }
        playerAmmoPane = new PlayerAmmoPane(playerView, width*firstEmptySpaceRatio, height, view);

        VoidSpaceButton voidSpaceButtonUp = new VoidSpaceButton("", width*voidSpaceWidthRatio, height, view);

        double marksPaneHeightRatio = (81.0)/(92.0);
        playerMarksPane = new PlayerMarksPane(playerView,width*marksPaneWidthRatio, height*marksPaneHeightRatio, view );
        playerMarksPane.setAlignment(Pos.TOP_LEFT);
        hBox.getChildren().add(0, playerAmmoPane);
        hBox.getChildren().add(1, voidSpaceButtonUp);
        hBox.getChildren().add(2, playerMarksPane);

        return  hBox;
    }

    private HBox UnderDamagePaneSpace(double width, double height) {
        HBox hBox = new HBox();
        hBox.setMinSize(width, height);
        hBox.setMaxSize(width, height);
        double firstEmptySpaceRatio = (228.0-97.0)/(852.0-97.0);
        double deathsPaneWidthRatio = (591.0-228)/(852.0-97.0);
        if(view.isFinalFrenzy()) {
            if(!(playerView.getBoard().getPoints().size() == view.getStartingPointsSize())){
                firstEmptySpaceRatio = (228.0-113.0)/(852.0-113.0);
                deathsPaneWidthRatio = (591.0-228)/(852.0-113.0);
            }
        }
        VoidSpaceButton voidSpaceButton = new VoidSpaceButton("", width*firstEmptySpaceRatio, height, view);
        voidSpaceButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        VBox vBox = new VBox();
        vBox.setMaxSize(width*deathsPaneWidthRatio, height);
        vBox.setMinSize(width*deathsPaneWidthRatio, height);

        double emptySpaceOverDeathPaneRatio =(197.0-177.0)/(270-177);
        double deathsPaneHeightRatio = (260.0-197.0)/(270-177);
        VoidSpaceButton emptySpaceOverDeathsPane = new VoidSpaceButton("", vBox.getMinWidth(), vBox.getMinHeight()*emptySpaceOverDeathPaneRatio, view);
        emptySpaceOverDeathsPane.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        vBox.getChildren().add(0, emptySpaceOverDeathsPane);

        int numberOfDeaths = playerView.getNOfDeaths();
        if(view.isFinalFrenzy()){
            VoidSpaceButton voidSpaceButtonDeaths = new VoidSpaceButton("", vBox.getMinWidth(), vBox.getMinHeight()*deathsPaneHeightRatio, view);
            vBox.getChildren().add(1, voidSpaceButtonDeaths);
        } else{
            PlayerDeathsPane playerDeathsPane = new PlayerDeathsPane(numberOfDeaths, vBox.getMinWidth(), vBox.getMinHeight()*deathsPaneHeightRatio, view);
            vBox.getChildren().add(1, playerDeathsPane);
        }

        hBox.getChildren().add(0, voidSpaceButton);
        hBox.getChildren().add(1, vBox);

        return  hBox;
    }

    private void setActionButtons(double width, double height) {
        VBox vSpace = new VBox();
        if(!view.isFinalFrenzy()) {
            setSizeButton(empty, width / 15, height / 5.8);
            setSizeButton(run, width / 15, height / 6);
            setSizeButton(grab, width / 15, height / 6);
            setSizeButton(shoot, width / 15, height / 6);
            vSpace.getChildren().add(empty);
            vSpace.getChildren().add(run);
            vSpace.getChildren().add(grab);
            vSpace.getChildren().add(shoot);
        }
        else {
            setSizeButton(empty, width / 15, height / 8);
            setSizeButton(shoot, width / 15, height / 7.5);
            setSizeButton(run, width / 15, height / 7.8);
            setSizeButton(grab, width / 15, height / 8);
            setSizeButton(frenzyEmpty, width / 15, height / 7.2);
            setSizeButton(frenzyShoot, width / 15, height / 7.5);
            setSizeButton(frenzyGrab, width / 15, height / 7.5);
            vSpace.getChildren().add(empty);
            vSpace.getChildren().add(shoot);
            vSpace.getChildren().add(run);
            vSpace.getChildren().add(grab);
            vSpace.getChildren().add(frenzyEmpty);
            vSpace.getChildren().add(frenzyShoot);
            vSpace.getChildren().add(frenzyGrab);

            if(ClientContext.get().getCurrentPlayer().getPlayerState() == PlayerState.FRENZY_AFTER) {
                shoot.setDisable(true);
                run.setDisable(true);
                grab.setDisable(true);
            }
            else if(ClientContext.get().getCurrentPlayer().getPlayerState() == PlayerState.FRENZY_BEFORE) {
                frenzyShoot.setDisable(true);
                frenzyGrab.setDisable(true);
            }
        }
        double vSpaceWidthRatio = 97.0/1120;
        if(view.isFinalFrenzy()) {
            if(!(playerView.getBoard().getPoints().size() == view.getStartingPointsSize())){
                vSpaceWidthRatio = 113.0/1120;
            }
        }

        vSpace.setMaxSize(width*vSpaceWidthRatio, height);
        vSpace.setMinSize(width*vSpaceWidthRatio, height);
        getChildren().add(0,vSpace);

        empty.setDisable(true);
        frenzyEmpty.setDisable(true);
        //set highlight
        setButtonProperties(empty);
        setButtonProperties(run);
        setButtonProperties(grab);
        setButtonProperties(shoot);
        setButtonProperties(frenzyEmpty);
        setButtonProperties(frenzyGrab);
        setButtonProperties(frenzyShoot);

        //set actions
        run.setOnAction(e -> {
            int actionIndex = -1;
            actionIndex = ClientContext.get().getPossibleChoices().getSelectableActions().contains(TypeOfAction.RUN) ?
                    ClientContext.get().getPossibleChoices().getSelectableActions().indexOf(TypeOfAction.RUN) : -1;
            if (actionIndex != -1) {
                view.getCommandParsing().initExecutionCommand(CommandParsing.getChooseActionCommand() + " " + + actionIndex);
            } else {
                view.playAudio("/sounds/denied.wav");
                view.ack("Cannot do the run action");
            }
        });

        grab.setOnAction(e -> {
            int actionIndex = -1;
            actionIndex = ClientContext.get().getPossibleChoices().getSelectableActions().contains(TypeOfAction.GRAB) ?
                    ClientContext.get().getPossibleChoices().getSelectableActions().indexOf(TypeOfAction.GRAB) : -1;
            if(actionIndex != -1) {
                view.getCommandParsing().initExecutionCommand(CommandParsing.getChooseActionCommand() + " " + + actionIndex);
            }
            else {
                view.playAudio("/sounds/denied.wav");
                view.ack("Cannot do the grab action");
            }
        });

        shoot.setOnAction(e -> {
            int actionIndex = -1;
            actionIndex = ClientContext.get().getPossibleChoices().getSelectableActions().contains(TypeOfAction.SHOOT) ?
                    ClientContext.get().getPossibleChoices().getSelectableActions().indexOf(TypeOfAction.SHOOT) : -1;
            if(actionIndex != -1) {
                view.getCommandParsing().initExecutionCommand(CommandParsing.getChooseActionCommand() + " " + + actionIndex);
            }
            else {
                view.playAudio("/sounds/denied.wav");
                view.ack("Cannot do the shoot action");
            }
        });
        frenzyShoot.setOnAction(shoot.getOnAction());
        frenzyGrab.setOnAction(grab.getOnAction());

    }

    private void setButtonProperties(Button button) {
        //do not display the text
        button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        button.setOpacity(0);
        button.setOnMouseEntered(e -> {
            button.setOpacity(0.15);
            button.setEffect(new DropShadow(BlurType.TWO_PASS_BOX, getColorFromPlayer(playerView.getPlayerColor()), 10, 100, 0 ,0));
        });
        button.setOnMouseExited(e -> {
            button.setOpacity(0);
            button.setEffect(null);
        });
    }

    private void setSizeButton(Button button, double w, double h) {
        button.setMinSize(w, h);
        button.setMaxSize(w, h);
    }

    private String getImageFromColor (PlayerColor playerColor){
        String imagesPath = "/images";
        String path = imagesPath + "/blue_board";

        switch (playerColor) {
            case YELLOW:
                path = imagesPath + "/yellow_board";
                break;
            case PURPLE:
                path = imagesPath + "/purple_board";
                break;
            case GREY:
                path = imagesPath + "/grey_board";
                break;
            case GREEN:
                path = imagesPath + "/green_board";
                break;
            default:
        }

        if(view.isFinalFrenzy()) {
            path = path + "_frenzy";
            if(playerView.getBoard().getPoints().size() == view.getStartingPointsSize()){
                path = path + "_normal";
            }
        }
        path = path + ".png";

        return path;
    }

    private Color getColorFromPlayer(PlayerColor playerColor) {
        Color color = Color.YELLOW;
        switch (playerColor) {
            case BLUE:
                color = Color.BLUE;
                break;
            case PURPLE:
                color = Color.PURPLE;
                break;
            case GREEN:
                color = Color.GREEN;
                break;
            case GREY:
                color = Color.GREY;
                break;
            default:
        }
        return color;
    }

    void deleteButtons() {
        empty.setDisable(true);
        run.setDisable(true);
        grab.setDisable(true);
        shoot.setDisable(true);
        frenzyEmpty.setDisable(true);
        frenzyShoot.setDisable(true);
        frenzyGrab.setDisable(true);
    }
}
