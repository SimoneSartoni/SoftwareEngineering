package view.gui;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import model.enums.PlayerColor;
import model.player.PlayerView;

import java.util.ArrayList;
import java.util.List;

class PlayerMarksPane extends HBox {
    private final GUIViewJavaFX view;
    private List<DamageImagePane> imagePanes;

    /**
     * Constructor for PlayerMarksPane class, receiving his width and height, obtaining current player marks from client
     * model.
     * Set dimensions to width and height.
     * @param width the width of the pane
     * @param height the height of the pane
     * @param playerView the player to create the marks pane for
     */
    PlayerMarksPane(PlayerView playerView, double width, double height, GUIViewJavaFX viewJavaFX) {
        super();
        this.view = viewJavaFX;
        this.setPrefSize(width, height);
        imagePanes = new ArrayList<>();
        DamageImagePane damageImagePane;
        int numberOfMarks, i=0;
        for(PlayerColor playerColor: PlayerColor.values()){
            if( (!playerView.getPlayerColor().equals(playerColor)) && (playerView.getMarksView().containsKey(playerColor))){
                numberOfMarks = playerView.getMarksView().get(playerColor);
                if(numberOfMarks>0){
                    damageImagePane = new DamageImagePane(playerColor, width/4, height, numberOfMarks, viewJavaFX);
                    imagePanes.add(damageImagePane);
                    getChildren().add(i, damageImagePane);
                    damageImagePane.setAlignment(Pos.TOP_LEFT);
                    i++;
                }
            }

        }

    }



}
