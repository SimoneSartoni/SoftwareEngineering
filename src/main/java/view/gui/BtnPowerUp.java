package view.gui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import model.enums.PlayerColor;
import model.enums.TypeOfAction;
import model.powerup.PowerUp;
import network.ClientContext;
import view.cli.CommandParsing;

public class BtnPowerUp extends Button {
    public final PowerUp powerUp;
    final String imagePath;
    public final GUIViewJavaFX viewJavaFX;

    /**
     * Construct a button that represent a powerup in GUI. It is created by taking images from file, adding the behavior
     * of the "choose powerup command" when clicked. If the command can be executed it will be done. A little player color border
     * surround the button when mouse enter in his area
     * @param powerUp the powerup that the button represent
     * @param imagePath the path of the image
     * @param width the width of the button
     * @param height the height of the button
     * @param viewJavaFX the GUI instance
     *
     */
    BtnPowerUp(PowerUp powerUp, String imagePath, double width, double height, GUIViewJavaFX viewJavaFX){
        super();
        this.viewJavaFX = viewJavaFX;
        this.powerUp = powerUp;
        this.imagePath = imagePath;
        if(powerUp!=null){
            setAccessibleText(powerUp.getName());
        }
        setPrefSize(width, height);
        double coverRatio = 0.1;
        setPadding(new Insets(height*coverRatio, height*coverRatio,width*coverRatio,width*coverRatio));
        setOnMouseClicked(e -> {
            if(e.getButton() == MouseButton.PRIMARY){
                if((powerUp!=null)&&(!ClientContext.get().getPossibleCommands().isEmpty())){
                    int choiceIndex;
                    if(ClientContext.get().getPossibleCommands().contains(CommandParsing.getChoosePowerupCommand())) {
                        choiceIndex = ClientContext.get().getPossibleChoices().getSelectablePowerUps().indexOf(powerUp);
                        viewJavaFX.getCommandParsing().initExecutionCommand(CommandParsing.getChoosePowerupCommand() + " " + choiceIndex);
                    }
                    else{
                        if(ClientContext.get().getPossibleCommands().contains(CommandParsing.getChooseActionCommand())) {
                            choiceIndex = ClientContext.get().getPossibleChoices().getSelectableActions().indexOf(TypeOfAction.POWER_UP);
                            viewJavaFX.getCommandParsing().initExecutionCommand(CommandParsing.getChooseActionCommand() + " " + choiceIndex);
                        }
                        else {
                            viewJavaFX.playAudio("/sounds/denied.wav");
                        }
                    }
                }
            }
            else if(e.getButton() == MouseButton.SECONDARY){
                viewJavaFX.onZoom(this);
            }
        });
        setOnAction(e -> {

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
            String url = getClass().getResource(imagePath).toExternalForm();
            Image mapImage = new Image(url);
            BackgroundSize backgroundSize = new BackgroundSize(width, height, false, false, false, false);
            BackgroundImage backgroundImage = new BackgroundImage(mapImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, backgroundSize);
            Background background = new Background(backgroundImage);
            this.setBackground(background);
        }
        catch (Exception e) {
            //dont set the background
        }
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
