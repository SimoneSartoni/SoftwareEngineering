package view.cli;

import network.ClientContext;
import network.ClientController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandParsing {

    private static final String ERROR_COMMAND = ">>> ERROR: Wrong selection";

    private int choiceNumber;
    private static final String QUIT_COMMAND = "quit";
    private static final String PRINT_HELP ="help";
    private static final String PLAYER_STATUS_COMMAND = "status";
    private static final String DRAW_MAP_COMMAND = "draw_map";
    private static final String CHOOSE_POWERUP_COMMAND = "powerup";
    private static final String NOTHING_COMMAND = "nothing";
    private static final String CHOOSE_ACTION_COMMAND = "action";
    private static final String CHOOSE_ROOM_COMMAND = "room";
    private static final String CHOOSE_TILE_COMMAND = "tile";
    private static final String CHOOSE_AMMO_COMMAND = "ammo";
    private static final String CHOOSE_TYPE_OF_EFFECT_COMMAND = "type";
    private static final String CHOOSE_EFFECT_COMMAND = "effect";
    private static final String CHOOSE_TARGET_COMMAND = "target";
    private static final String CHOOSE_WEAPON_COMMAND = "weapon";
    private static final String CHOOSE_DIRECTION_COMMAND = "direction";
    private static final String OTHER_STATUS_COMMAND = "other_status";
    private static final String TILE_STATUS_COMMAND = "status_tile";
    private final ClientController clientController;

    public CommandParsing(ClientController clientController) {
        this.clientController = clientController;
    }

    /***
     * Function that given a command, init to execute it or printHelp if that command does not exists.
     * @param command the command given by the user.
     */
    public void initExecutionCommand(String command) {
        String [] splitted = command.split("\\s");
        List<String> args = new ArrayList<>();
        if(splitted.length > 1) {
            command = splitted[0];
            args = new ArrayList<>(Arrays.asList(splitted));
            args.remove(0);
        }

        if(ClientContext.get().isAskForCommand() && !clientController.isToClose()) {
            clientController.requestPossibleCommands();
        }
        if(!clientController.isToClose()) {
            executeCommand(command, args);
        }
        else if(!ClientContext.get().isAskForCommand() && !clientController.isToClose()) {
            clientController.requestPossibleCommands();
        }


    }

    /***
     * Method the actually executed the right command calling methods of client controller.
     * @param command the command to executed
     * @param args the parameters of the command
     */
    private void executeCommand(String command, List<String> args) {
        switch (command) {
            case (QUIT_COMMAND):
                clientController.quit();
                break;
            case (DRAW_MAP_COMMAND) :
                clientController.drawMap();
                break;
            case (PLAYER_STATUS_COMMAND):
                clientController.giveStatus();
                break;
            case (OTHER_STATUS_COMMAND):
                clientController.giveOtherStatus();
                break;
            case (TILE_STATUS_COMMAND):
                giveTileStatus(args);
                break;
            case (CHOOSE_POWERUP_COMMAND):
                parseInteger(args);
                executePowerupCommand();
                break;
            case (NOTHING_COMMAND):
                clientController.chooseNothing();
                break;
            case (CHOOSE_ACTION_COMMAND):
                parseInteger(args);
                executeActionCommand();
                break;
            case (CHOOSE_ROOM_COMMAND):
                parseInteger(args);
                executeRoomCommand();
                break;
            case (CHOOSE_TILE_COMMAND):
                parseInteger(args);
                executeTileCommand();
                break;
            case (CHOOSE_AMMO_COMMAND):
                parseInteger(args);
                executeAmmoCommand();
                break;
            case (CHOOSE_TYPE_OF_EFFECT_COMMAND):
                parseInteger(args);
                executeEffectTypeCommand();
                break;
            case (CHOOSE_EFFECT_COMMAND):
                parseInteger(args);
                executeEffectCommand();
                break;
            case (CHOOSE_TARGET_COMMAND):
                parseInteger(args);
                executeTargetCommand();
                break;
            case (CHOOSE_WEAPON_COMMAND):
                parseInteger(args);
                executeWeaponCommand();
                break;
            case (CHOOSE_DIRECTION_COMMAND):
                parseInteger(args);
                executeDirectionCommand();
                break;
            case (PRINT_HELP):
                clientController.printCommands();
                break;
            default:
                clientController.printCommands();
        }
    }

    private void executePowerupCommand() {
        if(choiceNumber >= ClientContext.get().getPossibleChoices().getSelectablePowerUps().size() || choiceNumber < 0) {
            ack(ERROR_COMMAND);
            clientController.getViewClient().denyMove();
            return;
        }
        clientController.choosePowerUp(ClientContext.get().getPossibleChoices().getSelectablePowerUps().get(choiceNumber));
    }

    private void executeActionCommand() {
        if(choiceNumber >= ClientContext.get().getPossibleChoices().getSelectableActions().size() || choiceNumber < 0) {
            ack(ERROR_COMMAND);
            clientController.getViewClient().denyMove();
            return;
        }
        clientController.chooseAction(ClientContext.get().getPossibleChoices().getSelectableActions().get(choiceNumber));
    }

    private void executeRoomCommand() {
        if(choiceNumber >= ClientContext.get().getPossibleChoices().getSelectableRooms().size() || choiceNumber < 0) {
            ack(ERROR_COMMAND);
            clientController.getViewClient().denyMove();
            return;
        }
        clientController.chooseRoom(ClientContext.get().getPossibleChoices().getSelectableRooms().get(choiceNumber));
    }

    private void executeTileCommand() {
        if(choiceNumber >= ClientContext.get().getPossibleChoices().getSelectableTiles().size() || choiceNumber < 0) {
            ack(ERROR_COMMAND);
            clientController.getViewClient().denyMove();
            return;
        }
        clientController.chooseTile(ClientContext.get().getPossibleChoices().getSelectableTiles().get(choiceNumber));
    }

    private void executeAmmoCommand() {
        if(choiceNumber >= ClientContext.get().getPossibleChoices().getSelectableAmmo().size() || choiceNumber < 0) {
            ack(ERROR_COMMAND);
            clientController.getViewClient().denyMove();
            return;
        }
        clientController.chooseAmmo(ClientContext.get().getPossibleChoices().getSelectableAmmo().get(choiceNumber));
    }

    private void executeEffectTypeCommand() {
        if(choiceNumber >= ClientContext.get().getPossibleChoices().getSelectableEffects().size() || choiceNumber < 0) {
            ack(ERROR_COMMAND);
            clientController.getViewClient().denyMove();
            return;
        }
        clientController.chooseTypeOfEffect(ClientContext.get().getPossibleChoices().getSelectableEffects().get(choiceNumber));
    }

    private void executeEffectCommand() {
        if(choiceNumber >= ClientContext.get().getPossibleChoices().getSelectableOptionalEffects().size() || choiceNumber < 0) {
            ack(ERROR_COMMAND);
            clientController.getViewClient().denyMove();
            return;
        }
        clientController.chooseEffect(ClientContext.get().getPossibleChoices().getSelectableOptionalEffects().get(choiceNumber));
    }

    private void executeTargetCommand() {
        if(choiceNumber >= ClientContext.get().getPossibleChoices().getSelectableTargets().size() || choiceNumber < 0) {
            ack(ERROR_COMMAND);
            clientController.getViewClient().denyMove();
            return;
        }
        clientController.chooseTarget(ClientContext.get().getPossibleChoices().getSelectableTargets().get(choiceNumber));
    }

    private void executeWeaponCommand() {
        if(choiceNumber >= ClientContext.get().getPossibleChoices().getSelectableWeapons().size() || choiceNumber < 0) {
            ack(ERROR_COMMAND);
            clientController.getViewClient().denyMove();
            return;
        }
        clientController.chooseWeapon(ClientContext.get().getPossibleChoices().getSelectableWeapons().get(choiceNumber));
    }

    private void executeDirectionCommand() {
        if(choiceNumber >= ClientContext.get().getPossibleChoices().getSelectableDirections().size() || choiceNumber < 0) {
            ack(ERROR_COMMAND);
            clientController.getViewClient().denyMove();
            return;
        }
        clientController.chooseDirection(ClientContext.get().getPossibleChoices().getSelectableDirections().get(choiceNumber));
    }

    private void giveTileStatus(List<String> args) {
        if(args.size() < 2) {
            ack(">>>>>> ERROR: Insert two parameters");
            clientController.getViewClient().denyMove();
            return;
        }
        int x;
        int y;
        try {
            x = Integer.parseInt(args.get(0));
            y = Integer.parseInt(args.get(1));
        } catch (NumberFormatException e) {
            ack("ERROR: Wrong parameters");
            clientController.getViewClient().denyMove();
            return;
        }

        clientController.giveTileStatus(x, y);
    }

    private void parseInteger(List<String> args){
        if(args.size()!=1){
            ack(">>>>>> ERROR: Insert one parameter");
            clientController.getViewClient().denyMove();
            choiceNumber = -1;
            return;
        }
        try {
            choiceNumber =Integer.parseInt(args.get(0));
        }catch(NumberFormatException e){
            ack("ERROR: Wrong parameter");
            clientController.getViewClient().denyMove();
            choiceNumber = -1;
        }
    }

    private void ack(String content) {
        clientController.getViewClient().ack(content);
    }

    public static String getErrorCommand() {
        return ERROR_COMMAND;
    }

    public static String getQuitCommand() {
        return QUIT_COMMAND;
    }

    public static String getPrintHelp() {
        return PRINT_HELP;
    }

    public static String getPlayerStatusCommand() {
        return PLAYER_STATUS_COMMAND;
    }

    public static String getDrawMapCommand() {
        return DRAW_MAP_COMMAND;
    }

    public static String getChoosePowerupCommand() {
        return CHOOSE_POWERUP_COMMAND;
    }

    public static String getNothingCommand() {
        return NOTHING_COMMAND;
    }

    public static String getChooseActionCommand() {
        return CHOOSE_ACTION_COMMAND;
    }

    public static String getChooseRoomCommand() {
        return CHOOSE_ROOM_COMMAND;
    }

    public static String getChooseTileCommand() {
        return CHOOSE_TILE_COMMAND;
    }

    public static String getChooseAmmoCommand() {
        return CHOOSE_AMMO_COMMAND;
    }

    public static String getChooseTypeOfEffectCommand() {
        return CHOOSE_TYPE_OF_EFFECT_COMMAND;
    }

    public static String getChooseEffectCommand() {
        return CHOOSE_EFFECT_COMMAND;
    }

    public static String getChooseTargetCommand() {
        return CHOOSE_TARGET_COMMAND;
    }

    public static String getChooseWeaponCommand() {
        return CHOOSE_WEAPON_COMMAND;
    }

    public static String getChooseDirectionCommand() {
        return CHOOSE_DIRECTION_COMMAND;
    }

    public static String getOtherStatusCommand() {
        return OTHER_STATUS_COMMAND;
    }

    public static String getTileStatusCommand() {
        return TILE_STATUS_COMMAND;
    }
}
