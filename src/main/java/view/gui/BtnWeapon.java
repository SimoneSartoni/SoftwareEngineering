package view.gui;

import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import model.enums.LoadedState;
import model.enums.PlayerColor;
import model.enums.TypeOfEffect;
import model.weapon.Effect;
import model.weapon.Weapon;
import network.ClientContext;
import view.cli.CommandParsing;


public class BtnWeapon extends StackPane {
    public final Weapon weapon;
    public final GUIViewJavaFX viewJavaFX;
    public final Button button;
    String imagePath;
    boolean addEffectsButtons;

    /**
     * Construct a button that represent a weapon in GUI. It is created by taking images from file, adding the behavior
     * of the "choose weapon command" when clicked on the top, otherwise every effect can be clicked separately to do the "choose effect" command.
     * If the commands can be executed it will be done. A little player color border sourround the button when mouse enter in his area
     * @param weapon the weapon that the button represent
     * @param imagePath the path of the image
     * @param width the width of the button
     * @param height the height of the button
     * @param rotation float that indicates if the weapon image has to be rotated
     * @param addEffectsButtons boolean that indicates if the button has to be surround with effects of halo and highlitness
     * @param showUnloaded boolean that indicates if the weapon is unloaded
     * @param viewJavaFX the GUI instance
     *
     */
    BtnWeapon(Weapon weapon, String imagePath, double width, double height, double rotation, boolean addEffectsButtons, boolean showUnloaded, GUIViewJavaFX viewJavaFX){
        super();
        this.imagePath = imagePath;
        this.addEffectsButtons = addEffectsButtons;
        this.viewJavaFX = viewJavaFX;
        this.weapon = weapon;
        this.button = new Button();
        if(weapon!=null){
            setAccessibleText(weapon.getIdName());
        }
        setMinSize(width, height);
        setMaxSize(width, height);
        AddWeaponButton(imagePath, width, height, rotation, showUnloaded);
        if(addEffectsButtons)
            AddEffectsButtons();
    }

    private void AddWeaponButton(String imagePath, double width, double height, double rotation, boolean showUnloaded){
        button.setMaxSize(getMaxWidth(), getMaxHeight());
        button.setMinSize(getMinWidth(), getMinHeight());

        button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        button.setOnMouseEntered(e -> {
            Color color;
            if(ClientContext.get().getCurrentPlayer() != null && ClientContext.get().getCurrentPlayer().getPlayerColor() != null) {
                color = getColorFromPlayer(ClientContext.get().getCurrentPlayer().getPlayerColor());
            }
            else {
                color = Color.GOLD;
            }
            button.setEffect(new DropShadow(BlurType.TWO_PASS_BOX, color, 10, 100, 0 ,0));
        });

        button.setOnMouseExited(e -> {
            button.setEffect(null);
        });

        button.setOnMouseClicked(e -> {
            if(e.getButton() == MouseButton.PRIMARY){
                if (this.weapon!=null){
                    int choiceIndex = ClientContext.get().getPossibleChoices().getSelectableWeapons().indexOf(weapon);
                    viewJavaFX.getCommandParsing().initExecutionCommand(CommandParsing.getChooseWeaponCommand() +" "+choiceIndex);
                }
                else {
                    viewJavaFX.playAudio("/sounds/denied.wav");
                }
            }
            else if(e.getButton() == MouseButton.SECONDARY){
                viewJavaFX.onZoom(this);
            }
        });
        try{
            String url = getClass().getResource(imagePath).toExternalForm();
            Image buttonImage = new Image(url);
            ImageView buttonView = new ImageView(buttonImage);
            if(rotation == 0) {
                buttonView.setFitWidth(width);
                buttonView.setFitHeight(height);
            }
            else {
                buttonView.setFitWidth(height);
                buttonView.setFitHeight(width);
            }
            button.setGraphic(buttonView);
            buttonView.setRotate(rotation);
            if((showUnloaded)&&(weapon.getLoaded().equals(LoadedState.UNLOADED))){
                setOpacity(0.5);
            }
        }
        catch (Exception e){
            //dont set the background
            viewJavaFX.ack("Exception on setting background");
        }
        getChildren().add(button);
    }

    private void AddEffectsButtons(){
        VBox vBox = new VBox();
        vBox.setMaxSize(getMaxWidth(), getMaxHeight());
        vBox.setMinSize(getMinWidth(), getMinHeight());
        Button btnBaseEffect, btnImageSpace, btnAlternativeEffect;
        HBox optionalBaseEffects;

        double imageSpaceHeightRatio = 173.0/406;
        btnImageSpace = new VoidSpaceButton("", getMaxWidth(), getMaxHeight()*imageSpaceHeightRatio, viewJavaFX);
        setActionAndStyle(btnImageSpace);
        btnImageSpace.setOnMouseClicked(e -> {
            if(e.getButton() == MouseButton.PRIMARY){
                if (this.weapon!=null){
                    if((!ClientContext.get().getPossibleCommands().isEmpty())&&(ClientContext.get().getPossibleCommands().contains(CommandParsing.getChooseWeaponCommand()))){
                        int choiceIndex = ClientContext.get().getPossibleChoices().getSelectableWeapons().indexOf(weapon);
                        viewJavaFX.getCommandParsing().initExecutionCommand(CommandParsing.getChooseWeaponCommand() +" "+choiceIndex);
                    }
                    else {
                        viewJavaFX.playAudio("/sounds/denied.wav");
                    }
                }
                else {
                    viewJavaFX.playAudio("/sounds/denied.wav");
                }
            }
            else if(e.getButton() == MouseButton.SECONDARY){
                viewJavaFX.onZoom(this);
            }
        });
        vBox.getChildren().add(0,btnImageSpace);

        if(weapon.getAlternativeEffect()!=null){
            btnBaseEffect = TypeOfEffectButton(getMaxWidth(), getMaxHeight()*((1.0-imageSpaceHeightRatio))/2, TypeOfEffect.BASE);
            btnAlternativeEffect = TypeOfEffectButton(getMaxWidth(),getMaxHeight()*((1.0-imageSpaceHeightRatio)/2), TypeOfEffect.ALTERNATIVE);
            vBox.getChildren().add(1, btnBaseEffect);
            vBox.getChildren().add(2, btnAlternativeEffect);
        }
        else {
            if (!weapon.getOptionalEffect().isEmpty()){
                btnBaseEffect = TypeOfEffectButton(getMaxWidth(), getMaxHeight() * ((1.0 - imageSpaceHeightRatio)) / 2, TypeOfEffect.BASE);
                optionalBaseEffects = OptionalEffectHBox(getMaxWidth(), getMaxHeight() * ((1.0 - imageSpaceHeightRatio) / 2));
                vBox.getChildren().add(1, btnBaseEffect);
                vBox.getChildren().add(2, optionalBaseEffects);
            }
            else{
                btnBaseEffect = TypeOfEffectButton(getMaxWidth(), getMaxHeight()*((1.0-imageSpaceHeightRatio)), TypeOfEffect.BASE);
                vBox.getChildren().add(1, btnBaseEffect);
            }
        }

        getChildren().add(vBox);
    }

    private Button TypeOfEffectButton(double width, double height, TypeOfEffect typeOfEffect) {
        Button btn = new Button();
        btn.setMinSize(width, height);
        btn.setMaxSize(width, height);
        btn.setOnMouseClicked(e -> {
            if(e.getButton() == MouseButton.PRIMARY){
                if (this.weapon!=null){
                    if((!ClientContext.get().getPossibleCommands().isEmpty())&&(ClientContext.get().getPossibleCommands().contains(CommandParsing.getChooseTypeOfEffectCommand()))) {
                        int choiceIndex = ClientContext.get().getPossibleChoices().getSelectableEffects().indexOf(typeOfEffect);
                        viewJavaFX.getCommandParsing().initExecutionCommand(CommandParsing.getChooseTypeOfEffectCommand() + " " + choiceIndex);
                    }
                    else {
                        viewJavaFX.playAudio("/sounds/denied.wav");
                    }
                }
                else {
                    viewJavaFX.playAudio("/sounds/denied.wav");
                }
            }
            else if(e.getButton() == MouseButton.SECONDARY){
                viewJavaFX.onZoom(this);
            }
        });

        setActionAndStyle(btn);
        return btn;
    }

    private HBox OptionalEffectHBox(double width, double height) {
        HBox hBox = new HBox();
        hBox.setMinSize(width, height);
        hBox.setMaxSize(width, height);
        Button btn;
        int i=0;
        for(Effect optionalEffect : weapon.getOptionalEffect()){
            btn = new Button();
            btn.setMinSize(width/weapon.getOptionalEffect().size(), height);
            btn.setMaxSize(width, height);
            btn.setOnMouseClicked(e -> {
                if(e.getButton() == MouseButton.PRIMARY){
                    if(optionalEffect!=null){
                        int choiceIndex;
                        if((!ClientContext.get().getPossibleCommands().isEmpty())&&(ClientContext.get().getPossibleCommands().contains(CommandParsing.getChooseEffectCommand()))){
                            choiceIndex = ClientContext.get().getPossibleChoices().getSelectableOptionalEffects().indexOf(optionalEffect);
                            viewJavaFX.getCommandParsing().initExecutionCommand(CommandParsing.getChooseEffectCommand() +" "+choiceIndex);
                        }
                        else if((!ClientContext.get().getPossibleCommands().isEmpty())&&(ClientContext.get().getPossibleCommands().contains(CommandParsing.getChooseTypeOfEffectCommand()))){
                            choiceIndex = ClientContext.get().getPossibleChoices().getSelectableEffects().indexOf(TypeOfEffect.OPTIONAL);
                            viewJavaFX.getCommandParsing().initExecutionCommand(CommandParsing.getChooseTypeOfEffectCommand() + " " + choiceIndex);
                        }
                        else {
                            viewJavaFX.playAudio("/sounds/denied.wav");
                        }
                        viewJavaFX.onUpdate(PaneToUpdate.LOGGER, "Command not available\n");
                    }
                    else {
                        viewJavaFX.playAudio("/sounds/denied.wav");
                    }
                }
                else if(e.getButton() == MouseButton.SECONDARY){
                    viewJavaFX.onZoom(this);
                }
            });
            setActionAndStyle(btn);
            hBox.getChildren().add(i, btn);
            i++;
        }
        return hBox;
    }

    private void setActionAndStyle(Button button) {
        //do not display the text
        button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        button.setOpacity(0);

        button.setOnMouseEntered(e -> {
            button.setOpacity(0.1);
            Color color;
            if(ClientContext.get().getCurrentPlayer() != null && ClientContext.get().getCurrentPlayer().getPlayerColor() != null) {
                color = getColorFromPlayer(ClientContext.get().getCurrentPlayer().getPlayerColor());
            }
            else {
                color = Color.GOLD;
            }
            button.setEffect(new DropShadow(BlurType.TWO_PASS_BOX, color, 10, 100, 0 ,0));
        });

        button.setOnMouseExited(e -> {
            button.setOpacity(0);
            button.setEffect(null);
        });
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