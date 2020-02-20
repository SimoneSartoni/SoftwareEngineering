package view.gui;

import javafx.scene.control.Button;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import model.enums.PlayerColor;
import model.player.PlayerView;
import network.ClientContext;


public class BtnEnemySymbol extends Button {
    public final PlayerView playerView;
    public final GUIViewJavaFX viewJavaFX;

    /**
     * Construct a button that represent an enemy in GUI. It is created by taking images from file, adding the behavior
     * consisting of zooming and let see the board of the enemy player selected when clicked. A little halo is showed when
     * the mouse enter in his area
     * @param playerView the color of the ammo that the button represent
     * @param width the width of the button
     * @param height the height of the button
     * @param viewJavaFX the GUI instance
     */
    BtnEnemySymbol(PlayerView playerView, double width, double height, GUIViewJavaFX viewJavaFX){
        super();
        this.viewJavaFX = viewJavaFX;
        this.playerView = playerView;
        if(playerView!=null){
            setAccessibleText(playerView.getPlayerID());
        }
        setPrefSize(width, height);

        setOnAction(e -> {
            if(this.playerView!=null){
                viewJavaFX.getClientController().setEnembyShownBoard(this.playerView);
            }
            else {
                viewJavaFX.playAudio("/sounds/denied.wav");
            }
        });

        setOnMouseEntered(e -> {
            Color color;
            if(ClientContext.get().getCurrentPlayer() != null && ClientContext.get().getCurrentPlayer().getPlayerColor() != null) {
                color = getColorFromPlayer(ClientContext.get().getCurrentPlayer().getPlayerColor());
            }
            else {
                color = Color.GOLD;
            }
            setEffect(new DropShadow(BlurType.TWO_PASS_BOX, color, 5, 100, 0 ,0));
        });

        setOnMouseExited(e -> {
            setEffect(null);
        });

        try{
            if(playerView!= null) {
                String path = getImageFromColor(playerView.getPlayerColor());
                String url = getClass().getResource(path).toExternalForm();
                Image mapImage = new Image(url);
                BackgroundSize backgroundSize = new BackgroundSize(width, height, false, false, false, false);
                BackgroundImage backgroundImage = new BackgroundImage(mapImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, backgroundSize);
                Background background = new Background(backgroundImage);
                this.setBackground(background);
            }
        }
        catch (Exception e){
            //dont set the background
            viewJavaFX.ack("Exception on setting background");
        }
    }

    private String getImageFromColor (PlayerColor playerColor){
        String imagesPath = "/images";
        String path = imagesPath + "/blue_symbol.png";

        switch (playerColor) {
            case YELLOW:
                path = imagesPath + "/yellow_symbol.png";
                break;
            case PURPLE:
                path = imagesPath + "/purple_symbol.png";
                break;
            case GREY:
                path = imagesPath + "/grey_symbol.png";
                break;
            case GREEN:
                path = imagesPath + "/green_symbol.png";
                break;
            default:
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
