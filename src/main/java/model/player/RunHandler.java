package model.player;

import model.board.GameManager;
import model.board.Tile;
import model.board.TileView;
import model.enums.TurnState;
import model.exceptions.MovementException;

import java.util.List;

public class RunHandler {

    public RunHandler(){
    }

    /**
     * method used to check if the run action can be done
     * @param player
     * @return true if it can be performed, false otherwise
     */
    public static boolean isValidMovement(Player player){
        if(player.getCurrentTurnAction().getOnlyMovement()!=0)
            return true;
        return false;
    }

    /**
     * method to perform the movement action, notifying the player about what tiles he can choose
     * @param gameManager
     * @param player the current player
     */
    public static void chooseTileMovementAction(GameManager gameManager,Player player){
        List<Tile> targettableTiles=gameManager.getPossibleTiles(1,player.getCurrentTurnAction().getOnlyMovement(),player.getCurrentTile());
        gameManager.getCurrentTurn().getPossibleChoices().setSelectableTiles(targettableTiles);
        player.notifyOnLog("Choose a tile where you want to run");
        player.setTurnState(TurnState.CHOOSE_TILE_FOR_RUN_ACTION);
        player.notifyTiles(gameManager);
    }

    /**
     * method to perform the movement to the selected tile (the action is finished)
     * @param gameManager
     * @param t1
     * @param player
     */
    public static void movementActionAfterTile(GameManager gameManager,Tile t1,Player player){
        t1.addPlayer(player);
        player.getCurrentTile().removePlayer(player);
        player.setCurrentTile(t1);
        gameManager.notifyOnMovement(player, t1);
        player.handleActions(gameManager);
    }

    /**
     * checks if the selected tile is valid or not
     * @param gameManager
     * @param t1 the selected tile
     * @return
     * @throws MovementException if the selected tile is not valid
     */
    public static Tile isAValidMovementAction(GameManager gameManager, TileView t1) throws MovementException {
        for (Tile t : gameManager.getCurrentTurn().getPossibleChoices().getSelectableTiles())
            if (t.getX() == t1.getX() && t.getY() == t1.getY())
                return t;
        if (t1.isHole()){ throw new MovementException("Can not move Out of Map"); }
        throw new MovementException("Can not move to this tile");
    }

}
