package view.gui;

import javafx.scene.control.Button;
import model.player.PlayerView;

public class VoidSpaceButton extends Button {

    private final GUIViewJavaFX view;
    private PlayerView playerView;

    /**
     * Constructor for VoidSpaceButton class, receiving his width and height, used to insert void spaces.
     * Set dimensions to width and height.
     * @param width the width of the pane
     * @param height the height of the pane
     */
    public VoidSpaceButton(String string, double width, double height, GUIViewJavaFX viewJavaFX) {
        super(string);
        this.view = viewJavaFX;
        setSize(width, height);
        setStyle("-fx-border-color: transparent; -fx-border-width: 0; -fx-background-radius: 0; -fx-background-color: transparent;");
    }


    public void setSize(double width, double height){
        setPrefSize(width, height);
        setMinSize(width, height);
        setMaxSize(width, height);
    }
}
