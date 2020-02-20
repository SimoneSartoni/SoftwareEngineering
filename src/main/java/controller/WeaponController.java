package controller;

import model.board.GameManager;
import model.enums.TypeOfEffect;
import model.exceptions.GameException;
import model.player.Player;
import model.weapon.Effect;
import model.weapon.Weapon;

import java.io.Serializable;

public class WeaponController implements Serializable {
    /**
     * method called when the base effect is selected
     * @param gameManager the game the player is in
     * @param p the player that's shooting
     * @param w the Weapon selected to shoot
     * @throws GameException if the BaseEffect cant be executed
     */
    public void baseEffect(GameManager gameManager, Player p, Weapon w) throws GameException{
            w.isAValidTypeOfEffect(TypeOfEffect.BASE,gameManager,p);
            w.baseEffectExecute(gameManager,p);
    }

    /**
     * method called when the Alternative Effect is selected
     * @param gameManager the game the player is in
     * @param p the player that's shooting
     * @param w the Weapon selected to shoot
     * @throws GameException if the AlternativeEffect cant be executed
     */
    public void alternativeEffect(GameManager gameManager, Player p,Weapon w) throws GameException {
            w.isAValidTypeOfEffect(TypeOfEffect.ALTERNATIVE,gameManager,p);
            w.alternativeEffectExecute(gameManager,p);
    }

    /**
     * method called when the Optional type Effect is selected
     * @param gameManager the game the player is in
     * @param player the player that's shooting
     * @param weapon the Weapon selected to shoot
     * @throws GameException if the optionalEffects cant be chosen
     */
    public void optionalTypeChosen(GameManager gameManager,Player player,Weapon weapon) throws GameException{
            weapon.isAValidTypeOfEffect(TypeOfEffect.OPTIONAL,gameManager,player);
            weapon.afterChooseTypeOfEffect( gameManager, player);
    }

    /**
     * method called when the base effect is selected
     * @param gameManager the game the player is in
     * @param p the player that's shooting
     * @param w the Weapon selected to shoot
     * @throws GameException if the single optional effect can't be executed
     */
    public void optionalEffect(Effect optionalChosen, GameManager gameManager, Player p,Weapon w) throws GameException{
            w.optionalEffectExecute(w.isAChoosableEffect(optionalChosen,gameManager),gameManager,p);
    }

    /**
     * method called after the shooting action is finished, when no more effects are chosen by the user
     * @param gameManager the game the player is in
     * @param player the player is shooting
     * @param weapon the weapon the player is shooting with
     */
    public void clear(GameManager gameManager,Player player,Weapon weapon){
        weapon.clear(gameManager,player);
    }

}
