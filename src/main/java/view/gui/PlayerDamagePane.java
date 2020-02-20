package view.gui;

import javafx.scene.layout.HBox;
import model.player.PlayerView;

import java.util.ArrayList;
import java.util.List;

class PlayerDamagePane extends HBox {
    private final GUIViewJavaFX view;
    private List<DamageImagePane> imagePanes;

    /**
     * Constructor for PlayerDamagePane class, receiving his width and height, obtaining received player Damages from client
     * model.
     * Set dimensions to width and height.
     * @param width the width of the pane
     * @param height the height of the pane
     * @param playerView the player to create the damage pane for
     */
    PlayerDamagePane(PlayerView playerView, double width, double height, GUIViewJavaFX viewJavaFX) {
        super();
        this.view = viewJavaFX;
        this.setPrefSize(width, height);
        imagePanes = new ArrayList<>();
        DamageImagePane damageImagePane;
        for(int i=0; i<playerView.getDamageTakenView().size(); i++){
            damageImagePane = new DamageImagePane(playerView.getDamageTakenView().get(i), width/12, height, 1, viewJavaFX);
            imagePanes.add(damageImagePane);
            getChildren().add(i, damageImagePane);
        }

    }



}
