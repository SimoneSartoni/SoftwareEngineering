package model.powerup;

import model.board.GameManager;
import model.board.Tile;
import model.board.TileView;
import model.enums.AmmoColor;
import model.enums.Direction;
import model.enums.PowerUpUse;
import model.enums.TurnState;
import model.exceptions.GameException;
import model.exceptions.WrongChoiceException;
import model.player.Player;
import model.player.PlayerView;
import model.utility.TurnStateHandler;
import model.utility.Visibility;
import network.ViewProxy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PowerUp implements Serializable {

    private int id = -1;
    private AmmoColor color;
    /**
     * when the powerUp can be used (in what state of the turn)
     */
    private PowerUpUse typeOfUse;
    private String name;
    /**
     * the effect related to the powerUp
     */
    private PowerUpEffect powerUpEffect;
    /**
     * the target selected during the effect
     */
    private transient Player selectedTarget;
    /**
     * list of targets that ca be chosen
     */
    private transient List<Player> choosableTargets;
    /**
     * list of tiles that can be chosen
     */
    private transient List<Tile> choosableTile;

    public PowerUp(AmmoColor color,PowerUpUse typeOfUse,String name,PowerUpEffect powerUpEffect){
        this.color=color;
        this.typeOfUse=typeOfUse;
        this.name=name;
        this.powerUpEffect =powerUpEffect;
        this.choosableTargets=new ArrayList<>();
        this.choosableTile=new ArrayList<>();
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public AmmoColor getColor() {
        return color;
    }


    public PowerUpUse getTypeOfUse() {
        return typeOfUse;
    }

    public PowerUpEffect getPowerUpEffect() {
        return powerUpEffect;
    }

    public String getName() {
        return name;
    }

    /**
     * checks if the current powerUp can be used or not in a specific situation
     * it checks ammos, if the player has the selected powerUp and if he is in the right state
     * @param gameManager the current game
     * @param player the players that's trying to use the powerUp
     * @return true if it can be used, false otherwise
     */
    public boolean isValid(GameManager gameManager,Player player) {
        if ((!player.getPowerUps().contains(this))||(!powerUpEffect.isValid(gameManager,player,this)))
            return false;
        if ((player.getTurnState() == TurnState.CHOOSE_AFTER_DAMAGE_POWERUP) && (typeOfUse == PowerUpUse.AFTER_DAMAGE))
            return true;
        if ((player.getTurnState() == TurnState.READY_FOR_ACTION) && (typeOfUse == PowerUpUse.SEPARATED))
            return true;
        if ((player.getTurnState() == TurnState.CHOOSE_COUNTER_ATTACK) && (typeOfUse == PowerUpUse.AFTER_TAKEN_DAMAGE)&&(gameManager.getVisiblePlayers(player).contains(gameManager.getCurrentPlayerTurn())))
            return true;
        return false;
    }

    /**
     * starting method of the powerUp execution. It makes the player pay the cost of the PowerUp or makes him discard a powerUp if he wants to
     * @param gameManager
     * @param currentPlayer the player that is playing the powerUp
     */
    public void payCost(GameManager gameManager,Player currentPlayer){
        if (currentPlayer.getPowerUpToDiscardForCost(getPowerUpEffect().getCost(),this,gameManager).isEmpty()) {
            if (powerUpEffect.getCost().getRedValue() < 0)
                currentPlayer.chooseAmmoToDiscard(gameManager);
            else {
                currentPlayer.payCost(gameManager, powerUpEffect.getCost());
                startEffect(gameManager, currentPlayer);
            }
        }
        else {
            gameManager.getCurrentTurn().getPossibleChoices().setSelectablePowerUps(currentPlayer.getPowerUpToDiscardForCost(powerUpEffect.getCost(),gameManager));
            currentPlayer.notifyOnLog("Choose a powerUp to discard to pay the cost of the powerUp");
            currentPlayer.setTurnState(TurnState.DISCARD_POWERUP_FOR_COST_POWERUP);
            currentPlayer.getViewPlayer().onPowerUps(gameManager.getCurrentTurn().getPossibleChoices().getSelectablePowerUps());
        }
    }

    /**
     * method used to check is the selected player is valid
     * @param player the selected player
     * @return the corresponding Player in choosabletargets
     * @throws GameException
     */
    public Player isAValidTarget(PlayerView player) throws GameException {
        for(Player p:choosableTargets)
            if(p.getPlayerID().equals(player.getPlayerID()))
                return p;
        throw new WrongChoiceException("a valid target in powerUp effect");
    }

    /**
     * method called after having paid the cost. It checks all the possible cases in all different usages
     * @param gameManager
     * @param currentPlayer the player that's using the powerUp
     */
    public void startEffect(GameManager gameManager,Player currentPlayer) {
        List<Player> targettablePlayers;
        List<Tile> targettableTiles;
        switch (typeOfUse){
            case SEPARATED:{
                if (powerUpEffect.isTargetEnemy()) {
                    currentPlayer.setTurnState(TurnState.CHOOSE_POWERUP_TARGET);
                    targettablePlayers = gameManager.getOnBoardPlayers();
                    targettablePlayers.remove(currentPlayer);
                    choosableTargets.addAll(targettablePlayers);
                    notifyOnTargets(currentPlayer,targettablePlayers);
                }
                else{
                    afterChooseTarget(gameManager, currentPlayer, currentPlayer);
                }
            } break;
            case AFTER_DAMAGE:{
                if(powerUpEffect.isAoe()) {
                    currentPlayer.notifyOnLog("Choose a tile where you want to deal damage from the already hit ones");
                    currentPlayer.setTurnState(TurnState.CHOOSE_POWERUP_TILE);
                    targettableTiles = gameManager.getCurrentTurn().getAlreadyHitTile().get(gameManager.getCurrentTurn().topEffect());
                    choosableTile.addAll(targettableTiles);
                    currentPlayer.notifyTiles(gameManager);
                }
                else {
                    currentPlayer.notifyOnLog("Choose a target from the already hit ones");
                    currentPlayer.setTurnState(TurnState.CHOOSE_POWERUP_TARGET);
                    targettablePlayers = gameManager.getCurrentTurn().getAlreadyHitPlayer().get(gameManager.getCurrentTurn().topEffect());
                    choosableTargets.addAll(targettablePlayers);
                    notifyOnTargets(currentPlayer,choosableTargets);
                }
            } break;
            case AFTER_TAKEN_DAMAGE:{
                if(powerUpEffect.getDmg()>0) {
                    gameManager.getCurrentPlayerTurn().addDamageTaken(currentPlayer, powerUpEffect.getDmg(), gameManager);
                    gameManager.setPlayerState(gameManager.getCurrentPlayerTurn());
                }
                gameManager.getCurrentPlayerTurn().addMarks(powerUpEffect.getMarks(),currentPlayer,gameManager);
                if(powerUpEffect.getMaxMovement()==0)
                    afterExecution(gameManager,currentPlayer);
                else {
                    currentPlayer.setTurnState(TurnState.CHOOSE_POWERUP_TILE);
                    selectedTarget=gameManager.getCurrentPlayerTurn();
                    choosableTile.addAll(gameManager.getPossibleTiles(0,powerUpEffect.getMaxMovement(),gameManager.getCurrentPlayerTurn().getCurrentTile()));
                    currentPlayer.notifyOnLog("Choose a tile where you want to move the player who dealt you the damage");
                    notifyOnTiles(currentPlayer,choosableTile);
                }
             break;}
        }
    }

    /**
     * method called after having chosen a valid target for the powerUp
     * @param gameManager
     * @param currentPlayer
     * @param targetPlayer the player selected
     */
    public void afterChooseTarget(GameManager gameManager,Player currentPlayer,Player targetPlayer){
        List<Tile> targettableTiles=new ArrayList<>();
        selectedTarget=targetPlayer;
        switch (typeOfUse){
            case SEPARATED:{
                if(powerUpEffect.getMaxMovement()>0) {
                    if(powerUpEffect.isStraightForward())
                        choosableTile.addAll(getDirectionTiles(gameManager));
                    else
                        choosableTile.addAll(gameManager.getPossibleTiles(powerUpEffect.getMinMovement(), powerUpEffect.getMaxMovement(), targetPlayer.getCurrentTile()));
                    currentPlayer.setTurnState(TurnState.CHOOSE_POWERUP_TILE);
                    notifyOnTiles(currentPlayer,choosableTile);
                }
                break;
            }
            case AFTER_DAMAGE:{
                if(powerUpEffect.getMaxMovement()>0) {
                    targettableTiles = gameManager.getPossibleTiles(powerUpEffect.getMinMovement(), powerUpEffect.getMaxMovement(), targetPlayer.getCurrentTile());
                    choosableTile.addAll(targettableTiles);
                    currentPlayer.setTurnState(TurnState.CHOOSE_POWERUP_TILE);
                    currentPlayer.notifyOnLog("Choose a tile where you want to move the player");
                    notifyOnTiles(currentPlayer,targettableTiles);
                }
                else {
                    if(powerUpEffect.getDmg()>0) {
                        targetPlayer.addDamageTaken(currentPlayer, powerUpEffect.getDmg(), gameManager);
                        gameManager.setPlayerState(targetPlayer);
                    }
                    if(powerUpEffect.getMarks()>0)
                        targetPlayer.addMarks(powerUpEffect.getMarks(), currentPlayer,gameManager);
                    afterExecution(gameManager,currentPlayer);
                }
            } break;
            default: break;
        }
    }

    private List<Tile> getDirectionTiles(GameManager gameManager) {
        List<Tile> ret = new ArrayList<>();
        ret.addAll(Visibility.getDirectionTiles(gameManager,selectedTarget,powerUpEffect.getMinMovement(),powerUpEffect.getMaxMovement(), Direction.NORTH));
        ret.addAll(Visibility.getDirectionTiles(gameManager,selectedTarget,powerUpEffect.getMinMovement(),powerUpEffect.getMaxMovement(), Direction.SOUTH));
        ret.addAll(Visibility.getDirectionTiles(gameManager,selectedTarget,powerUpEffect.getMinMovement(),powerUpEffect.getMaxMovement(), Direction.EAST));
        ret.addAll(Visibility.getDirectionTiles(gameManager,selectedTarget,powerUpEffect.getMinMovement(),powerUpEffect.getMaxMovement(), Direction.WEST));
        return ret;
    }

    /**
     * method used to check if the selected target is valid
     * @param tile the TileView selected
     * @return the corresponding Tile in choosableTile
     * @throws GameException id the TileView is not valid
     */
    public Tile isValidTile(TileView tile) throws GameException{
        for(Tile t:choosableTile)
            if(t.getX() == tile.getX() && t.getY() == tile.getY())
                return t;
        throw new WrongChoiceException(" tile in powerUp effect");
    }

    //only for tests
    public void setSelectedTarget(Player selectedTarget) {
        this.selectedTarget = selectedTarget;
    }

    /**
     * method called after having chosen a Tile. It distinguish among all the possible cases and usage, using powerUp effect flags
     * @param gameManager
     * @param currentPlayer
     * @param targetTile
     */
    public void afterChooseTile(GameManager gameManager, Player currentPlayer, Tile targetTile){
        switch(typeOfUse){
            case SEPARATED:
                selectedTarget.getCurrentTile().removePlayer(selectedTarget);
                selectedTarget.setCurrentTile(targetTile);
                targetTile.addPlayer(selectedTarget);
                notifyOnMovement(gameManager,targetTile);
                if(powerUpEffect.getDmg()>0)
                    selectedTarget.addDamageTaken(currentPlayer,powerUpEffect.getDmg(),gameManager);
                if(powerUpEffect.getMarks()>0)
                    selectedTarget.addMarks(powerUpEffect.getMarks(),currentPlayer,gameManager);
                afterExecution(gameManager,currentPlayer);
                break;
            case AFTER_DAMAGE:{
                if(powerUpEffect.isAoe()) {
                    for (Player p : targetTile.getPlayers()) {
                        if(!p.equals(currentPlayer)) {
                            p.addDamageTaken(currentPlayer, powerUpEffect.getDmg(), gameManager);
                            gameManager.setPlayerState(p);
                        }
                    }
                }
                else {
                    selectedTarget.setCurrentTile(targetTile);
                    targetTile.removePlayer(selectedTarget);
                    notifyOnMovement(gameManager,targetTile);
                }
            afterExecution(gameManager,currentPlayer);
            break;
            }
            case AFTER_TAKEN_DAMAGE: {
                selectedTarget.setCurrentTile(targetTile);
                targetTile.removePlayer(selectedTarget);
                notifyOnMovement(gameManager,targetTile);
            }
                afterExecution(gameManager, currentPlayer);
                break;
            }

        }


    /**
     * method to notify player about what targets he needs to choose next
     * @param currentPlayer the current player
     * @param players the list of possible targets
     */
        public void notifyOnTargets(Player currentPlayer,List<Player> players){
            currentPlayer.getViewPlayer().onTargets(playersToPlayerViews(players));
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
     * method to notify player about what tiles he needs to choose next
     * @param currentPlayer the current player
     * @param targettableTiles the list of possible Tiles
     */
    public void notifyOnTiles(Player currentPlayer,List<Tile> targettableTiles){
            currentPlayer.getViewPlayer().onTiles(GameManager.tilesToTileViews(targettableTiles));
        }

    /**
     * method used to notify all the players after having moved for the effect
     * @param gameManager
     * @param targetTile the target Tile to which the selected player has moved
     */
        public void notifyOnMovement(GameManager gameManager,Tile targetTile){
            TileView tileView = GameManager.createTileView(targetTile);
            for(ViewProxy v:gameManager.getNotifyObservers()) {
                PlayerView pV = selectedTarget.createPlayerView();
                pV.setTileView(GameManager.createTileView(selectedTarget.getCurrentTile()));
                v.onMovement(pV, tileView);
            }
        }

    /**
     * method call at the end of the powerUp execution. It uses the turnStateHandler to handle with the turnstates
     * of all the players
     * @param gameManager
     * @param player the player that has used the powerUp
     */
    public void afterExecution(GameManager gameManager,Player player){
        choosableTile.clear();
        choosableTargets.clear();
        selectedTarget=null;
        TurnStateHandler.afterPowerUpExecution(player,gameManager,this);
    }

    @Override
    public boolean equals(Object obj) {
        PowerUp p;
        if(obj instanceof PowerUp){
            p=(PowerUp) obj;
            return p.color == color && p.name.equals(name) && p.typeOfUse == typeOfUse && p.powerUpEffect.equals(powerUpEffect);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}
