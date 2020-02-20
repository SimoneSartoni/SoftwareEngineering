package view.gui;

import javafx.scene.image.Image;
import javafx.scene.layout.*;
import model.enums.AmmoColor;
import model.utility.MapInfoView;
import model.weapon.Weapon;
import network.ClientContext;

import java.util.List;


class GameBoardPane extends  VBox{
    private final GUIViewJavaFX view;
    KillShotTrackPane killShotTrackPane;
    private HSpawnPointTileBox spawnPointTileBoxBlue;
    private VSpawnPointTileBox spawnPointTileBoxYellow;
    private VSpawnPointTileBox spawnPointTileBoxRed;
    private MapGridPane mapGridPane;
    double firstRowRatio = 400.0/1930.0;

    /**
     * Create the main window of the GUI. The window is divided in three row, and each row contains the subelements composing the graphic.
     * Every pane is set so that it will not be stretched if different screen resolutions
     * @param mapInfoView the current state of the map
     * @param width the width of the pane
     * @param height the height of the pane
     * @param viewJavaFX the GUI instance
     */
    GameBoardPane(MapInfoView mapInfoView, double width, double height, GUIViewJavaFX viewJavaFX) {
        super();
        this.view = viewJavaFX;


        setPrefSize(width, height);

        HBox firstRow = new HBox();
        firstRow.setPrefSize(width, height*firstRowRatio);
        firstRow.setMaxSize(width, height*firstRowRatio);
        firstRow.setMinSize(width, height*firstRowRatio);

        killShotTrackPane = new KillShotTrackPane(ClientContext.get().getKillShotTrack(), firstRow.getPrefHeight(), viewJavaFX);
        firstRow.getChildren().add(0, killShotTrackPane);

        List<Weapon> blueWeaponList = mapInfoView.getMap().get(0).get(2).getWeapons();
        List<String> blueWeaponsImages = view.getWeaponsListImages(blueWeaponList);
        spawnPointTileBoxBlue = new HSpawnPointTileBox(blueWeaponList, blueWeaponsImages, AmmoColor.BLUE, firstRow.getPrefHeight(), viewJavaFX);
        firstRow.getChildren().add(1,spawnPointTileBoxBlue);

        double widthLeft = width - killShotTrackPane.getPrefWidth()-spawnPointTileBoxBlue.getPrefWidth();
        VoidSpaceButton voidSpaceButton = new VoidSpaceButton("", (2279.0-2220.0)/(2550.0)*width, firstRow.getPrefHeight(), viewJavaFX);
        BtnPowerUpDeck btnPowerUpDeck = new BtnPowerUpDeck(firstRow.getPrefHeight()*(368.0-105.0)/400.0, firstRow.getPrefHeight(), width*(2466.0-2279.0)/(2550.0), firstRow.getPrefHeight()*105.0/400.0, viewJavaFX);
        firstRow.getChildren().add(2, voidSpaceButton);
        firstRow.getChildren().add(3, btnPowerUpDeck);

        double secondRowRatio = 1.0 - firstRowRatio;
        HBox secondRow = new HBox();
        secondRow.setPrefSize(width, height*secondRowRatio);
        secondRow.setMaxSize(width, height*secondRowRatio);
        secondRow.setMinSize(width, height*secondRowRatio);


        double beforeMapRatio = 414.0/2550;
        double mapWidthRatio = (2144.0-414.0)/2550;
        double mapHeightRatio = (1719.0-401.0)/(1930-401);
        double afterMapRatio = 1.0 - beforeMapRatio - mapWidthRatio;


        double secondRowFirstColoumnRatio = (675.0-400.0)/(2550-400);
        VBox secondRowFirstColoum = new VBox();
        secondRowFirstColoum.setPrefSize(secondRow.getPrefWidth()*beforeMapRatio, secondRow.getPrefHeight());
        secondRowFirstColoum.setMaxSize(secondRow.getPrefWidth()*beforeMapRatio, secondRow.getPrefHeight());
        secondRowFirstColoum.setMinSize(secondRow.getPrefWidth()*beforeMapRatio, secondRow.getPrefHeight());

        VoidSpaceButton btn = new VoidSpaceButton("", secondRowFirstColoum.getPrefWidth(), secondRowFirstColoum.getPrefHeight()*secondRowFirstColoumnRatio, view);
        List<Weapon> redWeaponsList = mapInfoView.getMap().get(1).get(0).getWeapons();
        List<String> redWeaponsImages = view.getWeaponsListImages(redWeaponsList);
        spawnPointTileBoxRed = new VSpawnPointTileBox(redWeaponsList, redWeaponsImages, AmmoColor.RED, secondRowFirstColoum.getPrefWidth(),-90.0, viewJavaFX);
        secondRowFirstColoum.getChildren().add(0, btn);
        secondRowFirstColoum.getChildren().add(1, spawnPointTileBoxRed);

        mapGridPane = new MapGridPane(mapInfoView, secondRow.getPrefWidth()*mapWidthRatio, secondRow.getPrefHeight()*mapHeightRatio, view);


        double secondRowThirdColoumRatio = (1059.0-400.0)/(1930-400);
        VBox secondRowThirdColoum = new VBox();
        secondRowThirdColoum.setPrefSize(secondRow.getPrefWidth()*afterMapRatio, secondRow.getPrefHeight());
        secondRowThirdColoum.setMaxSize(secondRow.getPrefWidth()*afterMapRatio, secondRow.getPrefHeight());
        secondRowThirdColoum.setMinSize(secondRow.getPrefWidth()*afterMapRatio, secondRow.getPrefHeight());

        double firstBtnRightColoumnHeightRatio = (519.0-400.0)/(1930-400);
        VoidSpaceButton firstBtnRightColoumn = new VoidSpaceButton("", secondRowThirdColoum.getPrefWidth(), secondRowThirdColoum.getPrefHeight()*firstBtnRightColoumnHeightRatio, view);
        double weaponDeckHeightRatio = (925.0-519.0)/(1930-400);
        double weaponDeckWidth = secondRowThirdColoum.getPrefHeight()*weaponDeckHeightRatio*240.0/406.0;
        BtnWeaponDeck btnWeaponDeck = new BtnWeaponDeck(weaponDeckWidth, secondRowThirdColoum.getPrefHeight()*weaponDeckHeightRatio, secondRowThirdColoum.getPrefWidth(), secondRowThirdColoum.getPrefWidth()*(2217.0-2144.0)/(2550.0-2144.0), viewJavaFX);
        double secondBtnRightColoumnHeightRatio = (1059.0-925.0)/(1930-400);
        VoidSpaceButton secondBtnRightColoumn = new VoidSpaceButton("", secondRowThirdColoum.getPrefWidth(), secondRowThirdColoum.getPrefHeight()*secondBtnRightColoumnHeightRatio, view);

        List<Weapon> yellowWeaponsList = mapInfoView.getMap().get(2).get(3).getWeapons();
        List<String> yellowWeaponsImages = view.getWeaponsListImages(yellowWeaponsList);
        spawnPointTileBoxYellow = new VSpawnPointTileBox(yellowWeaponsList, yellowWeaponsImages, AmmoColor.YELLOW, secondRowThirdColoum.getPrefWidth(), 90.0,  view);
        secondRowThirdColoum.getChildren().add(0, firstBtnRightColoumn);
        secondRowThirdColoum.getChildren().add(1, btnWeaponDeck);
        secondRowThirdColoum.getChildren().add(2, secondBtnRightColoumn);
        secondRowThirdColoum.getChildren().add(3, spawnPointTileBoxYellow);

        secondRow.getChildren().add(secondRowFirstColoum);
        secondRow.getChildren().add(mapGridPane);
        secondRow.getChildren().add(secondRowThirdColoum);

        getChildren().add(0,firstRow);
        getChildren().add(1,secondRow);


        try{
            int mapNumber = ClientContext.get().getMap().getNumber();
            String path = "/images/map" + mapNumber + "transp.png";
            String url = getClass().getResource(path).toExternalForm();
            Image mapImage = new Image(url);
            BackgroundSize backgroundSize = new BackgroundSize(width, height+2, false, false, false, false);
            BackgroundImage backgroundImage = new BackgroundImage(mapImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, backgroundSize);
            Background background = new Background(backgroundImage);
            this.setBackground(background);
        }
        catch (Exception e){
            //do not set background
            viewJavaFX.ack("Exception on setting background");
        }



    }


}
