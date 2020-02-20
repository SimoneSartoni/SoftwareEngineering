package view.gui;

import javafx.scene.control.Button;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import model.enums.AmmoColor;
import model.enums.PlayerColor;
import network.ClientContext;
import view.cli.CommandParsing;


public class BtnAmmo extends Button {
    public final GUIViewJavaFX viewJavaFX;

    /**
     * Construct a button that represent an ammo in GUI. It is created by taking images from file, adding the behavior
     * of the "choose ammo command" when clicked. If the command can be executed it will be done. A little player color border
     * surround the button when mouse enter in his area
     * @param ammoColor the color of the ammo that the button represent
     * @param width the width of the button
     * @param height the height of the button
     * @param activeButton boolean that indicates if the button is active or not
     * @param viewJavaFX the GUI instance
     */
    BtnAmmo(AmmoColor ammoColor, double width, double height, boolean activeButton, GUIViewJavaFX viewJavaFX){
        super();
        this.viewJavaFX = viewJavaFX;
        setPrefSize(width, height);

        if(activeButton) {
            setOnAction(e -> {
                if((!ClientContext.get().getPossibleCommands().isEmpty())&&(ClientContext.get().getPossibleCommands().contains(CommandParsing.getChooseAmmoCommand()))) {
                    int choiceIndex = ClientContext.get().getPossibleChoices().getSelectableAmmo().indexOf(ammoColor);
                    viewJavaFX.getCommandParsing().initExecutionCommand(CommandParsing.getChooseAmmoCommand() + " " + choiceIndex);
                }
                else {
                    viewJavaFX.playAudio("/sounds/denied.wav");
                }
            });

            setOnMouseEntered(e -> {
                Color color;
                if (ClientContext.get().getCurrentPlayer() != null && ClientContext.get().getCurrentPlayer().getPlayerColor() != null) {
                    color = getColorFromPlayer(ClientContext.get().getCurrentPlayer().getPlayerColor());
                } else {
                    color = Color.GOLD;
                }
                setEffect(new DropShadow(BlurType.TWO_PASS_BOX, color, 5, 100, 0, 0));
            });

            setOnMouseExited(e -> {
                setEffect(null);
            });
        }
        try{
            String path = getImageFromColor(ammoColor);
            String url = getClass().getResource(path).toExternalForm();
            Image mapImage = new Image(url);
            BackgroundSize backgroundSize = new BackgroundSize(width, height, false, false, false, false);
            BackgroundImage backgroundImage = new BackgroundImage(mapImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, backgroundSize);
            Background background = new Background(backgroundImage);
            setBackground(background);
        }
        catch (Exception e){
            //dont set the background
            viewJavaFX.ack("Exception on setting background");
        }
    }


    private String getImageFromColor (AmmoColor ammoColor){
        String imagesPath = "/images";
        String path = imagesPath + "/red_ammo.png";
        switch (ammoColor) {
            case YELLOW:
                path = imagesPath + "/yellow_ammo.png";
                break;
            case BLUE:
                path = imagesPath + "/blue_ammo.png";
                break;
        }

        return path;
    }


    private Color getColorFromPlayer(PlayerColor playerColor) {
        Color color = Color.YELLOW;
        switch (playerColor) {
            case BLUE:
                color = Color.BLUE;
                break;
            case PURPLE:
                color = Color.PURPLE;
                break;
            case GREEN:
                color = Color.GREEN;
                break;
            case GREY:
                color = Color.GREY;
                break;
            default:
        }
        return color;
    }

}
