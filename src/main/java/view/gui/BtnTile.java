package view.gui;


import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import model.enums.Direction;
import model.enums.PlayerColor;
import network.ClientContext;
import view.cli.CommandParsing;


public class BtnTile extends Button {
    private final int row;
    private final int column;
    public  final GUIViewJavaFX view;

    /**
     * Construct a button that represent a tile in GUI. It is created by taking images from file, adding the behavior
     * of the "choose tile command" or "choose direction" or "choose_room" when clicked.
     * If the command can be executed it will be done. A little player color border surround the button when mouse enter in his area
     * @param label the powerup that the button represent
     * @param width the width of the button
     * @param height the height of the button
     * @param row the x coordinates of the tile
     * @param column the y coordinates of the tile
     * @param view the GUI instance
     *
     */
    BtnTile(String label, double width, double height, int row, int column, GUIViewJavaFX view){
        super(label);
        this.view = view;
        this.row = row;
        this.column = column;
        setPrefSize(width, height);
        setPadding(new Insets(0,0,0,0));
        setActionAndStyle();
    }

    private void setActionAndStyle() {
        //do not display the text
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        setOpacity(0);

        setOnMouseEntered(e -> {
            setOpacity(0.1);
            Color color;
            if(ClientContext.get().getCurrentPlayer() != null && ClientContext.get().getCurrentPlayer().getPlayerColor() != null) {
                color = getColorFromPlayer(ClientContext.get().getCurrentPlayer().getPlayerColor());
            }
            else {
                color = Color.GOLD;
            }
            setEffect(new DropShadow(BlurType.TWO_PASS_BOX, color, 10, 100, 0 ,0));
        });

        setOnMouseExited(e -> {
            setOpacity(0);
            setEffect(null);
        });

        setOnMouseClicked(e -> {
            int actionIndex;
            if((!ClientContext.get().getPossibleCommands().isEmpty())&&(ClientContext.get().getPossibleCommands().contains(CommandParsing.getChooseTileCommand()))) {
                actionIndex = ClientContext.get().indexOfSelectableTile(row, column);
                view.getCommandParsing().initExecutionCommand(CommandParsing.getChooseTileCommand() + " " + +actionIndex);
            }
            else if((!ClientContext.get().getPossibleCommands().isEmpty())&&(ClientContext.get().getPossibleCommands().contains(CommandParsing.getChooseDirectionCommand()))) {
                Direction direction = ClientContext.get().getDirectionBetweenTiles(ClientContext.get().getCurrentPlayer().getCurrentTile(), ClientContext.get().getMap().getMap().get(row).get(column));
                if(direction != null) {
                    actionIndex = ClientContext.get().getPossibleChoices().getSelectableDirections().indexOf(direction);
                    view.getCommandParsing().initExecutionCommand(CommandParsing.getChooseDirectionCommand() + " " + +actionIndex);
                }
                else {
                    view.playAudio("/sounds/denied.wav");
                }
            }
            else if((!ClientContext.get().getPossibleCommands().isEmpty())&&(ClientContext.get().getPossibleCommands().contains(CommandParsing.getChooseRoomCommand()))) {
                actionIndex = ClientContext.get().getPossibleChoices().getSelectableRooms().indexOf(ClientContext.get().getMap().getMap().get(row).get(column).getRoom());
                if (actionIndex != -1) {
                    view.getCommandParsing().initExecutionCommand(CommandParsing.getChooseRoomCommand() + " " + +actionIndex);
                }
                else {
                    view.playAudio("/sounds/denied.wav");
                }
            }
            else {
                view.playAudio("/sounds/denied.wav");
            }
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
