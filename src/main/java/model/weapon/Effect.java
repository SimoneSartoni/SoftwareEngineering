package model.weapon;

import model.board.GameManager;
import model.board.Tile;
import model.board.TileView;
import model.enums.Direction;
import model.enums.ForcedMovement;
import model.enums.RoomColor;
import model.enums.TurnState;
import model.exceptions.*;
import model.player.Player;
import model.player.PlayerView;
import model.utility.Ammo;
import model.utility.TurnStateHandler;
import model.utility.Visibility;
import model.weapon.actions.Action;
import model.weapon.actions.DamageAction;
import model.weapon.actions.MarkAction;
import model.weapon.actions.MovementAction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Effect implements Serializable {
    private String name;
    private boolean executed;
    /**
     * the list of all actions
     */
    private List<Action> actions;
    /**
     * true if targets can be only different target from the already hit ones (except for the additional damage)
     */
    private boolean newTarget;
    private int nOfOptionalTargets;
    private int alreadyTarget;
    private Ammo cost;
    /**
     * used to see if the effect can be used in a specific moment
     */
    private boolean canBeUsed;
    /**
     * used in linked to next actions
     */
    private List<List<Player>> alreadyInteractedPlayers;
    /**
     * the list of possible players that can be hit in the effect in a specific moment
     */
    public List<Player> effectTargettable;
    /**
     * true if he effect is linked to an action
     */
    private boolean isLinked;
    /**
     * if and only if the chained effect has been executed
     */
    private Effect chainedTo;
    /**
     * this attribute indicates the visibility while shooting in this particular effect
     */
    private Visibility visibility;

    /**
     * this attribute indicates the number of the actions with which the effect is chained to
     * 0 if it is chained to all the effect (see "Grenade Launcher" rules)
     */
    private int chainedToAction;

    /**
     * true if this effect contains only MovementActions
     */
    private boolean onlyMovement;
    private boolean throughWalls;
    private boolean afterBaseLinked;

    public Effect() {

    }

    /**
     * the effect is constructed and filled in the parsing process
     *
     * @param name               name of the effect
     * @param executed           true if the effect has been executed
     * @param actions            list of all Actions in the effect
     * @param newTarget     number of players to be targeted
     * @param nOfOptionalTargets number of Optional Targets; it's up to the client to decide if he wants to target them or not
     * @param alreadyTarget      number of Targets to be targeted that has been already hit in a previous effect

     */

    public Effect(String name, boolean executed, List<Action> actions, boolean newTarget, int nOfOptionalTargets,
                  int alreadyTarget, Effect chainedTo, Visibility visibility, Ammo cost, int chainedToAction,
                  boolean onlyMovement, boolean throughWalls,boolean afterBaseLinked) {
        this.name = name;
        this.executed = executed;
        this.actions = new ArrayList<>();
        this.actions.addAll(actions);
        this.newTarget = newTarget;
        this.nOfOptionalTargets = nOfOptionalTargets;
        this.alreadyTarget = alreadyTarget;
        this.chainedTo = chainedTo;
        this.visibility = visibility;
        this.cost = cost;
        this.chainedToAction = chainedToAction;
        this.onlyMovement = onlyMovement;
        this.alreadyInteractedPlayers = new ArrayList<>();
        this.throughWalls = throughWalls;
        this.effectTargettable=new ArrayList<>();
        this.afterBaseLinked=afterBaseLinked;
    }

    public String getName() {
        return name;
    }


    public boolean isExecuted() {
        return executed;
    }

    public void setExecuted(boolean executed) {
        this.executed = executed;
    }

    public List<Action> getActions() {
        return actions;
    }

    public boolean isNewTarget() {
        return newTarget;
    }

    public int getNOfOptionalTargets() {
        return nOfOptionalTargets;
    }

    public int getAlreadyTarget() {
        return alreadyTarget;
    }

    public Effect getChainedTo() {
        return chainedTo;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public boolean isAfterBaseLinked() {
        return afterBaseLinked;
    }

    public void setLinked(boolean linked) {
        isLinked = linked;
    }

    public boolean isLinked() {
        return isLinked;
    }
    public Ammo getCost() {
        return cost;
    }

    public void setChainedTo(Effect chained) {
        this.chainedTo = chained;
    }

    public int getChainedToAction() {
        return chainedToAction;
    }

    public void setChainedToAction(int setChainedToAction) {
        this.chainedToAction = setChainedToAction;
    }

    public boolean isOnlyMovement() {
        return onlyMovement;
    }

    public boolean isCanBeUsed() {
        return canBeUsed;
    }

    public void setCanBeUsed(boolean canBeUsed) {
        this.canBeUsed = canBeUsed;
    }

    public boolean isThroughWalls(){
        return throughWalls;
    }

    public List<List<Player>> getAlreadyInteractedPlayers() { return alreadyInteractedPlayers; }

    /**
     * method to check if it's possible ,using this Effect, to do a valid shooting action with the current Weapon
     * it checks if the player has the right ammos to pay for the cost and if it has at least one valid target to hit
     * @param gameManager the currentGame
     * @param player the player that he's trying to shoot
     * @return
     */
    public boolean isValid(GameManager gameManager, Player player) {
        effectTargettable.clear();
        boolean temp = player.getPotentialAmmos().hasCorrectCost(cost) && canBeUsed;
        List<Player> ret;
        if (! visibility.isChainSee()) {
            ret = visibility.getTargettablePlayers(gameManager, player, this);
            visibility.removeNotValidPlayers(gameManager, ret, player, visibility.getMinMov(), visibility.getMaxMov(), this);
        } else {
            ret = visibility.getTargettablePlayers(gameManager, gameManager.getCurrentTurn().getAlreadyHitPlayer().get(chainedTo).get(0), this);
            ret.remove(gameManager.getCurrentTurn().getAlreadyHitPlayer().get(chainedTo).get(0));
            ret=visibility.removeNotValidPlayers(gameManager, ret, gameManager.getCurrentTurn().getAlreadyHitPlayer().get(chainedTo).get(0), visibility.getMinMov(), visibility.getMaxMov(), this);
        }
        effectTargettable.addAll(ret);
        if(ret.isEmpty())
            return false;
        return temp;
    }

    /**
        this method notifies the current player about the tile he can choose
     */
    public void notifyTiles(List<Tile> targettableTiles, GameManager gameManager, Player currentPlayer){
        if(targettableTiles.isEmpty())
            afterExecution(gameManager.getCurrentTurn().topActions().get(0),gameManager,currentPlayer);
        else {
            gameManager.getCurrentTurn().getPossibleChoices().setSelectableTiles(targettableTiles);
            currentPlayer.setTurnState(TurnState.CHOOSE_TILE_FOR_WEAPON_ACTION);
            currentPlayer.getViewPlayer().onPrintHelp(gameManager,currentPlayer);
            currentPlayer.getViewPlayer().onTiles(GameManager.tilesToTileViews(targettableTiles));
        }
    }

    /**
     this method notifies the current player about the target he can choose
     */
    public void notifyTargets(List<Player> targettableTargets, GameManager gameManager, Player currentPlayer){
        if(targettableTargets.isEmpty())
            afterExecution(gameManager.getCurrentTurn().topActions().get(0),gameManager,currentPlayer);
        else {
            gameManager.getCurrentTurn().getPossibleChoices().setSelectableTargets(targettableTargets);
            currentPlayer.setTurnState(TurnState.CHOOSE_TARGET);
            currentPlayer.getViewPlayer().onPrintHelp(gameManager,currentPlayer);
            currentPlayer.getViewPlayer().onTargets(playersToPlayerViews(targettableTargets));
        }
    }

    private List<PlayerView> playersToPlayerViews(List<Player> playerList) {
        List<PlayerView> ret= new ArrayList<>();
        for(Player p: playerList){
            PlayerView pV = p.createPlayerView();
            pV.setTileView(GameManager.createTileView(p.getCurrentTile()));
            ret.add(pV);
        }
        return ret;
    }

    /**
     this method notifies the current player about the rooms he can choose
     */
    public void notifyRooms(List<RoomColor> targettableRoomColors, GameManager gameManager, Player currentPlayer){
        if(targettableRoomColors.isEmpty())
            afterExecution(gameManager.getCurrentTurn().topActions().get(0),gameManager,currentPlayer);
        else {
            gameManager.getCurrentTurn().getPossibleChoices().setSelectableRooms(targettableRoomColors);
            currentPlayer.setTurnState(TurnState.CHOOSE_ROOM);
            currentPlayer.getViewPlayer().onPrintHelp(gameManager,currentPlayer);
            currentPlayer.getViewPlayer().onRooms(targettableRoomColors);
        }
    }

    /**
     this method notifies the current player about the directions he can choose
     */
    public void notifyDirections(List<Direction> targettableDirections, GameManager gameManager, Player currentPlayer){
        if(targettableDirections.isEmpty())
            afterExecution(gameManager.getCurrentTurn().topActions().get(0),gameManager,currentPlayer);
        else {
            gameManager.getCurrentTurn().getPossibleChoices().setSelectableDirections(targettableDirections);
            currentPlayer.setTurnState(TurnState.CHOOSE_DIRECTION);
            currentPlayer.getViewPlayer().onPrintHelp(gameManager,currentPlayer);
            currentPlayer.getViewPlayer().onDirections(targettableDirections);
        }
    }

    /**
     this method notifies the current player about the powerUp he can use during the effect
     */
    public void notifyPowerUps(GameManager gameManager, Player currentPlayer, TurnState turnState){
            currentPlayer.setTurnState(turnState);
            gameManager.getCurrentTurn().getPossibleChoices().setSelectablePowerUps(currentPlayer.getValidPowerUps(gameManager));
            currentPlayer.getViewPlayer().onPrintHelp(gameManager,currentPlayer);
            currentPlayer.getViewPlayer().onPowerUps(gameManager.getCurrentTurn().getPossibleChoices().getSelectablePowerUps());
    }

    /**
     this method notifies the current player about the powerUps he can choose to pay the cost
     */
    public void notifyPowerupsForCost(GameManager gameManager, Player currentPlayer, TurnState turnState){
        currentPlayer.setTurnState(turnState);
        currentPlayer.getViewPlayer().onPrintHelp(gameManager,currentPlayer);
        currentPlayer.getViewPlayer().onPowerUps(gameManager.getCurrentTurn().getPossibleChoices().getSelectablePowerUps());
    }

    /**
     this method notifies the current player about the linked effect he can choose
     */
    public void notifyLinkedEffect(GameManager gameManager, Player currentPlayer, Action action) {
        List<Effect> effects = new ArrayList<>();
        effects.add(action.getLinkedEffect());
        currentPlayer.setTurnState(TurnState.CHOOSE_LINKED_EFFECT);
        gameManager.getCurrentTurn().getPossibleChoices().setSelectableOptionalEffects(effects);
        currentPlayer.notifyPrintHelp(gameManager);
        currentPlayer.getViewPlayer().onEffects(gameManager.getCurrentTurn().getPossibleChoices().getSelectableOptionalEffects());
    }


    /**
     * method called at the beginning of the effect to pay the corresponding cost.
     * If player has valid powerUps to discard for the cost  then he makes the player choose among one of them
     */
    public void payCost(GameManager gameManager, Player currentPlayer){
       if (currentPlayer.getPowerUpToDiscardForCost(cost,gameManager).isEmpty()){
           currentPlayer.payCost(gameManager,cost);
           startingExecution(actions.get(0),gameManager,currentPlayer);
       }
       else {
           gameManager.getCurrentTurn().getPossibleChoices().setSelectablePowerUps(currentPlayer.getPowerUpToDiscardForCost(cost,gameManager));
           currentPlayer.notifyOnLog("Choose a powerup to discard to pay Effect Cost");
           notifyPowerupsForCost(gameManager,currentPlayer,TurnState.DISCARD_POWERUP_FOR_COST_EFFECT);
       }
    }

    /**
     * visitor pattern calling the current action (unknown type)
     * @param action
     * @param gameManager
     * @param currentPlayer
     */
    public void startingExecution(Action action, GameManager gameManager, Player currentPlayer) {
        action.initialExecute(this, gameManager, currentPlayer );
    }

    /**
     * method called after the visit. Here it's known the type of the currentAction it's going to be executed
     * It takes the valid target that can be hit from the visibility methods. After that, it requests to the client
     * the correct targets:tiles, rooms, directions or players.
     * @param damageAction the type of new Action
     * @param gameManager the current Game
     * @param currentPlayer the current Player
     */
    public void startAction(DamageAction damageAction, GameManager gameManager, Player currentPlayer) {
        gameManager.getCurrentTurn().setCurrentAction(damageAction);
        List<Direction> selectableDirections;
        alreadyInteractedPlayers.add(new ArrayList<Player>());
        List<Player> selectablePlayers = visibility.getTargettablePlayers(gameManager,gameManager.getCurrentTurn().getCurrentPlayerForVisibility(), this);
        selectablePlayers=visibility.removeNotValidPlayers(gameManager,selectablePlayers,gameManager.getCurrentTurn().getCurrentPlayerForVisibility(),damageAction.getMinDist(),damageAction.getMaxDist(), this);
        selectablePlayers=visibility.removeNotValidActionPlayer(gameManager,selectablePlayers,damageAction,currentPlayer,this);
        if(visibility.isChainedToTake())
            selectablePlayers.add(gameManager.getCurrentTurn().getCurrentPlayerForVisibility());
        if(visibility.isRangeDist()){
            for(Player p:selectablePlayers) {
                handleDamage(gameManager, damageAction, p);
                TurnStateHandler.manageAddCounterAttackingQueue(p, gameManager);
            }
            if(gameManager.getCurrentTurn().getCounterAttackingPlayers().size()==0) {
                currentPlayer.setTurnState(TurnState.CHOOSE_AFTER_DAMAGE_POWERUP);
                if (currentPlayer.hasValidPowerUp(gameManager)) {
                    notifyPowerUps(gameManager, currentPlayer, TurnState.CHOOSE_AFTER_DAMAGE_POWERUP);
                    return;
                }
                else{
                    currentPlayer.setTurnState(TurnState.DISCARD_POWERUP_FOR_COST_EFFECT);
                    afterExecution(damageAction,gameManager,currentPlayer);
                    return;
                }
            }
            else{
                currentPlayer.setTurnState(TurnState.CAN_BE_COUNTER_ATTACKED);
                currentPlayer.notifyOnLog("Waiting for counterAttacking players...");
                return;
            }
        }
        if (visibility.isStraightForward()) {
            if (gameManager.getCurrentTurn().getDirection() == null) {
                selectableDirections = visibility.getTargettableDirections(gameManager.getCurrentTurn().getCurrentPlayerForVisibility(), gameManager, this);
                selectableDirections=visibility.removeNotReachableDirections(gameManager,selectableDirections,gameManager.getCurrentTurn().getCurrentPlayerForVisibility(), this);
                notifyDirections(selectableDirections, gameManager, currentPlayer);
            } else {
                afterChooseDirectionAction(damageAction, gameManager, gameManager.getCurrentTurn().getDirection(), currentPlayer);
            }
        } else {
            if (visibility.isAnotherRoom()) {
                List<RoomColor> targettableRoomColors = visibility.getTargettableRoomColors( gameManager.getCurrentTurn().getCurrentPlayerForVisibility(),selectablePlayers);
                notifyRooms(targettableRoomColors, gameManager, currentPlayer);
            } else {
                if (damageAction.isAoe()) {
                    List<Tile> targettableTiles = visibility.getTargettableTiles(gameManager.getCurrentTurn().getCurrentPlayerForVisibility(),selectablePlayers);
                    if(!targettableTiles.isEmpty())
                        currentPlayer.notifyOnLog("choose a tile where you want to deal damage");
                    notifyTiles(targettableTiles, gameManager, currentPlayer);
                } else {
                    gameManager.getCurrentTurn().getPossibleChoices().setSelectableTargets(selectablePlayers);
                    if(!selectablePlayers.isEmpty())
                        currentPlayer.notifyOnLog("choose a target to hit");
                    notifyTargets(selectablePlayers, gameManager, currentPlayer);
                }
            }
        }
    }

    /**
     * method called after the visit. Here it's known the type of the currentAction it's going to be executed.
     * It checks all the parameters that are needed to take the right choice about what the player has to do next:
     * if he needs to choose a target to move or he needs to move himself
     * @param movementAction the current action
     * @param gameManager
     * @param currentPlayer
     */
    public void startAction (MovementAction movementAction, GameManager gameManager, Player currentPlayer){
        gameManager.getCurrentTurn().setCurrentAction(movementAction);
        alreadyInteractedPlayers.add(new ArrayList<Player>());
        List<Player> targettablePlayers = new ArrayList<>();
        List<Tile> targettableTiles = new ArrayList<>();
        Tile temp=currentPlayer.getCurrentTile();
        if(isOnlyMovement()){
            targettableTiles = gameManager.getPossibleTiles(1, movementAction.getMaxAmount(), temp);
            if(!gameManager.getCurrentTurn().getCurrentWeapon().getBaseEffect().isExecuted()) {
                temp.removePlayer(currentPlayer);
                List<Tile> temp1= new ArrayList<>(targettableTiles);
                for (Tile t : temp1){
                    t.addPlayer(currentPlayer);
                    currentPlayer.setCurrentTile(t);
                    if (!gameManager.getCurrentTurn().getCurrentWeapon().getBaseEffect().isValid(gameManager, currentPlayer))
                        targettableTiles.remove(t);
                    t.removePlayer(currentPlayer);
                }
                currentPlayer.setCurrentTile(temp);
                temp.addPlayer(currentPlayer);
            }
            if(!targettableTiles.isEmpty())
                currentPlayer.notifyOnLog("choose a tile where you want to move before dealing damage");
            notifyTiles(targettableTiles,gameManager,currentPlayer);
            return;
        }

        if (visibility.isStraightForward()) {
            if (gameManager.getCurrentTurn().getDirection() == null) {
                List<Direction> targettableDirections;
                targettableDirections = visibility.getTargettableDirections(gameManager.getCurrentTurn().getCurrentPlayerForVisibility(),gameManager,this);
                targettableDirections=visibility.removeNotReachableDirections(gameManager,targettableDirections,gameManager.getCurrentTurn().getCurrentPlayerForVisibility(),this);
                notifyDirections(targettableDirections, gameManager, currentPlayer);
            }
            else { afterChooseDirectionAction(movementAction, gameManager, gameManager.getCurrentTurn().getDirection(), currentPlayer); }
        }
        else {
            if (movementAction.isTargetEnemy()) {

                targettablePlayers = visibility.getTargettablePlayers(gameManager, gameManager.getCurrentTurn().getCurrentPlayerForVisibility(), this);
                targettablePlayers=visibility.removeNotValidActionPlayer(gameManager,targettablePlayers,movementAction,currentPlayer,this);
                targettablePlayers=visibility.removeNotValidPlayers(gameManager,targettablePlayers,gameManager.getCurrentTurn().getCurrentPlayerForVisibility(),movementAction.getMinAmount(),movementAction.getMaxAmount(),this);
                if(!targettablePlayers.isEmpty())
                    currentPlayer.notifyOnLog("Choose an enemy target to move");
                notifyTargets(targettablePlayers, gameManager, currentPlayer);
            } else {
                switch (movementAction.getToTile()) {
                    case FORCED_TO_PLAYER:
                        targettablePlayers=visibility.getTargettablePlayers(gameManager,gameManager.getCurrentTurn().getCurrentPlayerForVisibility(), this);
                        if(!targettablePlayers.isEmpty())
                            currentPlayer.notifyOnLog("choose a target you want to move in your square");
                        notifyTargets(targettablePlayers, gameManager, currentPlayer);
                        break;
                    default:
                        targettableTiles = gameManager.getPossibleTiles(movementAction.getMinAmount(), movementAction.getMaxAmount(), currentPlayer.getCurrentTile());
                        gameManager.getCurrentTurn().setChosenTarget(null);

                        break;
                }
            }
        }
    }

    /**
     * method called after the visit. Here it's known the type of the currentAction it's going to be executed
     * It's similar to the startAction about the damageAction.
     * @param markAction
     * @param gameManager
     * @param currentPlayer
     */
    public void startAction (MarkAction markAction, GameManager gameManager, Player currentPlayer){
        alreadyInteractedPlayers.add(new ArrayList<Player>());
        gameManager.getCurrentTurn().setCurrentAction(markAction);

        List<Direction> targettableDirections;
        List<Player> targettablePlayers=visibility.getTargettablePlayers(gameManager,gameManager.getCurrentTurn().getCurrentPlayerForVisibility(), this);
        targettablePlayers=visibility.removeNotValidPlayers(gameManager,targettablePlayers,gameManager.getCurrentTurn().getCurrentPlayerForVisibility(),markAction.getMinDist(),markAction.getMaxDist(), this);
        if (visibility.isStraightForward()) {
            if (gameManager.getCurrentTurn().getDirection() == null) {
                targettableDirections = visibility.getTargettableDirections(gameManager.getCurrentTurn().getCurrentPlayerForVisibility(), gameManager, this);
                targettableDirections=visibility.removeNotReachableDirections(gameManager,targettableDirections,gameManager.getCurrentTurn().getCurrentPlayerForVisibility(), this);
                notifyDirections(targettableDirections, gameManager, currentPlayer);
            } else {
                afterChooseDirectionAction(markAction, gameManager, gameManager.getCurrentTurn().getDirection(), currentPlayer);
            }
        } else {
            if (visibility.isAnotherRoom()) {
                List<RoomColor> targettableRoomColors = visibility.getTargettableRoomColors( gameManager.getCurrentTurn().getCurrentPlayerForVisibility(),targettablePlayers);
                notifyRooms(targettableRoomColors, gameManager, currentPlayer);
            } else {
                if (markAction.isAoe()) {
                    List<Tile> targettableTiles = visibility.getTargettableTiles(gameManager.getCurrentTurn().getCurrentPlayerForVisibility(),targettablePlayers);
                    notifyTiles(targettableTiles, gameManager, currentPlayer);
                } else { notifyTargets(targettablePlayers, gameManager, currentPlayer); }
            }
        }
    }

    /**
     * method to check if the chosen direction is valid
     * @param gameManager
     * @param direction the cosen direction
     * @return the corresponding direction in the possible Choices
     * @throws GameException it's thrown if the choice is not correct
     */
    public Direction isAValidDirection(GameManager gameManager,Direction direction) throws GameException {
        for(Direction d:gameManager.getCurrentTurn().getPossibleChoices().getSelectableDirections())
            if(d==direction)
                return d;
       throw  new WrongDirectionException();
    }

    /**
     * method called after the visit. Here it's known the type of the currentAction it's going to be executed.
     * this method is called after the user has chosen a valid direction
     * It checks again what types of targets need to be chosen: players or tiles
     */
    public void afterChooseDirectionAction (DamageAction damageAction,GameManager gameManager,Direction direction, Player player) {
        gameManager.getCurrentTurn().getPossibleChoices().clear();
        gameManager.getCurrentTurn().setDirection(direction);
        List<Player> targettablePlayers = visibility.getDirectionPlayers(direction, gameManager.getCurrentTurn().getCurrentPlayerForVisibility(), gameManager, this);
        targettablePlayers=visibility.removeNotValidPlayers(gameManager,targettablePlayers,gameManager.getCurrentTurn().getCurrentPlayerForVisibility(),damageAction.getMinDist(),damageAction.getMaxDist(), this);
        List<Tile> targettableTiles= new ArrayList<>();
        if (damageAction.isAoe()){
            for(Player p:targettablePlayers){
                if(!targettableTiles.contains(p.getCurrentTile()))
                    targettableTiles.add(p.getCurrentTile());}
            notifyTiles(targettableTiles, gameManager, player);
        }
        else{
            notifyTargets(targettablePlayers, gameManager, player);
        }
    }

    /**
     * method called after the visit. Here it's known the type of the currentAction it's going to be executed.
     * this method is called after the user has chosen a valid direction
     * It checks again what types of targets need to be chosen: players or tiles
     */
    public void afterChooseDirectionAction (MarkAction markAction,GameManager gameManager,Direction direction,Player player) {
        gameManager.getCurrentTurn().getPossibleChoices().clear();
        gameManager.getCurrentTurn().setDirection(direction);
        List<Player> targettablePlayers = visibility.getDirectionPlayers(direction, gameManager.getCurrentTurn().getCurrentPlayerForVisibility(), gameManager, this);
        targettablePlayers=visibility.removeNotValidPlayers(gameManager,targettablePlayers,gameManager.getCurrentTurn().getCurrentPlayerForVisibility(),markAction.getMinDist(),markAction.getMaxDist(), this);
        List<Tile> targettableTiles= new ArrayList<>();
        if (markAction.isAoe()){
            for(Player p:targettablePlayers){
                if(!targettableTiles.contains(p.getCurrentTile()))
                    targettableTiles.add(p.getCurrentTile());}
            notifyTiles(targettableTiles, gameManager, player);
        }
        else{ notifyTargets(targettablePlayers, gameManager, player); }
    }

    /**
     * method called after the visit. Here it's known the type of the currentAction it's going to be executed.
     * this method is called after the user has chosen a valid direction
     * It checks again what types of targets need to be chosen: players or tiles; it also check if the currentplayer needs to be moved or
     * an enemy needs to. It differentiate also among the different types of possible movements
     */
    public void afterChooseDirectionAction (MovementAction movementAction,GameManager gameManager,Direction direction,Player currentPlayer) {
        gameManager.getCurrentTurn().getPossibleChoices().clear();
        gameManager.getCurrentTurn().setDirection(direction);
        List<Player> targettablePlayers;
        List<Tile> targettableTiles ;
        if(movementAction.isTargetEnemy()){
            targettablePlayers = visibility.getDirectionPlayers(direction, gameManager.getCurrentTurn().getCurrentPlayerForVisibility(), gameManager, this);
            targettablePlayers=visibility.removeNotValidPlayers(gameManager,targettablePlayers,gameManager.getCurrentTurn().getCurrentPlayerForVisibility(),movementAction.getMinAmount(),movementAction.getMaxAmount(),this);
            notifyTargets(targettablePlayers, gameManager, currentPlayer);
        } else {
            switch (movementAction.getToTile()) {
                case FORCED_TO_PLAYER:{
                    targettablePlayers = visibility.getDirectionPlayers(direction,gameManager.getCurrentTurn().getCurrentPlayerForVisibility(), gameManager, this);
                    targettablePlayers=visibility.removeNotValidPlayers(gameManager,targettablePlayers,gameManager.getCurrentTurn().getCurrentPlayerForVisibility(),movementAction.getMinAmount(),movementAction.getMaxAmount(),this);
                    notifyTargets(targettablePlayers, gameManager, currentPlayer);
                    break;
                }
                default:{
                    targettableTiles = visibility.getDirectionTiles(gameManager, gameManager.getCurrentTurn().getCurrentPlayerForVisibility(), movementAction.getMinAmount(), movementAction.getMaxAmount(), direction);
                    gameManager.getCurrentTurn().setChosenTarget(null);
                    notifyTiles(targettableTiles, gameManager, currentPlayer);
                    break;
                }
            }
        }
    }

    /**
     *  checks if the chosen playerView is a valid target
     * @param gameManager
     * @param p the playerView chosen
     * @return the corresponding player in possibleChoices
     * @throws GameException if the player chosen is not valid
     */
    public Player isAValidTarget(GameManager gameManager, PlayerView p) throws GameException{
        for(Player player:gameManager.getCurrentTurn().getPossibleChoices().getSelectableTargets())
            if(player.getPlayerID().equals(p.getPlayerID()))
                return player;
        throw  new WrongTargetException();
    }

    /**
     * method called after the visit. Here it's known the type of the currentAction it's going to be executed.
     * this method is called after the user has chosen a valid target
     * it checks if the target player has a valid powerUp to counter attack; if not, it checks if the currentPlayer
     * has valid after damage powerUps. If not it terminates the action computation
     * @param damageAction the current action
     * @param gameManager
     * @param currentPlayer the player that's shooting
     * @param targetPlayer the player chosen
     */
    public void afterChooseTargetAction (DamageAction damageAction, GameManager gameManager, Player currentPlayer, Player targetPlayer) {
        gameManager.getCurrentTurn().getPossibleChoices().clear();
        gameManager.getCurrentTurn().setChosenTarget(targetPlayer);
        alreadyInteractedPlayers.add(new ArrayList<Player>());
        handleDamage(gameManager, damageAction, targetPlayer);
        if(damageAction.getHitInTheEffect()!=null)
            gameManager.getCurrentTurn().getAlreadyHitPlayer().get(damageAction.getHitInTheEffect()).remove(targetPlayer);
        targetPlayer.setTurnState(TurnState.CHOOSE_COUNTER_ATTACK);
        currentPlayer.setTurnState(TurnState.CHOOSE_AFTER_DAMAGE_POWERUP);
        if (targetPlayer.hasValidPowerUp(gameManager)) {
            gameManager.getCurrentTurn().getCounterAttackingPlayers().putIfAbsent(targetPlayer, targetPlayer.getValidPowerUps(gameManager));
            notifyPowerUps(gameManager, targetPlayer, TurnState.CHOOSE_COUNTER_ATTACK);
            currentPlayer.notifyOnLog("Waiting for Counter Attack...");
            currentPlayer.setTurnState(TurnState.CAN_BE_COUNTER_ATTACKED);
        } else {
            targetPlayer.setTurnState(TurnState.WAIT_FOR_OTHER_PLAYERS_TURN);
            currentPlayer.setTurnState(TurnState.CHOOSE_AFTER_DAMAGE_POWERUP);
            if (currentPlayer.hasValidPowerUp(gameManager))
                notifyPowerUps(gameManager, currentPlayer, TurnState.CHOOSE_AFTER_DAMAGE_POWERUP);
            else {
                currentPlayer.setTurnState(TurnState.CHOOSE_TARGET);
                afterExecution(damageAction, gameManager, currentPlayer);
            }
        }
    }

    /**
     * handles the damage to deal to a specific target
     * @param gameManager
     * @param damageAction
     * @param targetPlayer the player to hit
     */
    private void handleDamage(GameManager gameManager,DamageAction damageAction,Player targetPlayer){
        alreadyInteractedPlayers.add(new ArrayList<Player>());
        targetPlayer.addDamageTaken(gameManager.getCurrentPlayerTurn(), damageAction.getMaxAmount(),gameManager);
        gameManager.setPlayerState(targetPlayer);
        alreadyInteractedPlayers.get(alreadyInteractedPlayers.size()-1).add(targetPlayer);
        gameManager.getCurrentTurn().getAlreadyHitPlayer().get(this).add(targetPlayer);
        if(!gameManager.getCurrentTurn().getAlreadyHitTile().get(this).contains(targetPlayer.getCurrentTile())){
            gameManager.getCurrentTurn().getAlreadyHitTile().get(this).add(targetPlayer.getCurrentTile());}
    }

    /**
     * method called after the visit. Here it's known the type of the currentAction it's going to be executed.
     * this method is called after the user has chosen a valid target. It gives the corresponding marks to him
     * @param markAction the current Action
     * @param gameManager
     * @param currentPlayer
     * @param targetPlayer the target chosen
     */
    public void afterChooseTargetAction (MarkAction markAction, GameManager gameManager, Player currentPlayer, Player targetPlayer){
        gameManager.getCurrentTurn().getPossibleChoices().clear();
        gameManager.getCurrentTurn().setChosenTarget(targetPlayer);
        gameManager.getCurrentTurn().addMark(targetPlayer,markAction.getMaxAmount());
        alreadyInteractedPlayers.get(alreadyInteractedPlayers.size()-1).add(targetPlayer);
        gameManager.getCurrentTurn().getAlreadyHitPlayer().get(this).add(targetPlayer);
        afterExecution(markAction,gameManager,currentPlayer);
    }

    /**
     * method called after the visit. Here it's known the type of the currentAction it's going to be executed.
     * this method is called after the user has chosen a valid target. It checks all the possible cases checking all the flag to
     * provide the right movement that need to be done according to the Weapon
     * @param movementAction the current action
     * @param gameManager
     * @param currentPlayer
     * @param targetPlayer the target chosen
     */
    public void afterChooseTargetAction (MovementAction movementAction, GameManager gameManager, Player currentPlayer,Player targetPlayer){
        gameManager.getCurrentTurn().getPossibleChoices().clear();
        gameManager.getCurrentTurn().setChosenTarget(targetPlayer);
        List<Player> targettablePlayers;
        List<Tile> targettableTiles;

        if (movementAction.isTargetEnemy()){
            if(visibility.isEverywhere()) {
                if(movementAction.getToTile()== ForcedMovement.FORCED_TO_PLAYER){
                    gameManager.getTiles().get(targetPlayer.getCurrentTile().getX()).get(targetPlayer.getCurrentTile().getY()).removePlayer(targetPlayer);
                    targetPlayer.getCurrentTile().removePlayer(targetPlayer);
                    targetPlayer.setCurrentTile(gameManager.getCurrentTurn().getCurrentPlayerForVisibility().getCurrentTile());
                    gameManager.getCurrentTurn().getCurrentPlayerForVisibility().getCurrentTile().addPlayer(targetPlayer);
                    gameManager.notifyOnMovement(targetPlayer,targetPlayer.getCurrentTile());
                    alreadyInteractedPlayers.get(alreadyInteractedPlayers.size()-1).add(targetPlayer);
                    afterExecution(movementAction,gameManager,currentPlayer);
                    return;
                }
                targettableTiles = gameManager.getPossibleTiles(movementAction.getMinAmount(), movementAction.getMaxAmount(), targetPlayer.getCurrentTile());
                List<Tile> support=new ArrayList<>(targettableTiles);
                Tile tile=targetPlayer.getCurrentTile();
                tile.removePlayer(targetPlayer);
                for(Tile t:support){
                    targetPlayer.setCurrentTile(t);
                    t.addPlayer(targetPlayer);
                    if(!gameManager.getVisiblePlayers(gameManager.getCurrentTurn().getCurrentPlayerForVisibility()).contains(targetPlayer))
                        targettableTiles.remove(t);
                    t.removePlayer(targetPlayer);
                }
                tile.addPlayer(targetPlayer);
                targetPlayer.setCurrentTile(tile);
                if(!targettableTiles.isEmpty())
                    currentPlayer.notifyOnLog("choose a tile where you want to move the enemy selected");
                notifyTiles(targettableTiles,gameManager,currentPlayer);
                return;
            }
           if (visibility.isStraightForward()){
               targettableTiles= visibility.getDirectionTiles(gameManager, targetPlayer,movementAction.getMinAmount(),movementAction.getMaxAmount(),gameManager.getCurrentTurn().getDirection());
               if(!targettableTiles.isEmpty())
                currentPlayer.notifyOnLog("choose a tile where you want to move the enemy selected");
               notifyTiles(targettableTiles, gameManager, currentPlayer);}
           else{
               switch(movementAction.getToTile()){
                   case FORCED_TO_PLAYER:{
                        gameManager.getTiles().get(targetPlayer.getCurrentTile().getX()).get(targetPlayer.getCurrentTile().getY()).removePlayer(targetPlayer);
                        targetPlayer.getCurrentTile().removePlayer(targetPlayer);
                        targetPlayer.setCurrentTile(gameManager.getCurrentTurn().getCurrentPlayerForVisibility().getCurrentTile());
                        gameManager.getCurrentTurn().getCurrentPlayerForVisibility().getCurrentTile().addPlayer(targetPlayer);
                        gameManager.notifyOnMovement(targetPlayer,targetPlayer.getCurrentTile());
                        alreadyInteractedPlayers.get(alreadyInteractedPlayers.size()-1).add(targetPlayer);
                        afterExecution(movementAction,gameManager,currentPlayer);
                        break;
                   }
                   default: {
                       gameManager.getCurrentTurn().setChosenTarget(targetPlayer);
                       targettableTiles=gameManager.getPossibleTiles(movementAction.getMinAmount(),movementAction.getMaxAmount(),targetPlayer.getCurrentTile());
                       if(!targettableTiles.isEmpty())
                           currentPlayer.notifyOnLog("choose a tile where you want to move the enemy selected");
                       notifyTiles(targettableTiles, gameManager, currentPlayer);
                       break;
                   }
               }
           }
        }
        else{
            if (visibility.isStraightForward()){
                switch (movementAction.getToTile()){
                    case FORCED_TO_PLAYER:{
                        gameManager.getCurrentTurn().getCurrentPlayerForVisibility().getCurrentTile().removePlayer(gameManager.getCurrentTurn().getCurrentPlayerForVisibility());
                        gameManager.getCurrentTurn().getCurrentPlayerForVisibility().setCurrentTile(targetPlayer.getCurrentTile());
                        targetPlayer.getCurrentTile().addPlayer(gameManager.getCurrentTurn().getCurrentPlayerForVisibility());
                        gameManager.notifyOnMovement(gameManager.getCurrentTurn().getCurrentPlayerForVisibility(),targetPlayer.getCurrentTile());
                        alreadyInteractedPlayers.get(alreadyInteractedPlayers.size()-1).add(targetPlayer);
                        afterExecution(movementAction,gameManager,currentPlayer);
                        break;}
                    default:{
                        targettableTiles = visibility.getDirectionTiles(gameManager,currentPlayer,movementAction.getMinAmount(),movementAction.getMaxAmount(),gameManager.getCurrentTurn().getDirection());
                        if(!targettableTiles.isEmpty())
                            currentPlayer.notifyOnLog("choose a tile where you want to move");
                        notifyTiles(targettableTiles, gameManager, currentPlayer);
                        break;
                    }
                }
            }
            else{
                switch (movementAction.getToTile()){
                    case FORCED_TO_PLAYER:{
                        gameManager.getCurrentTurn().getCurrentPlayerForVisibility().getCurrentTile().removePlayer(gameManager.getCurrentTurn().getCurrentPlayerForVisibility());
                        gameManager.getCurrentTurn().getCurrentPlayerForVisibility().setCurrentTile(targetPlayer.getCurrentTile());
                        targetPlayer.getCurrentTile().addPlayer(gameManager.getCurrentTurn().getCurrentPlayerForVisibility());
                        gameManager.notifyOnMovement(gameManager.getCurrentTurn().getCurrentPlayerForVisibility(),targetPlayer.getCurrentTile());
                        alreadyInteractedPlayers.get(alreadyInteractedPlayers.size()-1).add(targetPlayer);
                        afterExecution(movementAction,gameManager,currentPlayer);
                        break;}
                    default:{
                        targettableTiles=gameManager.getPossibleTiles(movementAction.getMinAmount(),movementAction.getMaxAmount(),currentPlayer.getCurrentTile());
                        if(!targettableTiles.isEmpty())
                            currentPlayer.notifyOnLog("choose a tile where you want to move");
                        notifyTiles(targettableTiles, gameManager, currentPlayer);
                        break;
                    }
                }
            }
        }
    }

    /**
     * checks if the selected TileView is valid
     * @param gameManager
     * @param t the TileView chosen
     * @return the corresponding Tile in possibleChoices
     * @throws GameException if the selected TileView is not valid
     */
    public Tile isAValidTile(GameManager gameManager, TileView t) throws GameException{
        for(Tile tile:gameManager.getCurrentTurn().getPossibleChoices().getSelectableTiles())
            if(tile.getX() == t.getX() && tile.getY() == t.getY())
                return tile;
        throw  new WrongTileException();
    }

    /**
     * method called after the visit. Here it's known the type of the currentAction it's going to be executed.
     * this method is called after the user has chosen a valid tile. It delas damage to all the players in the tile (except for the current
     * player). It also check for possible counterattacks and after damage powerups.
     * @param damageAction the current action
     * @param gameManager
     * @param currentPlayer
     * @param targetTile the tile selected
     */
    public void afterChooseTileAction(DamageAction damageAction, GameManager gameManager, Player currentPlayer, Tile targetTile) {
        gameManager.getCurrentTurn().getPossibleChoices().clear();
        for (Player player : targetTile.getPlayers()) {
            if (!player.equals(currentPlayer)) {
                player.addDamageTaken(gameManager.getCurrentPlayerTurn(), damageAction.getMaxAmount(), gameManager);
                gameManager.setPlayerState(player);
                gameManager.getCurrentTurn().getAlreadyHitPlayer().get(this).add(player);
                alreadyInteractedPlayers.get(alreadyInteractedPlayers.size() - 1).add(player);
                if (!gameManager.getCurrentTurn().getAlreadyHitTile().get(this).contains(player.getCurrentTile())) {
                    gameManager.getCurrentTurn().getAlreadyHitTile().get(this).add(player.getCurrentTile());
                }
                currentPlayer.setTurnState(TurnState.CAN_BE_COUNTER_ATTACKED);
                TurnStateHandler.manageAddCounterAttackingQueue(player, gameManager);
            }
        }
        if (gameManager.getCurrentTurn().getCounterAttackingPlayers().isEmpty()) {
            currentPlayer.setTurnState(TurnState.CHOOSE_AFTER_DAMAGE_POWERUP);
            if (currentPlayer.hasValidPowerUp(gameManager))
                notifyPowerUps(gameManager, currentPlayer, TurnState.CHOOSE_AFTER_DAMAGE_POWERUP);
            else {
                currentPlayer.setTurnState(TurnState.CHOOSE_TARGET);
                afterExecution(damageAction, gameManager, currentPlayer);
            }
        }
        else{
            currentPlayer.notifyOnLog("Waiting for Counter Attack...");
        }
    }

    /**
     * method called after the visit. Here it's known the type of the currentAction it's going to be executed.
     * this method is called after the user has chosen a valid tiles. It gives the corresponding marks to all
     * the players in the tile (except for the current player)
     * @param markAction the current action
     * @param gameManager
     * @param currentPlayer
     * @param targetTile the tile selected
     */
    public void afterChooseTileAction (MarkAction markAction, GameManager gameManager, Player currentPlayer, Tile targetTile){
        gameManager.getCurrentTurn().getPossibleChoices().clear();
        gameManager.getCurrentTurn().setChosenTile(targetTile);
        for (Player player: targetTile.getPlayers()) {
            if (!player.equals(currentPlayer)) {
                gameManager.getCurrentTurn().addMark(player, markAction.getMaxAmount());
                alreadyInteractedPlayers.get(alreadyInteractedPlayers.size() - 1).add(player);
            }
        }
        afterExecution(markAction, gameManager, currentPlayer);
    }

    /**
     * method called after the visit. Here it's known the type of the currentAction it's going to be executed.
     * this method is called after the user has chosen a valid tile. It checks all the flag the provide the right
     * movement (of an enemy or of the current player)
     * @param movementAction the current Action
     * @param gameManager
     * @param currentPlayer
     * @param t1 the Tile selected by the player
     */
    public void afterChooseTileAction (MovementAction movementAction, GameManager gameManager, Player currentPlayer,Tile t1) {
        Player targetPlayer = gameManager.getCurrentTurn().getChosenTarget();
        gameManager.getCurrentTurn().getPossibleChoices().clear();
        if (isOnlyMovement()) {
            currentPlayer.getCurrentTile().removePlayer(currentPlayer);
            currentPlayer.setCurrentTile(t1);
            t1.addPlayer(currentPlayer);
            gameManager.notifyOnMovement(currentPlayer,t1);
            alreadyInteractedPlayers.get(alreadyInteractedPlayers.size()-1).add(currentPlayer);
            afterExecution(movementAction, gameManager, currentPlayer);
            return;
        }
        else {
            if (movementAction.isTargetEnemy()) {
                gameManager.getCurrentTurn().addInteractedInCurrentAction(targetPlayer);
                gameManager.getTiles().get(targetPlayer.getCurrentTile().getX()).get(targetPlayer.getCurrentTile().getY()).removePlayer(targetPlayer);
                targetPlayer.setCurrentTile(t1);
                t1.addPlayer(targetPlayer);
                alreadyInteractedPlayers.get(alreadyInteractedPlayers.size()-1).add(targetPlayer);
                gameManager.notifyOnMovement(targetPlayer, targetPlayer.getCurrentTile());
            } else {
                int x = gameManager.getCurrentTurn().getCurrentPlayerForVisibility().getCurrentTile().getX();
                int y = gameManager.getCurrentTurn().getCurrentPlayerForVisibility().getCurrentTile().getY();
                gameManager.getTiles().get(x).get(y).removePlayer(gameManager.getCurrentTurn().getCurrentPlayerForVisibility());
                gameManager.getCurrentTurn().getCurrentPlayerForVisibility().setCurrentTile(t1);
                t1.addPlayer(gameManager.getCurrentTurn().getCurrentPlayerForVisibility());
                gameManager.notifyOnMovement(gameManager.getCurrentTurn().getCurrentPlayerForVisibility(), t1);
            }
            afterExecution(movementAction, gameManager, currentPlayer);
        }
    }

    /**
     * checks if the selected room is valid
     * @param gameManager
     * @param roomColor the room selected
     * @return
     * @throws GameException if the room selected is not valid
     */
    public RoomColor isAValidRoom(GameManager gameManager,RoomColor roomColor) throws GameException{
        for(RoomColor roomColor1:gameManager.getCurrentTurn().getPossibleChoices().getSelectableRooms())
            if(roomColor==roomColor1)
                return roomColor1;
        throw  new WrongRoomException();
    }

    /**
     * method called after the visit. Here it's known the type of the currentAction it's going to be executed.
     * this method is called after the user has chosen a valid rooms. it deals damage to all the players in that room.
     * checks if there are players that can use powerUp for counter attack or if the current player can use an after damage powerUp
     * @param damageAction
     * @param gameManager
     * @param currentPlayer
     * @param roomColor the room selected
     */
    public void afterChooseRoomAction(DamageAction damageAction, GameManager gameManager, Player currentPlayer, RoomColor roomColor){
        gameManager.getCurrentTurn().getPossibleChoices().clear();
        for (Tile tile : gameManager.getTileOfColor(roomColor)) {
            for (Player player : tile.getPlayers()) {
                if(!player.equals(currentPlayer)) {
                    player.addDamageTaken(currentPlayer, damageAction.getMaxAmount(), gameManager);
                    gameManager.setPlayerState(player);
                    alreadyInteractedPlayers.get(alreadyInteractedPlayers.size() - 1).add(player);
                    currentPlayer.setTurnState(TurnState.CAN_BE_COUNTER_ATTACKED);
                    TurnStateHandler.manageAddCounterAttackingQueue(player, gameManager);
                }
            }
        }
        if(gameManager.getCurrentTurn().getCounterAttackingPlayers().isEmpty()) {
            currentPlayer.setTurnState(TurnState.CHOOSE_AFTER_DAMAGE_POWERUP);
            if (currentPlayer.hasValidPowerUp(gameManager))
                notifyPowerUps(gameManager, currentPlayer, TurnState.CHOOSE_AFTER_DAMAGE_POWERUP);
            else {
                currentPlayer.setTurnState(TurnState.CHOOSE_ROOM);
                afterExecution(damageAction, gameManager, currentPlayer);
            }
        }
        else {
            currentPlayer.notifyOnLog("Waiting for Counter Attack...");
        }
    }

    /**
     * method called after the visit. Here it's known the type of the currentAction it's going to be executed.
     * this method is called after the user has chosen a valid room. Gives marks to all the player in the room
     * @param markAction the current action
     * @param gameManager
     * @param currentPlayer
     * @param roomColor the room selected
     */
    public void afterChooseRoomAction(MarkAction markAction, GameManager gameManager, Player currentPlayer, RoomColor roomColor){
        gameManager.getCurrentTurn().getPossibleChoices().clear();
        for (Tile tile : gameManager.getTileOfColor(roomColor)) {
            for (Player player : tile.getPlayers()) {
                gameManager.getCurrentTurn().addMark(player , markAction.getMaxAmount());
                alreadyInteractedPlayers.get(alreadyInteractedPlayers.size()-1).add(player);
            }
        }
        afterExecution(markAction,gameManager,currentPlayer);
    }

    /**
     * method to deal with the end of the current Action.
     * It checks if there's a possible Linked Effect, if the effect has ended or if there are action linked.
     * In case the effect is finished it calls a method of the current Weapon to check if there are other possible effects
     * @param action the current Action
     * @param gameManager
     * @param player the current Player
     */
    public void afterExecution(Action action, GameManager gameManager,Player player){
        if(action.getLinkedEffect()!=null){
            gameManager.getCurrentTurn().topActions().remove(0);
            if(action.getLinkedToNext()!=null)
                action.getLinkedEffect().setLinked(true);
            if (player.getPotentialAmmos().hasCorrectCost(action.getLinkedEffect().getCost())) {
                notifyLinkedEffect(gameManager, player, action);
            }
            else{
                gameManager.getCurrentTurn().topEffect().startingExecution(gameManager.getCurrentTurn().topActions().get(0), gameManager, player);
            }
        }
        else{
        if (action.getLinkedToNext()!=null) {
            gameManager.getCurrentTurn().topActions().remove(0);
            action.getLinkedToNext().linkedToNextExecute(this, gameManager, player); }
        else {
            if (actions.indexOf(action) >= actions.size() - 1) {
                gameManager.getCurrentTurn().popEffect();
                setExecuted(true);
                if(gameManager.getCurrentTurn().topEffect()!=null) {
                    if(isLinked) {
                        isLinked = false;
                        gameManager.getCurrentTurn().topActions().get(0).linkedToNextExecute(gameManager.getCurrentTurn().topEffect(), gameManager, player);
                    }
                    else {
                        gameManager.getCurrentTurn().topEffect().startingExecution(gameManager.getCurrentTurn().topActions().get(0), gameManager, player);
                    }
                }
                else{
                 gameManager.getCurrentTurn().getCurrentWeapon().afterEffect(this,gameManager,player);

                   }

            }
            else {
                gameManager.getCurrentTurn().topActions().remove(action);
                startingExecution(gameManager.getCurrentTurn().topActions().get(0), gameManager, player);
                }
            }
        }
    }

    /**
     * checks if two effects are the same
     * @param obj
     * @return true if they are the same, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        Effect e;
        if(obj instanceof Effect){
            e=(Effect)obj;
             return e.getName().equals(name);
        }
        return false;
    }
}

