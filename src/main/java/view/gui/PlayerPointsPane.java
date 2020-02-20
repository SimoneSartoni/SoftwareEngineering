package view.gui;

import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

class PlayerPointsPane extends TextField {
    private final GUIViewJavaFX view;
    private ImageView imageView;

    /**
     * Constructor for PlayerPointsPane class, receiving his height and obtaining player points from client
     * model.
     * Set dimensions to height received and calculated width to preserve proportions.
     * @param height the width of the pane
     */
    PlayerPointsPane(String points, double height, GUIViewJavaFX viewJavaFX) {
        super(points);
        this.view = viewJavaFX;
        double width = height * 275.0/406.0;
        this.setPrefSize(width, height);
        setEditable(false);
        if(!points.equals("0")) {
            try {
                setOpacity(1);
                String path = "/images/points.png";
                String url = getClass().getResource(path).toExternalForm();
                Image mapImage = new Image(url);
                BackgroundSize backgroundSize = new BackgroundSize(width, height, false, false, false, false);
                BackgroundImage backgroundImage = new BackgroundImage(mapImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, backgroundSize);
                Background background = new Background(backgroundImage);
                setBackground(background);
                setStyle("-fx-text-inner-color: red;");
                setAlignment(Pos.CENTER);
                setFont(Font.font("Verdana", FontWeight.BOLD, 50*height/406.0));
            } catch (Exception e) {
                //dont set the background
                viewJavaFX.ack("Exception on setting background");
            }
        }
        else{
            setOpacity(0);
        }
    }

}
