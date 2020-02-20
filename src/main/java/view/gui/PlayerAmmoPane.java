package view.gui;

import javafx.scene.layout.HBox;
import model.enums.AmmoColor;
import model.player.PlayerView;
import network.ClientContext;

import java.util.ArrayList;
import java.util.List;

class PlayerAmmoPane extends HBox {
    private final GUIViewJavaFX view;
    private List<BtnAmmo> imagePanes;

    /**
     * Constructor for PlayerAmmoPane class, receiving his width and height, obtaining received player Ammo from client
     * model.
     * Set dimensions to width and height.
     * @param width the width of the pane
     * @param height the height of the pane
     * @param playerView the player to create the ammo pane for
     */
    PlayerAmmoPane(PlayerView playerView, double width, double height, GUIViewJavaFX viewJavaFX) {
        super();
        this.view = viewJavaFX;
        this.setPrefSize(width, height);
        imagePanes = new ArrayList<>();
        BtnAmmo ammoImagePane;
        double ammoHeightRatio = 47.0/97;
        for(int i=0; i<playerView.getAmmo().getYellowValue(); i++){
            ammoImagePane = new BtnAmmo(AmmoColor.YELLOW, width/9, height*ammoHeightRatio, playerView.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID()),  viewJavaFX);
            imagePanes.add(ammoImagePane);
            getChildren().add(i, ammoImagePane);
        }
        for(int i=0; i<playerView.getAmmo().getBlueValue(); i++){
            ammoImagePane = new BtnAmmo(AmmoColor.BLUE, width/9, height*ammoHeightRatio,playerView.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID()), viewJavaFX);
            imagePanes.add(ammoImagePane);
            getChildren().add(i, ammoImagePane);
        }
        for(int i=0; i<playerView.getAmmo().getRedValue(); i++){
            ammoImagePane = new BtnAmmo(AmmoColor.RED, width/9, height*ammoHeightRatio,playerView.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID()), viewJavaFX);
            imagePanes.add(ammoImagePane);
            getChildren().add(i, ammoImagePane);
        }

    }
}
