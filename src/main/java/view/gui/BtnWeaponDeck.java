package view.gui;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;


class BtnWeaponDeck extends HBox {

    /**
     * Construct a not clickable button that represent the deck of weapons
     * @param width the width of the button
     * @param height the height of the button
     * @param widthBeforeDeck the width of the void space between the button and the image inside
     * @param widthDeck the width of the image inside the button
     * @param viewJavaFX the GUI instance
     *
     */
    BtnWeaponDeck(double widthDeck, double height, double width, double widthBeforeDeck, GUIViewJavaFX viewJavaFX){
        super();
        setMinSize(width, height);
        setMaxSize(width, height);
        Image weaponDeckImage = null;
        try{
            String path = "/images/cards/WEAPONS_BACK.png";
            String url = getClass().getResource(path).toExternalForm();
            weaponDeckImage = new Image(url);
            ImageView weaponDeckImageView = new ImageView(weaponDeckImage);
            weaponDeckImageView.setFitWidth(widthDeck);
            weaponDeckImageView.setFitHeight(height);

            getChildren().add(0, new VoidSpaceButton("", widthBeforeDeck, height, viewJavaFX));
            getChildren().add(1,weaponDeckImageView);
        }
        catch (Exception e){
            //dont set the background
            Button button = new Button();
            button.setPrefSize(widthDeck, height);
            viewJavaFX.ack("Wrong set background deck");
            getChildren().add(0, new VoidSpaceButton("", widthBeforeDeck, height, viewJavaFX));
            getChildren().add(1,button);
        }

    }

}