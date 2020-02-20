package view.gui;

import javafx.scene.layout.HBox;
import model.powerup.PowerUp;

import java.util.ArrayList;
import java.util.List;

public class PowerUpPane extends HBox {

    private final GUIViewJavaFX view;
    private List<BtnPowerUp> powerUpButtons;

    /**
     * Constructor for PowerUpPane class, receiving height.
     * Set dimensions to height received and calculated width to preserve proportions.
     * Create from 0 to 3 PowerUpPane to add to its children according to number of power-ups in current player hand,
     * obtained from client model
     * @param height the height of the pane
     */
    PowerUpPane(List<PowerUp> powerUps, List<String> imagesPath, double height, GUIViewJavaFX viewJavaFX) {
        super();
        this.view = viewJavaFX;
        double width = height *169.0*3/264.0;
        setPrefSize(width, height);
        powerUpButtons = new ArrayList<>();
        BtnPowerUp btnPowerUp;
        for(int i = 0; ((i<3)&&(i<powerUps.size())); i++){
            String path = "";
            if(imagesPath.size() > i) {
                path = imagesPath.get(i);
            }
            btnPowerUp = new BtnPowerUp(powerUps.get(i), path, width/3, height, view);
            powerUpButtons.add(btnPowerUp);
            getChildren().add(btnPowerUp);
        }
    }
}
