package view.gui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import model.powerup.PowerUp;
import model.utility.MapInfoView;
import model.weapon.Weapon;
import network.ClientContext;

import java.rmi.RemoteException;
import java.util.List;

class PlayingScene extends Scene {

    private GameBoardPane gameBoardPane;
    private EnemiesBoardPane enemiesBoardPane;
    private PlayerBoardPane playerBoardPane;
    private WeaponPane weaponPane;
    private PowerUpPane powerUpPane;
    private EnemyWeaponsPane enemyWeaponsPane;
    private EnemyPowerUpPane enemyPowerUpPane;
    private PlayerPointsPane enemyPointsPane, playerPointsPane;
    ZoomPane zoomPane;
    GUILogger guiLogger;
    private HBox hBoxTop;
    private HBox hBoxBottom;
    private VBox vBoxTopRight;
    private HBox hBoxEnemyCards;
    private HBox hBoxZoomLogger;
    private double gameBoardWidthRatio;
    private double widthWeaponsPowerUpsPoints;
    private double heightWeaponsPowerUpsPoints;
    final  public GUIViewJavaFX viewJavaFX;

    private final KeyCombination fullscreenKeyCombination = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.ALT_DOWN);
    private final KeyCombination helpKeyCombination = new KeyCodeCombination(KeyCode.H);
    private boolean hPressed;

    /***
     * Constructor for PlayingScene class, receiving a void VBox, his width and height.
     * Set scene root to VBox and set VBox dimensions to width and height
     * Set each characteristics and function for a playable GUI for Adrenaline Board Game
     * @param vBox  void Vbox to use as root
     * @param width the wFidth of the pane
     * @param height the height of the pane
     */
    PlayingScene(VBox vBox, double width, double height, GUIViewJavaFX view) {
        super(vBox, width, height);
        this.viewJavaFX = view;

        addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if(fullscreenKeyCombination.match(e)){
                view.getMainStage().setFullScreen(!view.getMainStage().isFullScreen());
            }

            else if(helpKeyCombination.match(e) && !hPressed) {
                try {
                    hPressed = true;
                    viewJavaFX.startTutorial(false);
                } catch (RemoteException ex) {
                    System.err.println("Error while starting tutorial screen cause of H button");
                }
            }
        });

        vBox.setSpacing(0);
        vBox.setPrefSize(getWidth(), getHeight());
        vBox.setPadding(new Insets(0,0,0,0));

        gameBoardWidthRatio = height*2550.0*1120/(width*(1930*1120+2550*270));
        hBoxTop = new HBox(0);
        hBoxTop.setPrefSize(vBox.getPrefWidth(), width*gameBoardWidthRatio*1930.0/2550);
        hBoxTop.setPadding(new Insets(0, 0, 0, 0));
        updateGameBoardPane();

        vBoxTopRight = new VBox();
        vBoxTopRight.setPrefSize(vBox.getPrefWidth()*(1-gameBoardWidthRatio), width*gameBoardWidthRatio*1930.0/2550);
        vBoxTopRight.setPadding(new Insets(0, 0, 0, 0));
        updateEnemyPlayerPane();

        double heightCardRatio = 406.0/(720.0+ 3*169.0/264*406 + 275.0);
        hBoxEnemyCards = new HBox();
        hBoxEnemyCards.setPrefSize(vBoxTopRight.getPrefWidth(), vBoxTopRight.getPrefWidth()*heightCardRatio);
        hBoxEnemyCards.setPadding(new Insets(0, 0, 0, 0));

        updateEnemyCardsPane();
        createZoomPane();
        createLogger();
        updateEnemyPointsPane();
        hBoxTop.getChildren().add(1, vBoxTopRight);

        hBoxBottom = new HBox(0);
        hBoxBottom.setPrefSize(vBox.getPrefWidth(), vBox.getPrefHeight() - hBoxTop.getPrefHeight());
        hBoxBottom.setPadding(new Insets(0, 0, 0, 0));
        updatePlayerPane();
        widthWeaponsPowerUpsPoints = hBoxBottom.getPrefWidth() - playerBoardPane.getPrefWidth();
        heightWeaponsPowerUpsPoints = widthWeaponsPowerUpsPoints*406/(720.0+ 3*169.0/264*406 + 275);
        updateWeaponsPane();
        updatePowerupsPane();
        updatePlayerPointsPane();

        vBox.getChildren().add(0, hBoxTop);
        vBox.getChildren().add(1, hBoxBottom);



        //set background
        final String string = "/images/background.png";
        String url = getClass().getResource(string).toExternalForm();
        Image backgroundSource = new Image(url);
        BackgroundSize backgroundSize = new BackgroundSize(width, height, false, false, false, false);
        BackgroundImage backgroundImage = new BackgroundImage(backgroundSource, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, backgroundSize);
        Background background = new Background(backgroundImage);
        vBox.setBackground(background);
    }

    void sethPressed(boolean hPressed) {
        this.hPressed = hPressed;
    }

    /***
     * Create an PlayerPointsPane object obtaining selected enemy player points from the
     * enemyShownBoard in the client model and set enemyPointsPane global reference to it.
     * Its dimensions adapt to hBoxEnemyCards dimensions.
     * Add the object to hBoxEnemyCards children nodes in position 0.
     */
    void updateEnemyPointsPane() {
        if(!ClientContext.get().getPlayerViews().isEmpty()) {
            enemyPointsPane = new PlayerPointsPane("?", hBoxEnemyCards.getPrefHeight(), viewJavaFX);
            if (!hBoxEnemyCards.getChildren().isEmpty()) {
                if (hBoxEnemyCards.getChildren().size() < 3)
                    hBoxEnemyCards.getChildren().add(2, enemyPointsPane);
                else
                    hBoxEnemyCards.getChildren().set(2, enemyPointsPane);
            }
        }
    }

    /***
     * Create an EnemyPowerUpPane and EnemyWeaponsPane objects obtaining selected enemy player weapons and power-ups
     * from the enemyShownBoard in the client model and set enemyPowerUpPane and enemyWeaponsPane global references
     * to them.
     * Their dimensions adapt to hBoxEnemyCards dimensions.
     * Add the objects to hBoxEnemyCards children nodes in position 0 and 1.
     */
    void updateEnemyCardsPane() {
        if(ClientContext.get().getEnemyShownBoard()!=null){
            enemyPowerUpPane = new EnemyPowerUpPane(ClientContext.get().getEnemyShownBoard(), hBoxEnemyCards.getPrefHeight(), viewJavaFX);
        }
        else{
            if(!ClientContext.get().getPlayerViews().isEmpty()){
                enemyPowerUpPane = new EnemyPowerUpPane(ClientContext.get().getPlayerViews().get(0), hBoxEnemyCards.getPrefHeight(), viewJavaFX);
            }
        }
        if(ClientContext.get().getEnemyShownBoard()!=null){
            enemyWeaponsPane = new EnemyWeaponsPane(ClientContext.get().getEnemyShownBoard(),  hBoxEnemyCards.getPrefHeight(), viewJavaFX);
        }
        else{
            if(!ClientContext.get().getPlayerViews().isEmpty()){
                enemyWeaponsPane = new EnemyWeaponsPane(ClientContext.get().getPlayerViews().get(0),  hBoxEnemyCards.getPrefHeight(), viewJavaFX);
            }
        }

        if(ClientContext.get().getPlayerViews().isEmpty()){
            //hBoxEnemyCards.getChildren().add(0, new VoidSpaceButton("",hBoxEnemyCards.getPrefWidth()/2, hBoxEnemyCards.getPrefHeight() , viewJavaFX));
            //hBoxEnemyCards.getChildren().add(1, new VoidSpaceButton("",hBoxEnemyCards.getPrefWidth()/2, hBoxEnemyCards.getPrefHeight() , viewJavaFX));
            if(hBoxEnemyCards.getChildren().isEmpty()) {
                hBoxEnemyCards.getChildren().add(0, new VoidSpaceButton("", hBoxEnemyCards.getPrefWidth() / 2, hBoxEnemyCards.getPrefHeight(), viewJavaFX));
                hBoxEnemyCards.getChildren().add(1, new VoidSpaceButton("", hBoxEnemyCards.getPrefWidth() / 2, hBoxEnemyCards.getPrefHeight(), viewJavaFX));
            }
            else{
                hBoxEnemyCards.getChildren().set(0, new VoidSpaceButton("", hBoxEnemyCards.getPrefWidth() / 2, hBoxEnemyCards.getPrefHeight(), viewJavaFX));
                hBoxEnemyCards.getChildren().set(1, new VoidSpaceButton("", hBoxEnemyCards.getPrefWidth() / 2, hBoxEnemyCards.getPrefHeight(), viewJavaFX));
            }
        }else{
            if(!hBoxEnemyCards.getChildren().isEmpty()){
                hBoxEnemyCards.getChildren().set(0,enemyWeaponsPane);
                if(hBoxEnemyCards.getChildren().size()<2){
                    hBoxEnemyCards.getChildren().add(1, enemyPowerUpPane);
                }
                else{
                    hBoxEnemyCards.getChildren().set(1,enemyPowerUpPane);
                }
            }
            else{
                hBoxEnemyCards.getChildren().add(0, enemyWeaponsPane);
                hBoxEnemyCards.getChildren().add(1, enemyPowerUpPane);
            }
        }

        if(!vBoxTopRight.getChildren().isEmpty()){
            if(vBoxTopRight.getChildren().size()<2){
                vBoxTopRight.getChildren().add(1, hBoxEnemyCards);
            }
            else{
                vBoxTopRight.getChildren().set(1,hBoxEnemyCards);
            }
        }
    }

    /***
     * Create a GameBoardPane object obtaining current Map information from the client model and
     * set gameBoardPane global reference to it. Its dimensions adapt to hBoxTop dimensions.
     * Add the object to hBoxTop children nodes in position 0.
     */
    void updateGameBoardPane() {
        MapInfoView mapInfoView = ClientContext.get().getMap();
        gameBoardPane = new GameBoardPane(mapInfoView , hBoxTop.getPrefWidth()*gameBoardWidthRatio, hBoxTop.getPrefHeight(), viewJavaFX);
        if(hBoxTop.getChildren().isEmpty())
            hBoxTop.getChildren().add(0, gameBoardPane);
        else
            hBoxTop.getChildren().set(0,gameBoardPane);
    }

    /***
     * Create a WeaponPane object obtaining current player weapons from the client model and
     * set weaponPane global reference to it. Its dimensions adapt to hBoxBottom dimensions.
     * Add the object to hBoxBottom children nodes in position 1.
     */
    void updateWeaponsPane() {
        List<Weapon> weaponList = ClientContext.get().getWeapons();
        List<String> weaponsImages = viewJavaFX.getWeaponsListImages(weaponList);
        weaponPane = new WeaponPane(weaponList, weaponsImages, heightWeaponsPowerUpsPoints, viewJavaFX );
        if(!hBoxBottom.getChildren().isEmpty()){
            if(hBoxBottom.getChildren().size()<2){
                hBoxBottom.getChildren().add(1, weaponPane);
            }
            else{
                hBoxBottom.getChildren().set(1,weaponPane);
            }
        }
    }

    /***
     * Create a PowerUpPane object obtaining current player power-ups from the client model and
     * set powerUpPane global reference to it. Its dimensions adapt to hBoxBottom dimensions.
     * Add the object to hBoxBottom children nodes in position 2.
     */
    void updatePowerupsPane() {
        List<PowerUp> powsList = ClientContext.get().getPowerUps();
        List<String> powsImages = viewJavaFX.getPowsListImages(powsList);
        powerUpPane = new PowerUpPane(powsList, powsImages, heightWeaponsPowerUpsPoints, viewJavaFX);
        if(!hBoxBottom.getChildren().isEmpty()){
            if(hBoxBottom.getChildren().size()<3){
                hBoxBottom.getChildren().add(2, powerUpPane);
            }
            else{
                hBoxBottom.getChildren().set(2, powerUpPane);
            }
        }
    }

    /***
     * Create a PlayerPointsPane object obtaining current player points from the client model and
     * set playerPointsPane global reference to it. Its dimensions adapt to hBoxBottom dimensions.
     * Add the object to hBoxBottom children nodes in position 3.
     */
    void updatePlayerPointsPane() {
        playerPointsPane = new PlayerPointsPane(new StringBuilder().append(ClientContext.get().getPoints()).toString(), heightWeaponsPowerUpsPoints, viewJavaFX);
        if(!hBoxBottom.getChildren().isEmpty()){
            if(hBoxBottom.getChildren().size()<4){
                hBoxBottom.getChildren().add(3, playerPointsPane);
            }
            else{
                hBoxBottom.getChildren().set(3, playerPointsPane);
            }
        }
    }

    /***
     * Create a PlayerBoardPane object obtaining current player's information from the client model and
     * set playerBoardPane global reference to it. Its dimensions adapt to hBoxBottom dimensions.
     * Add the object to hBoxBottom children nodes in position 0.
     */
    void updatePlayerPane() {
        playerBoardPane = new PlayerBoardPane(ClientContext.get().getCurrentPlayer(), gameBoardPane.getPrefWidth(), viewJavaFX);
        playerBoardPane.setTranslateY(8);
        playerBoardPane.setTranslateX(2);
        if(!hBoxBottom.getChildren().isEmpty()){
            if(hBoxBottom.getChildren().size()<1){
                hBoxBottom.getChildren().add(0, playerBoardPane);
            }
            else{
                hBoxBottom.getChildren().set(0, playerBoardPane);
            }
        }
        else{
            hBoxBottom.getChildren().add(0, playerBoardPane);
        }
    }

    /***
     * Create an EnemyBoardPane object obtaining enemy players' information from the client model and
     * set enemiesBoardPane reference of PlayingScene to it. Object dimensions adapt to vBoxTopRight dimensions.
     * Add the object to vBoxTopRight children nodes in position 0.
     */
    void updateEnemyPlayerPane() {
        double rightPartRatio =1-gameBoardWidthRatio;
        double heightEnemyBoard = (getWidth()*rightPartRatio)*270/(1120+270);
        if(!ClientContext.get().getPlayerViews().isEmpty()){
            if(!vBoxTopRight.getChildren().isEmpty())
                vBoxTopRight.getChildren().remove(0);
            if(ClientContext.get().getEnemyShownBoard()!=null){
                enemiesBoardPane = new EnemiesBoardPane(ClientContext.get().getEnemyShownBoard(), heightEnemyBoard, viewJavaFX);
            }
            else{
                enemiesBoardPane = new EnemiesBoardPane(ClientContext.get().getPlayerViews().get(0), heightEnemyBoard, viewJavaFX);
            }
            vBoxTopRight.getChildren().add(0, enemiesBoardPane);
        }
        else{
            if(vBoxTopRight.getChildren().isEmpty()) {
                vBoxTopRight.getChildren().add(0, new VoidSpaceButton("", getWidth() * rightPartRatio, heightEnemyBoard, viewJavaFX));
            }else{
                vBoxTopRight.getChildren().set(0, new VoidSpaceButton("", getWidth() * rightPartRatio, heightEnemyBoard, viewJavaFX));

            }

        }
    }

    /***
     * Create a GUILogger object using fixed percentages to avoid images stretches or bad positioning,
     * in hBoxZoomLogger's children nodes at position 1.
     * The position and dimensions adapts to the width and height of the PlayingScene
     */
    private void createLogger() {
        double rightPartRatio =1-gameBoardWidthRatio;
        double heightEnemyBoard = (getWidth()*rightPartRatio)*270/(1120+270);
        guiLogger = new GUILogger(hBoxZoomLogger.getPrefWidth()/2, hBoxZoomLogger.getPrefHeight(), viewJavaFX);
        hBoxZoomLogger.getChildren().add(1, guiLogger);
    }

    /***
     * Create a ZoomPane object using fixed percentages to avoid images stretches or bad positioning,
     * in hBoxZoomLogger's children nodes at position 0. If hBoxZoomLogger is null creates it.
     * The position and dimensions adapts to the width and height of the PlayingScene
     */
    private void createZoomPane() {
        double rightPartRatio =1-gameBoardWidthRatio;
        double heightEnemyBoard = (getWidth()*rightPartRatio)*270/(1120+270);
        hBoxZoomLogger = new HBox();
        hBoxZoomLogger.setPrefSize(vBoxTopRight.getPrefWidth(), vBoxTopRight.getPrefHeight()-heightEnemyBoard-hBoxEnemyCards.getPrefHeight());
        zoomPane = new ZoomPane(hBoxZoomLogger.getPrefWidth()/2, hBoxZoomLogger.getPrefHeight(), viewJavaFX);
        if(hBoxZoomLogger.getChildren().isEmpty()){
            hBoxZoomLogger.getChildren().add(0, zoomPane);
        }
        else{
            hBoxZoomLogger.getChildren().set(0, zoomPane);
        }
        if(!vBoxTopRight.getChildren().isEmpty())
            if(vBoxTopRight.getChildren().size()<3)
                vBoxTopRight.getChildren().add(2, hBoxZoomLogger);
            else
                vBoxTopRight.getChildren().set(2, hBoxZoomLogger);
    }

    GUILogger getGuiLogger() {
        return guiLogger;
    }
}
