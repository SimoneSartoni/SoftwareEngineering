package view.gui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import model.board.AmmoTile;
import model.board.TileView;
import model.enums.PlayerState;
import model.player.PlayerView;
import model.utility.MapInfoView;
import network.ClientContext;

class MapGridPane extends TilePane {
    private final GUIViewJavaFX view;

    /**
     * Constructor for MapGridPane class, receiving his width and height, obtaining information from parameter MapInfo.
     * Set dimensions to width and height.
     * @param width the width of the pane
     * @param height the height of the pane
     * @param map the MapInfoView from which obtain map information
     */
    MapGridPane(MapInfoView map, double width, double height, GUIViewJavaFX viewJavaFX) {
        super();
        this.view = viewJavaFX;
        int index=0;
        setMinSize(width, height);
        setMaxSize(width, height);
        setPrefSize(width, height);
        setPadding(new Insets(0, 0, 0, 0));
        setVgap(0);
        setHgap(0);
        setPrefColumns(map.getMapWidth());
        setPrefRows(map.getMapHeight());
        for(int i=0;i<map.getMapHeight(); i++){
            for(int k=0; k<map.getMapWidth(); k++){
                addStackPane(i, k, width, height, map, index);
                index++;
            }
        }
    }

    /***
     * Create an StackPane object inserting an Tile Image in position i, k of map.
     * @param map MapInfo
     * @param i row of map
     * @param k column of map
     * @param width width of the stackPane
     * @param height height of the stackPane
     */
    private void addStackPane(int i, int k, double width, double height, MapInfoView map, int index) {
        StackPane stackPane = new StackPane();
        double buttonWidht = width/map.getMapWidth()-1;
        double buttonHeight = height/map.getMapHeight()-1;
        addImages(stackPane, i, k, buttonWidht, buttonHeight);
        BtnTile btnTile = new BtnTile((i + " , " + k), buttonWidht, buttonHeight, i, k, view);
        stackPane.getChildren().add(btnTile);
        getChildren().add(index, stackPane);
    }

    /***
     * Create an StackPane object inserting an Tile Image in position i, k of map.
     * @param stackPane stackPane to insert the image in
     * @param i row of map
     * @param k column of map
     * @param width width of the stackPane
     * @param height height of the stackPane
     */
    private void addImages(StackPane stackPane, int i, int k, double width, double height) {
        GridPane gridPane = new GridPane();
        gridPane.setMinSize(width/1.2, height/1.2);
        gridPane.setMaxSize(width/1.2, height/1.2);

        MapInfoView map = ClientContext.get().getMap();

        TileView curTile = map.getMap().get(i).get(k);
        AmmoTile ammoTile = curTile.getAmmo();
        Button emptyButton = new Button();
        emptyButton.setMinHeight(height/4);
        emptyButton.setMinWidth(height/4);
        emptyButton.setDisable(true);
        emptyButton.setVisible(false);
        gridPane.add(emptyButton, 0, 0);
        if(ammoTile != null && ammoTile.getAmmoGained() != null) {
            String ammoName = ammoTile.getNOfPowerUp() + "P" +
                    ammoTile.getAmmoGained().getRedValue() + "R" +
                    ammoTile.getAmmoGained().getBlueValue() + "B" +
                    ammoTile.getAmmoGained().getYellowValue() + "Y";
            ammoName = ammoName.replace("0P", "");
            ammoName = ammoName.replace("0R", "");
            ammoName = ammoName.replace("0B", "");
            ammoName = ammoName.replace("0Y", "");
            String ammoUrl = "/images/ammoTiles/" + ammoName + ".png";
            ImageView imageAmmo = new ImageView(new Image(ammoUrl));
            imageAmmo.setFitHeight(height / 4);
            imageAmmo.setFitWidth(height / 4);
            gridPane.add(imageAmmo, 1 ,0);
        }

        int row = 1;
        int col = 0;
        for(PlayerView player : curTile.getPlayerViews()) {
            String playerUrl = "/images/" + player.getPlayerColor().name().toLowerCase() + "_circle";
            if(player.getPlayerState()== PlayerState.DEAD){
                playerUrl = playerUrl+"_dead";
            }
            playerUrl = playerUrl+".png";
            ImageView playerImage = new ImageView(new Image(playerUrl));
            playerImage.setFitWidth(height/3.5);
            playerImage.setFitHeight(height/3.5);
            gridPane.add(playerImage, col, row);
            col ++;
            if(col > 2) {
                col = 0;
                row ++;
            }
        }

        stackPane.getChildren().add(gridPane);
    }

}
