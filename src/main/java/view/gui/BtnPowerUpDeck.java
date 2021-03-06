package view.gui;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


class BtnPowerUpDeck extends VBox {

    /**
     * Construct a not clickable button that represent the deck of weapons
     * @param width the width of the button
     * @param height the height of the button
     * @param heightBeforeDeck the height of the void space between the button and the image inside
     * @param heightDeck the height of the image inside the button
     * @param viewJavaFX the GUI instance
     *
     */
    BtnPowerUpDeck(double heightDeck, double height, double width, double heightBeforeDeck, GUIViewJavaFX viewJavaFX){
        super();
        setMinSize(width, height);
        setMaxSize(width, height);
        Image weaponDeckImage = null;
        try{
            String path = "/images/cards/POWS_BACK.png";
            String url = getClass().getResource(path).toExternalForm();
            weaponDeckImage = new Image(url);
            ImageView weaponDeckImageView = new ImageView(weaponDeckImage);
            weaponDeckImageView.setFitWidth(width);
            weaponDeckImageView.setFitHeight(heightDeck);

            getChildren().add(0, new VoidSpaceButton("", width, heightBeforeDeck, viewJavaFX));
            getChildren().add(1,weaponDeckImageView);
        }
        catch (Exception e){
            //dont set the background
            Button button = new Button();
            button.setPrefSize(width, height);
            viewJavaFX.ack("Wrong set background deck");
            getChildren().add(0, new VoidSpaceButton("", width, heightBeforeDeck, viewJavaFX));
            getChildren().add(1,button);
        }

    }

}