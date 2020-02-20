package model.weapon;

import model.board.GameManager;
import model.board.Tile;
import model.enums.LoadedState;
import model.enums.TurnState;
import model.enums.TypeOfEffect;
import model.exceptions.GameException;
import model.exceptions.WrongChoiceException;
import model.exceptions.WrongTypeException;
import model.player.Player;
import model.utility.Ammo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class representing the weapon element. It's composed of effects that will be executed
 * according to user decisions.
 * */
public class Weapon implements Serializable{
    private transient Logger logger = Logger.getAnonymousLogger();
    /**
     * {@link Effect} representing the base effect of the weapon.
     * It's always != null, and it always has not a cost (cost 0,0,0).
     * It can be used without specific conditions.
     * */
    private Effect baseEffect;
    /**
     * {@link Effect} representing the alternative effect of the weapon.
     * Either the base effect or the alternative effect can be used, both is forbidden.
     * Can be null. Cost can be != 0.
     * */
    private Effect alternativeEffect;
    /**
     * ArrayList of {@link Effect} representing the list of optional effects of the weapon.
     * Except for movement only effects, they can be used only using the base effect too.
     * Can be null. Cost can be != 0.
     * It's empty when weapon has only a base effect or an alternative  effect
     */
    private List<Effect> optionalEffect;
    /**
     * {@link Integer} representing the kind of weapon:
     * 0 normal, 1 optional, 2 alternative
     * */
    private int typeEffect;
    /**
     * {@link Ammo} represeting the cost of a partially loaded weapon.
     * When picked up this is the cost of what to be payed
     * */
    private Ammo partiallyLoadedCost;
    /**
     * {@link Ammo} represeting the color of the weapon
     * It's always just one ammo color cube.
     * Reaload cost + partiallyLoadedCost = cost of a fully charge. To be paid to reload after shooting
     * */
    private Ammo reloadCost;
    /**
     * {@link String} representing the name of the weapon
     * */
    private String idName;
    /**
     * {@link Integer} representing the state of the weapon:
     * 0 weapon unloaded, 1 partially loaded, 2 fully loaded
     * */
    private LoadedState loaded;

    /**
     * Empty constructor
     * */
    public Weapon() {
        optionalEffect = new ArrayList<>();
    }

    /**
     * Constructor for a new weapon giving all the parameters.Builds a weapon with either alternative effect or optional effects
     * @param baseEffect the {@link Effect} which represent the normal use of a weapon. Can not be null.
     * @param alternativeEffect the {@link Effect} which represent, if != null, the alternative effect of the weapon.
     * @param optionalEffect the ArrayList of {@link Effect} which represent the list of all the optional effects of the weapon.
     * @param typeEffect the {@link Integer} representing the kind of weapon.
     * @param partiallyLoadedCost the {@link Ammo} representing the cost of a partially loaded weapon. To be paid to pick it up.
     * @param reloadCost the {@link Ammo} representing the color of the weapon. partiallyLoadedCost + reloadCost = ammos to be paid for a full charge.
     * @param idName the {@link String} representing the name of the weapon
     * @param loaded the {@link Integer} representing the state of the weapon
     */
    public Weapon(Effect baseEffect, Effect alternativeEffect, List<Effect> optionalEffect, int typeEffect, Ammo partiallyLoadedCost, Ammo reloadCost, String idName, LoadedState loaded) {
        this.baseEffect = baseEffect;
        this.alternativeEffect = alternativeEffect;
        this.optionalEffect = new ArrayList<>();
        this.optionalEffect.addAll(optionalEffect);
        this.typeEffect = typeEffect;
        this.partiallyLoadedCost = partiallyLoadedCost;
        this.reloadCost = reloadCost;
        this.idName = idName;
        this.loaded = loaded;
    }

    /**
     * Constructor for a new weapon giving some of the parameters.
     * @param typeEffect the {@link Integer} representing the kind of weapon.
     * @param partiallyLoadedCost the {@link Ammo} representing the cost of a partially loaded weapon. To be paid to pick up it.
     * @param reloadCost the {@link Ammo} representing the color of the weapon. partiallyLoadedCost + reloadCost = ammos to be paid for a full charge.
     * @param idName the {@link String} representing the name of the weapon
     * @param loaded the {@link Integer} representing the state of the weapon
     * */
    public Weapon(int typeEffect, Ammo partiallyLoadedCost, Ammo reloadCost, String idName, LoadedState loaded) {
        this.typeEffect = typeEffect;
        this.partiallyLoadedCost = partiallyLoadedCost;
        this.reloadCost = reloadCost;
        this.idName = idName;
        this.loaded = loaded;
        optionalEffect = new ArrayList<>();
    }


    /**
     *
     * @param player the player to notify
     * @param effects the list of effect he can choose
     */

    public void notifyEffects(Player player,List<Effect> effects,GameManager gameManager){
        player.setTurnState(TurnState.CHOOSE_EFFECT);
        player.notifyPrintHelp(gameManager);
        player.getViewPlayer().onEffects(effects);
    }

    /**
     *
     * @param player the player to notify
     * @param typeOfEffects the list of types of effect that can be chosen
     */
    public void notifyTypeOfEffect(Player player,List<TypeOfEffect> typeOfEffects,GameManager gameManager){
        player.setTurnState(TurnState.CHOOSE_TYPE_OF_EFFECT);
        player.notifyPrintHelp(gameManager);
        player.getViewPlayer().onTypeEffects(typeOfEffects);
    }

    public Effect getBaseEffect() {
        return baseEffect;
    }

    public void setBaseEffect(Effect baseEffect) {
        try {
            this.baseEffect = baseEffect;
        }
        catch (NullPointerException e) {
            logger.log(Level.SEVERE, e.toString());
        }
    }

    public Effect getAlternativeEffect() {
        return alternativeEffect;
    }

    public void setAlternativeEffect(Effect alternativeEffect) {
        this.alternativeEffect = alternativeEffect;
    }

    public List<Effect> getOptionalEffect() {
        return optionalEffect;
    }

    public void setOptionalEffect(List<Effect> optionalEffect) {
        this.optionalEffect = new ArrayList<>();
        this.optionalEffect.addAll(optionalEffect);
    }

    public int getTypeEffect() {
        return typeEffect;
    }


    public Ammo getPartiallyLoadedCost() {
        return partiallyLoadedCost;
    }

    /**
     *
     * @return the full reload cost of the Weapon
     */
    public Ammo getReloadCost() {
        Ammo ret=new Ammo(0,0,0);
        ret.setRedValue(reloadCost.getRedValue()+partiallyLoadedCost.getRedValue());
        ret.setYellowValue(reloadCost.getYellowValue()+partiallyLoadedCost.getYellowValue());
        ret.setBlueValue(reloadCost.getBlueValue()+partiallyLoadedCost.getBlueValue());
        return ret;
    }

    public String getIdName() {
        return idName;
    }

    public LoadedState getLoaded() {
        return loaded;
    }

    public void setLoaded(LoadedState loaded) {
        this.loaded = loaded;
    }

/**
 * this weapon checks if there's a possible path using the effects in which the player can actually shoot
 * the onlyMovement effects are simulated to achieve the right answer anytime
 * @param actingPlayer: the player that has the weapon
 * @param gameManager: the general structure of the game
 * @return true if it's possible to shoot with this weapon, false otherwise
 **/
    public boolean isValid(Player actingPlayer,GameManager gameManager) {
        boolean temp = actingPlayer.getWeapons().contains(this) &&  loaded == LoadedState.LOADED;
        boolean baseEffectValid = isAValidEffect(baseEffect,gameManager,actingPlayer);
        boolean alternativeEffectValid = alternativeEffect != null && isAValidEffect(alternativeEffect,gameManager,actingPlayer);
        Tile tile=actingPlayer.getCurrentTile();
        tile.removePlayer(actingPlayer);
        if (!baseEffectValid) {
            for (Effect e : optionalEffect)
                if (e.isOnlyMovement() && e.isCanBeUsed()&&actingPlayer.getPotentialAmmos().hasCorrectCost(e.getCost()))
                    for (Tile t : gameManager.getPossibleTiles(e.getActions().get(0).getMinAmount(), e.getActions().get(0).getMaxAmount(), tile)) {
                        actingPlayer.setCurrentTile(t);
                        t.addPlayer(actingPlayer);
                        if (isAValidEffect(baseEffect, gameManager, actingPlayer)) {
                            baseEffectValid = true;
                            baseEffect.setCanBeUsed(true);
                        }
                        t.removePlayer(actingPlayer);
                    }
        }
        actingPlayer.setCurrentTile(tile);
        tile.addPlayer(actingPlayer);
        if(!baseEffectValid && !alternativeEffectValid)
            return false;
        return temp;
    }


    /**
     *
     * @param gameManager the current Game
     * @param p the current player
     * @return the List of Type of Effects tha can be chosen
     */
    public List<TypeOfEffect> getPossibleTypeOfEffects(GameManager gameManager,Player p){
        List <TypeOfEffect> ret= new ArrayList<>();
       if((baseEffect.isCanBeUsed())&&(!baseEffect.isExecuted())&&(isAValidEffect(baseEffect,gameManager,p)))
           ret.add(TypeOfEffect.BASE);
       if(alternativeEffect != null && alternativeEffect.isCanBeUsed() && !alternativeEffect.isExecuted() && isAValidEffect(alternativeEffect,gameManager,p))
            ret.add(TypeOfEffect.ALTERNATIVE);
        if(!getPossibleOptionalEffects(gameManager,p).isEmpty())
           ret.add(TypeOfEffect.OPTIONAL);
        return ret;
    }

    /**
     * this method is called when the player selects a weapon to shoot. It checks if there are more types of effects available.
     * If only base or alternative are available it will automatically proceed to start the one that is possible.
     * If only Optional effects are available then the effect has to chosen by the player. If more then 1 type of effect is available then
     * the type to be used has to be chosen by the player
     * @param gameManager the current Game
     * @param player the current Player
     */
    public void beforeBaseEffect(GameManager gameManager,Player player){
        if(getPossibleTypeOfEffects(gameManager,player).size()>1){
            gameManager.getCurrentTurn().getPossibleChoices().setSelectableEffects(getPossibleTypeOfEffects(gameManager,player));
            notifyTypeOfEffect(player,gameManager.getCurrentTurn().getPossibleChoices().getSelectableEffects(),gameManager);
        }
        else{
            if(getPossibleTypeOfEffects(gameManager,player).size()==1){
                if(getPossibleTypeOfEffects(gameManager,player).get(0) == TypeOfEffect.BASE){
                    baseEffectExecute(gameManager, player);
                }
                else{
                    if(getPossibleTypeOfEffects(gameManager,player).get(0) == TypeOfEffect.ALTERNATIVE)
                        alternativeEffectExecute(gameManager, player);
                    else {
                        afterChooseTypeOfEffect(gameManager,player);
                    }
                }
            }
        }
     }

    /**
     *
     * @param typeOfEffect the typeOfEffect chosen by the user
     * @param gameManager the current Game
     * @param player the current Player
     * @return true if the effect is selectable
     * @throws GameException thrown if the effect is not one of the selectable
     */
    public TypeOfEffect isAValidTypeOfEffect(TypeOfEffect typeOfEffect,GameManager gameManager,Player player) throws GameException {
        for(TypeOfEffect typeOfEffect1:gameManager.getCurrentTurn().getPossibleChoices().getSelectableEffects())
            if(typeOfEffect==typeOfEffect1)
                return typeOfEffect1;
        throw  new WrongTypeException(typeOfEffect.toString());
    }

    /**
     * method used to notify the client about what optional effect he wants to use
     * @param gameManager the current Game
     * @param p the player to be notified
     */
    public void afterChooseTypeOfEffect(GameManager gameManager,Player p){
                gameManager.getCurrentTurn().getPossibleChoices().setSelectableOptionalEffects(getPossibleOptionalEffects(gameManager,p));
                notifyEffects(p,gameManager.getCurrentTurn().getPossibleChoices().getSelectableOptionalEffects(),gameManager);
        }

    /**
     * this method initializes all the structure in CurrentTurn that are used in the effect computation. Then it starts the base effect computation
     * @param gameManager the current Game
     * @param player the current Player
     */
    public void baseEffectExecute(GameManager gameManager,Player player){
        gameManager.getCurrentTurn().setCurrentPlayerForVisibility(player);
        gameManager.getCurrentTurn().pushEffect(baseEffect, baseEffect.getActions());
        gameManager.getCurrentTurn().getAlreadyHitPlayer().put(baseEffect,new ArrayList<>());
        gameManager.getCurrentTurn().getAlreadyHitTile().put(baseEffect,new ArrayList<>());
        if(alternativeEffect != null)
            alternativeEffect.setCanBeUsed(false);
        baseEffect.payCost(gameManager,player);

    }

    /**
     * this method initializes all the structure in CurrentTurn that are used in the effect computation. Then it starts the alternative effect computation
     * @param gameManager the current Game
     * @param player the current Player
     */
    public void alternativeEffectExecute(GameManager gameManager, Player player){
        gameManager.getCurrentTurn().setCurrentPlayerForVisibility(player);
        gameManager.getCurrentTurn().pushEffect(alternativeEffect, alternativeEffect.getActions());
        gameManager.getCurrentTurn().getAlreadyHitPlayer().put(alternativeEffect,new ArrayList<>());
        gameManager.getCurrentTurn().getAlreadyHitTile().put(alternativeEffect,new ArrayList<>());
        baseEffect.setCanBeUsed(false);
        alternativeEffect.payCost(gameManager,player);
    }

    /**
     * method called after the complete execution of an effect
     * if no more effects are available, it will automatically proceed to end the shooting action.
     * if some effects are available, then it will notify the client to choose the next effect
     * @param e the effect that has been completed
     * @param gameManager the current Game
     * @param player the current Player
     */
    public void afterEffect(Effect e,GameManager gameManager,Player player){
        setCanBeUsedEffects(e);
        if(getPossibleTypeOfEffects(gameManager,player).isEmpty()) {
            clear(gameManager, player);
        }
        else{
            if((getPossibleTypeOfEffects(gameManager,player).size()==1) && (getPossibleTypeOfEffects(gameManager,player).get(0)==TypeOfEffect.OPTIONAL)) {
                player.setTurnState(TurnState.CHOOSE_EFFECT);
                gameManager.getCurrentTurn().getPossibleChoices().setSelectableOptionalEffects(getPossibleOptionalEffects(gameManager, player));
                notifyEffects(player, gameManager.getCurrentTurn().getPossibleChoices().getSelectableOptionalEffects(),gameManager);
            }
            else {
                player.setTurnState(TurnState.CHOOSE_TYPE_OF_EFFECT);
                gameManager.getCurrentTurn().getPossibleChoices().setSelectableEffects(getPossibleTypeOfEffects(gameManager,player));
                notifyTypeOfEffect(player,gameManager.getCurrentTurn().getPossibleChoices().getSelectableEffects(),gameManager);
            }
        }
    }

    public void afterBaseEffect(GameManager gameManager,Player player){
        if (alternativeEffect!=null){
            alternativeEffect.setCanBeUsed(false);}
        else {
            for (Effect e: optionalEffect){
                if(e.getChainedTo().equals(baseEffect)){
                    e.setCanBeUsed(true);}
        }}
        if(getPossibleOptionalEffects(gameManager,player).isEmpty())
            clear(gameManager,player);
        else{
        player.setTurnState(TurnState.CHOOSE_EFFECT);
        gameManager.getCurrentTurn().getPossibleChoices().setSelectableOptionalEffects(getPossibleOptionalEffects(gameManager,player));
        notifyEffects(player,gameManager.getCurrentTurn().getPossibleChoices().getSelectableOptionalEffects(),gameManager);}
    }

    public void optionalEffectExecute(Effect optionalEffect, GameManager gameManager, Player player){
        gameManager.getCurrentTurn().pushEffect(optionalEffect, optionalEffect.getActions());
        gameManager.getCurrentTurn().getAlreadyHitPlayer().put(optionalEffect,new ArrayList<>());
        gameManager.getCurrentTurn().getAlreadyHitTile().put(optionalEffect,new ArrayList<>());
        if (optionalEffect.getVisibility().isChainSee())
            gameManager.getCurrentTurn().setCurrentPlayerForVisibility(gameManager.getCurrentTurn().getAlreadyHitPlayer().get(optionalEffect.getChainedTo()).get(0));
        optionalEffect.payCost(gameManager,player);
    }

    public void setCanBeUsedEffects(Effect e){
        for(Effect effect:getChainedEffect(e))
            effect.setCanBeUsed(true);
    }

    public List<Effect> getChainedEffect(Effect e){
        List <Effect> effects=new ArrayList<>();
        for(Effect effect:optionalEffect){
            if(effect.getChainedTo()!=null)
                if(effect.getChainedTo().equals(e))
                    effects.add(effect);
        }
        return effects;
    }

    /**
     * this effect returns true if it has at least one possible target. Returns true also if it's an only movement effect and has a position in which the Base Effect can be executed
     * @param effect the effect to be analysed
     * @param gameManager the current Game
     * @param player the current Player
     * @return true if it's possible to use the effect, false otherwise
     */
    public boolean isAValidEffect(Effect effect, GameManager gameManager,Player player){
        Tile tile=player.getCurrentTile();
        if (effect.isOnlyMovement()&& effect.isCanBeUsed()&& !effect.isExecuted()) {
            if (baseEffect.isExecuted()&&player.getPotentialAmmos().hasCorrectCost(effect.getCost())) {
                return true;
            }
            else {
                tile.removePlayer(player);
                for (Tile t : gameManager.getPossibleTiles(effect.getActions().get(0).getMinAmount(), effect.getActions().get(0).getMaxAmount(), tile)) {
                    player.setCurrentTile(t);
                    t.addPlayer(player);
                    if (baseEffect.isValid(gameManager,player) &&player.getPotentialAmmos().hasCorrectCost(effect.getCost())){
                        player.setCurrentTile(tile);
                        t.removePlayer(player);
                        tile.addPlayer(player);
                        return true;
                    }
                    t.removePlayer(player);
                }
                tile.addPlayer(player);
                player.setCurrentTile(tile);
                return false;
            }
        }
        if(effect.isValid(gameManager,player)&& !effect.isExecuted()&& effect.isCanBeUsed())
            return true;
        return false;
    }

    public Effect isAChoosableEffect(Effect effect,GameManager gameManager) throws  GameException{
        for(Effect effect1:gameManager.getCurrentTurn().getPossibleChoices().getSelectableOptionalEffects())
            if(effect1.equals(effect))
                return effect1;
        throw  new WrongChoiceException("wrong effect");
    }

    /**
     * returns all the optional effects that can be used in a specific moment of the turn
     * @param gameManager the current game
     * @param currentPlayer the current player
     * @return
     */
    public List<Effect> getPossibleOptionalEffects(GameManager gameManager, Player currentPlayer){
        List<Effect> ret= new ArrayList<>();
        for(Effect e: optionalEffect){
                if(e.isCanBeUsed() && isAValidEffect(e, gameManager, currentPlayer) && !e.isExecuted()) {
                    ret.add(e);
                }
            }
        return ret;
    }

     /**
     * this method sets all the Weapon and Effect for a new use. Clears the current Turn in GameManager
     * @param gameManager  the current game
     * @param currentPlayer the current Player
     */
    public void clear(GameManager gameManager,Player currentPlayer) {
        for (Player p : gameManager.getCurrentTurn().getMarks().keySet())
            p.addMarks(gameManager.getCurrentTurn().getMarks().get(p), currentPlayer, gameManager);
        gameManager.getCurrentTurn().resetAfterWeapon();
        baseEffect.setCanBeUsed(true);
        baseEffect.setExecuted(false);
        baseEffect.getAlreadyInteractedPlayers().clear();
        if (alternativeEffect != null) {
            alternativeEffect.setExecuted(false);
            alternativeEffect.setCanBeUsed(true);
            alternativeEffect.getAlreadyInteractedPlayers().clear();
        }
        for (Effect e : optionalEffect) {
            e.setExecuted(false);
            e.getAlreadyInteractedPlayers().clear();
            if (e.isOnlyMovement()) {
                e.setCanBeUsed(true);
            }
            else {
                e.setCanBeUsed(false);
            }
        }
            currentPlayer.handleActions(gameManager);

    }

    public List<String> printStatus() {
        List<String> res = new ArrayList<>();
        res.add("Weapon name: " + idName);
        //todo
        // carmelo
        // complete this
        return res;
    }

    @Override
    public boolean equals(Object obj) {
        Weapon w;
        if(obj instanceof Weapon){
            w=(Weapon)obj;
        return w.getIdName().equals(idName);
    }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
