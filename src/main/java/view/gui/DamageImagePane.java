package view.gui;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import model.enums.PlayerColor;

class DamageImagePane extends StackPane {
    private final GUIViewJavaFX view;
    private ImageView imageView;

    /**
     * Create a new Stack Pane to represent the damages taken from a player
     * @param playerColor the color of the player who owns the pane
     * @param width the width of the pane
     * @param height the height of the pane
     * @param number the number of damages
     * @param viewJavaFX the GUI instance
     */
    DamageImagePane(PlayerColor playerColor, double width, double height, int number, GUIViewJavaFX viewJavaFX) {
        super();
        this.view = viewJavaFX;
        this.setPrefSize(width, height);

        try{
            String path = getImageFromColor(playerColor, number);
            String url = getClass().getResource(path).toExternalForm();
            Image mapImage = new Image(url);
            imageView = new ImageView(mapImage);
            imageView.setFitWidth(width*3/4);
            imageView.setFitHeight(height*3/4);
            getChildren().add(imageView);
        }
        catch (Exception e){
            //dont set the background
            viewJavaFX.ack("Exception on setting background-marks:" +e.getMessage());
        }
    }



    private String getImageFromColor (PlayerColor playerColor, int number){
        String imagesPath = "/images";
        String path = imagesPath + "/blue_drop"+number+".png";
        switch (playerColor) {
            case YELLOW:
                path = imagesPath + "/yellow_drop"+number+".png";
                break;
            case PURPLE:
                path = imagesPath + "/purple_drop"+number+".png";
                break;
            case GREY:
                path = imagesPath + "/grey_drop"+number+".png";
                break;
            case GREEN:
                path = imagesPath + "/green_drop"+number+".png";
                break;
        }

        return path;
    }


}
