package view.gui;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import model.player.PlayerView;

import java.util.ArrayList;
import java.util.List;

class EnemyPowerUpPane extends HBox {

    /**
     * Create a new HBox to represent the selected enemy powerups. They will always be on their back becuase every player can see only his own powerups
     * @param playerView the enemy selected
     * @param height the height of the pane
     * @param viewJavaFX the GUI insance
     */
    EnemyPowerUpPane(PlayerView playerView, double height, GUIViewJavaFX viewJavaFX) {
        super();
        double width = height *720/406;
        setPrefSize(width, height);
        ImageView imageViewPowerUp;
        for(int i = 0; ((i<3)&&(i<playerView.getNOfPowerUps())); i++){
            String path = "/images/cards/POWS_BACK.png";
            imageViewPowerUp = new ImageView(new Image(path, width/3, height, false, true));
            getChildren().add(imageViewPowerUp);
        }
    }
}
