package view.gui;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

class PlayerDeathsPane extends HBox {
    private final GUIViewJavaFX view;

    /**
     * Constructor for PlayerDeathsPane class, receiving his width and height, obtaining received number of Deaths from
     * client model and creating an ImageView with red_skull.
     * Set dimensions to width and height.
     * @param width the width of the pane
     * @param height the height of the pane
     * @param nOfDeaths the number of deaths.
     */
    PlayerDeathsPane(int nOfDeaths, double width, double height, GUIViewJavaFX viewJavaFX) {
        super();
        this.view = viewJavaFX;
        setSize(width, height);
        ImageView[] skullsImageViews = new ImageView[nOfDeaths];
        double skullWidth = width/6;
        for(int i = 0; i < nOfDeaths; i++) {
            skullsImageViews[i] = new ImageView(new Image("/images/red_skull.png", skullWidth, height, false, true));
            getChildren().add(i, skullsImageViews[i]);
        }
    }


    private void setSize(double width, double height){
        setPrefSize(width, height);
        setMinSize(width, height);
        setMaxSize(width, height);
    }
}

