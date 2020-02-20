package controller;

import model.board.GameManager;
import model.board.TileView;
import model.exceptions.GameException;
import model.player.Player;
import model.player.PlayerView;
import model.powerup.PowerUp;

import java.io.Serializable;

public class PowerUpController implements Serializable {
    /**
     * method to start the effect of a powerUp
     * @param powerUp the powerUp used
     * @param gameManager the game in which the action has been made
     * @param player the player that's playing the powerUp
     */
    public void startEffect(PowerUp powerUp, GameManager gameManager, Player player){
        powerUp.startEffect(gameManager,player);
    }

    /**
     *  method used when the player selects a target in a powerUp effect
     * @param powerUp the powerUp used
     * @param gameManager the game the player is in
     * @param player the player that's using the powerUp
     * @param targetPlayer the PlayerView selected
     * @throws GameException if the playerView selected is not valid
     */
    public void afterChoosingTarget(PowerUp powerUp, GameManager gameManager, Player player, PlayerView targetPlayer) throws  GameException{
        powerUp.afterChooseTarget(gameManager,player,powerUp.isAValidTarget(targetPlayer));
    }

    /**
     * method called when the player selects a tile in a powerUp effect
     * @param powerUp the powerUp used
     * @param gameManager the game the player is in
     * @param player  the player that's using the powerUp
     * @param tile the selected tileView
     * @throws GameException if the TileView selected is not valid
     */
    public void afterChoosingTile(PowerUp powerUp, GameManager gameManager, Player player, TileView tile) throws GameException{
            powerUp.afterChooseTile(gameManager,player,powerUp.isValidTile(tile));
    }


}
