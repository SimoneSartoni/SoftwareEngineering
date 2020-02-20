package controller;

import model.board.GameManager;
import model.board.TileView;
import model.enums.Direction;
import model.enums.RoomColor;
import model.exceptions.GameException;
import model.player.Player;
import model.player.PlayerView;
import model.weapon.Effect;
import model.weapon.Weapon;

import java.io.Serializable;

public class EffectController implements Serializable {

    /**
     * the method called if the player need to start a new action in current effect
     * @param gameManager the game the player he's in
     * @param player the player who's playing
     */
    public void startingAction(GameManager gameManager, Player player){
       if(getLinkedEffect(gameManager.getCurrentTurn().getCurrentWeapon())==null) {
           gameManager.getCurrentTurn().topEffect().startingExecution(gameManager.getCurrentTurn().topActions().get(0), gameManager, player);
       }
        else {
            if(getLinkedEffect(gameManager.getCurrentTurn().getCurrentWeapon()).equals(gameManager.getCurrentTurn().topEffect()))
                gameManager.getCurrentTurn().topEffect().startingExecution(gameManager.getCurrentTurn().topActions().get(0),gameManager,player);
            else {
                if (getLinkedEffect(gameManager.getCurrentTurn().getCurrentWeapon()).isLinked())
                    gameManager.getCurrentTurn().topActions().get(0).linkedToNextExecute(gameManager.getCurrentTurn().topEffect(), gameManager, player);
                else
                    gameManager.getCurrentTurn().topEffect().startingExecution(gameManager.getCurrentTurn().topActions().get(0), gameManager, player);
            }
       }
    }

    /**
     * method called after the target is selected by a player in a shooting action
     * @param gameManager the game the player is in
     * @param currentPlayer the player that is shooting
     * @param targetPlayer the target PlayerView selected
     * @throws GameException if the PlayerView is not valid
     */
    public void chooseTargetAction (GameManager gameManager, Player currentPlayer, PlayerView targetPlayer) throws GameException{
            gameManager.getCurrentTurn().topActions().get(0).afterChooseTargetExecute(gameManager.getCurrentTurn().topEffect(),gameManager,currentPlayer,gameManager.getCurrentTurn().topEffect().isAValidTarget(gameManager,targetPlayer));
    }

    /**
     *method called after the player has chosen a TileView
     * @param gameManager the game the player is in
     * @param player the player that is shooting
     * @param t1 the TileView selected
     * @throws GameException if the tileView selected is not valid
     */
    public void chooseTileAction(GameManager gameManager, Player player, TileView t1) throws  GameException{
            gameManager.getCurrentTurn().topActions().get(0).afterChooseTileExecute(gameManager.getCurrentTurn().topEffect(),gameManager,player,gameManager.getCurrentTurn().topEffect().isAValidTile(gameManager,t1));
    }

    /**
     * method called after the player has selected a direction
     * @param gameManager the game the player is in
     * @param player the player that is shooting
     * @param direction the direction selected
     * @throws GameException if the direction selected is not valid
     */
    public void chooseDirectionAction(GameManager gameManager,Player player, Direction direction) throws  GameException{
            gameManager.getCurrentTurn().topActions().get(0).afterChooseDirectionExecute(gameManager.getCurrentTurn().topEffect(),gameManager,player,gameManager.getCurrentTurn().topEffect().isAValidDirection(gameManager,direction));
    }

    /**
     * method called after the player has selected a room to target
     * @param gameManager the game he is in
     * @param player the player that's shooting
     * @param room the room selected
     * @throws GameException if the room is not valid
     */
    public void chooseRoomAction(GameManager gameManager, Player player, RoomColor room) throws GameException {
            gameManager.getCurrentTurn().topActions().get(0).afterChooseRoomExecute(gameManager.getCurrentTurn().topEffect(),gameManager,player,gameManager.getCurrentTurn().topEffect().isAValidRoom(gameManager,room));
    }

    /**
     * method called to end the current action
     * @param gameManager the game the player is in
     * @param player the one who's playing
     */
    public void afterExecution(GameManager gameManager,Player player){
       gameManager.getCurrentTurn().topEffect().afterExecution(gameManager.getCurrentTurn().topActions().get(0),gameManager,player);
    }

    /**
     *  method called after the player has selected no powerUps after damage dealt
     * @param player the player who's playing
     * @param gameManager the gama in which the player is contained
     */
    public void noPowerUpSelectedAfterDamage(Player player,GameManager gameManager){
        gameManager.getCurrentTurn().topEffect().afterExecution(gameManager.getCurrentTurn().topActions().get(0),gameManager,player);
    }

    private Effect getLinkedEffect(Weapon weapon){
        for(Effect e:weapon.getOptionalEffect())
            if(e.getChainedToAction()>=0)
                return e;
        return null;
    }
}

