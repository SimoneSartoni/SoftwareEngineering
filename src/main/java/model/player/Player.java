package model.player;

import model.board.*;
import model.enums.*;
import model.exceptions.*;
import model.powerup.PowerUp;
import model.utility.Ammo;
import model.utility.TurnStateHandler;
import model.weapon.Weapon;
import network.ViewProxy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static model.enums.TurnState.*;


public class Player implements Serializable {
    /**
     * the unique identifier
     */
    private String token;
    /**
     * the gameID he's playing into
     */
    private int gameID;
    /**
     * the username chosen
     */
    private String playerID;
    /**
     * the color chosen
     */
    private PlayerColor playerColor;
    /**
     * the current state (in adrenaline, normal, frenzy ..)
     */
    private PlayerState playerState;
    /**
     * the current turn state
     * it refers to what type of action can be performed by the user
     */
    private TurnState turnState;
    /**
     * his current score
     */
    private int score;
    /**
     * his current ammo (max 3,3,3)
     */
    private Ammo ammo;
    /**
     * his number of kills
     */
    private int nOfKills;
    /**
     * the current marks received (max 3 per player)
     */
    private Map<Player,Integer> marks;
    /**
     * the damage taken
     */
    private List<Player> damageTaken;
    /**
     * his current number of death
     */
    private int nOfDeaths;
    /**
     * the tile is into
     */
    private Tile currentTile;
    /**
     * the point he's worth
     */
    private PointsBoard board;
    /**
     * the current actions he can do
     */
    private TurnAction currentTurnAction;
    /**
     * the weapons in his hand
     */
    private List<Weapon> weapons;
    /**
     * the powerUps in his hand
     */
    private List<PowerUp> powerUps;
    /**
     * true if he's disconnected (for inactivity or connection)
     */
    private boolean disconnected;
    /**
     * set if he's currently using a powerUp
     */
    private PowerUp inUsePowerUp;
    /**
     * constant for the killing threshold
     */
    private final static int deadLives = 11;
    /**
     * constant for the overkilling threshold
     */
    private final static int overkillLives = 12;
    /**
     * the view associated used to comunicate with client
     */
    private transient ViewProxy viewPlayer;


    public Player (){
        this.damageTaken=new ArrayList<>();
        this.marks=new HashMap<>();
        this.powerUps= new ArrayList<>();
        this.weapons = new ArrayList<>();
        this.gameID = -1;
        this.token = "NOT INIT";
    }
    public Player ( String playerID,PlayerColor playerColor, PlayerState playerState, Tile currentTile, TurnAction currentTurnAction) {
        this.token = "NOT INIT";
        this.playerID=playerID;
        this.playerColor=playerColor;
        this.playerState=playerState;
        this.currentTile=currentTile;
        this.ammo = new Ammo(1, 1, 1);
        this.damageTaken=new ArrayList<>();
        this.marks=new HashMap<>();
        this.nOfDeaths=0;
        this.nOfKills=0;
        this.powerUps= new ArrayList<>();
        this.weapons = new ArrayList<>();
        disconnected=false;
        this.currentTurnAction = currentTurnAction;
        this.gameID = -1;
    }

    public static Player copyPlayer(Player player) {
        Player returnPlayer = new Player(player.playerID, player.playerColor, player.playerState, player.currentTile, player.currentTurnAction);
        returnPlayer.ammo = new Ammo(player.ammo.getRedValue(), player.ammo.getBlueValue(), player.ammo.getYellowValue());
        returnPlayer.damageTaken.addAll(player.damageTaken);
        for(Player player1 : player.marks.keySet()) {
            returnPlayer.marks.put(player1, player.marks.get(player1));
        }
        returnPlayer.nOfDeaths = player.nOfDeaths;
        returnPlayer.nOfKills = player.nOfKills;
        returnPlayer.powerUps.addAll(player.powerUps);
        returnPlayer.weapons.addAll(player.weapons);
        returnPlayer.disconnected = player.disconnected;
        returnPlayer.turnState = player.turnState;
        returnPlayer.score = player.score;
        returnPlayer.board = player.board;
        return returnPlayer;
    }

    private int getNOfLoadedWeapons(){
        int loaded=0;
        for(Weapon w : weapons) {
            if(w.getLoaded() == LoadedState.LOADED) {
                loaded++;
            }
        }
        return loaded;
    }

    public void setViewPlayer(ViewProxy viewPlayer) {
        this.viewPlayer = viewPlayer;
    }

    public ViewProxy getViewPlayer() {
        return viewPlayer;
    }

    public int getGameID() {
        return gameID;
    }

    public void setGameID(int gameID) {
        this.gameID = gameID;
    }

    public void setPlayerState(PlayerState playerState) {
        this.playerState = playerState;
    }

    public void setAmmo(Ammo ammo) {
        this.ammo = ammo;
    }

    /**
     * used when the player gains ammos
     * @param moreAmmo the ammo gained
     */
    public void addAmmo (Ammo moreAmmo){
        int r = this.ammo.getRedValue() + moreAmmo.getRedValue();
        int b = this.ammo.getBlueValue() + moreAmmo.getBlueValue();
        int y = this.ammo.getYellowValue() + moreAmmo.getYellowValue();
        if(r > 3)
            r = 3;
        if(b > 3)
            b = 3;
        if(y > 3)
            y = 3;
        this.ammo.setRedValue(r);
        this.ammo.setBlueValue(b);
        this.ammo.setYellowValue(y);
    }

    public String getPlayerID() {
        return playerID;
    }

    public PlayerColor getPlayerColor() {
        return playerColor;
    }

    public PlayerState getPlayerState() {
        return playerState;
    }

    public boolean isDisconnected(){ return disconnected;}

    public void setDisconnected(boolean disconnected){
        this.disconnected = disconnected;
    }

    public TurnState getTurnState() {
        return turnState;
    }

    public void setTurnState(TurnState turnState) {
        this.turnState = turnState;
    }

    public int getScore() {
        return score;
    }

    public int getNOfPowerUps() {
        return powerUps.size();
    }

    public Ammo getAmmo() {
        return ammo;
    }

    public int getNOfKills() {
        return nOfKills;
    }

    public Map<Player, Integer> getMarks() {
        return marks;
    }

    public List<Player> getDamageTaken() {
        return damageTaken;
    }

    public int getNOfDeaths() {
        return nOfDeaths;
    }

    public Tile getCurrentTile() {
        return currentTile;
    }

    /**
     * used to retrieve his unloaded weapons
     * @return
     */
    public List<Weapon> getUnloadedWeapons() {
        List<Weapon> unloaded = new ArrayList<>();
        for(Weapon w : weapons) {
            if(w.getLoaded() == LoadedState.UNLOADED) {
                unloaded.add(w);
            }
        }
        return unloaded;
    }

    public List<Weapon> getWeapons() {
        return weapons;
    }

    public List<PowerUp> getPowerUps() {
        return powerUps;
    }

    public PowerUp getInUsePowerUp() {
        return inUsePowerUp;
    }

    /**
     * it delete all the damage taken until now
     */
    public void resetDamageTaken(){
        damageTaken=new ArrayList<>();
    }

    public TurnAction getCurrentTurnAction() {
        return currentTurnAction;
    }

    public PointsBoard getBoard() {
        return board;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void addScore(int addingScore){
        this.score+=addingScore;
    }

    public void setNOfKills(int nOfKills) {
        this.nOfKills = nOfKills;
    }

    public void addNOfKills(){
        this.nOfKills++;
    }

    public void setCurrentTurnAction(TurnAction currentTurnAction){
        this.currentTurnAction=currentTurnAction;
    }

    public void addNOfDeaths() {
        this.nOfDeaths++;
    }

    public void setCurrentTile(Tile currentTile) {
        this.currentTile = currentTile;
    }

    /**
     * method called when the player receives marks from another player
     * @param newMark the marks dealt to him
     * @param marker the player that has given marks
     * @param gameManager
     */
    public void addMarks(int newMark,Player marker,GameManager gameManager){
        int oldMarks=0;
       if(marks.containsKey(marker)){
           if(marks.get(marker)+newMark>=3) {
               oldMarks=marks.get(marker);
               marks.replace(marker, marks.get(marker),3);
               gameManager.notifyOnMark(this,marker,3,oldMarks);
           }
           else {
               oldMarks=marks.get(marker);
               marks.replace(marker, marks.get(marker),marks.get(marker) + newMark);
               gameManager.notifyOnMark(this,marker,oldMarks+newMark,oldMarks);
           }
       }
       else
           if(newMark>=3) {
               marks.put(marker, 3);
               gameManager.notifyOnMark(this,marker,3,0);
           }
           else {
               marks.put(marker, newMark);
               gameManager.notifyOnMark(this,marker,newMark,0);
           }
    }

    public void setBoard(PointsBoard board){
        this.board=board;
    }

    /**
     * removes a weapon depending on the index
     * @param index the index of the weapon that has to be removed
     * @return the Weapon removed (null if index does not check)
     */
    public Weapon removeWeapon(int index){
        if(index<weapons.size()) {
            Weapon removedWeapon= weapons.remove(0);
            currentTile.addWeapon(removedWeapon);
            return removedWeapon;
        }
       return null;
    }

    /**
     * method called when a player picks an ammo tile
     * @param ammoTile the ammo tile drawn
     * @param gameManager
     */
    public void pickAmmoTile(AmmoTile ammoTile,GameManager gameManager){
        for (int i=0;i<ammoTile.getNOfPowerUp();i++){
            if(powerUps.size()==3)
                break;
            if(gameManager.getPowerUpDeck().isEmpty())
                gameManager.getPowerUpDeck().reShuffleAll();
            drawPowerUp(gameManager,gameManager.getPowerUpDeck().draw());
        }
        if(ammo.getRedValue()+ammoTile.getAmmoGained().getRedValue()>3){
            ammo.setRedValue(3);}
        else{
        ammo.setRedValue(ammo.getRedValue()+ammoTile.getAmmoGained().getRedValue());}
        if(ammo.getYellowValue()+ammoTile.getAmmoGained().getYellowValue()>3){
            ammo.setYellowValue(3);}
        else{
            ammo.setYellowValue(ammo.getYellowValue()+ammoTile.getAmmoGained().getYellowValue());}
        if(ammo.getBlueValue()+ammoTile.getAmmoGained().getBlueValue()>3){
            ammo.setBlueValue(3);}
        else {
            ammo.setBlueValue(ammo.getBlueValue() + ammoTile.getAmmoGained().getBlueValue());
        }

    }

    /**
     * method called when a player receives damage
     * the marks (related to the shooter) go down as damage
     * @param player the player that is dealing damage
     * @param damage the amount of damage
     * @param gameManager
     */
    public void addDamageTaken(Player player, int damage,GameManager gameManager){
        int contDamage=0;
        int contMarksDown=0;
        if(damageTaken.size() >= overkillLives) return;
        for(int i=0; i < damage; i++) {
            contDamage++;
            damageTaken.add(player);
            if(damageTaken.size() >= deadLives) {
                playerState = PlayerState.DEAD;
                if(damageTaken.size() == overkillLives) {
                    player.addMarks(1, this,gameManager);
                    break;
                }
            }
        }
        if(marks.containsKey(player)) {
            if (damageTaken.size() < overkillLives) {
                for (int i = 0; i < marks.get(player); i++) {
                    contMarksDown++;
                    damageTaken.add(player);
                    if (damageTaken.size() >= deadLives) {
                        playerState = PlayerState.DEAD;
                        if (damageTaken.size() == overkillLives) {
                            player.addMarks(1, this,gameManager);
                            marks.replace(player, 0);
                            break;
                        }
                    }
                }
                marks.replace(player, 0);
            } else {
                marks.replace(player, 0);
            }
        }
        gameManager.notifyOnDamage(player,this,contDamage,contMarksDown);
    }


    /**
     * trivially calls the GrabHandler method
     * @param gameManager
     * @return true if is valid grab
     */
    public boolean isValidGrab(GameManager gameManager) {
        return GrabHandler.isValidGrab(gameManager,this);
    }

    /**
     * trivially calls the ShootHandler method
     * @param gameManager
     * @return true if is valid shoot
     */
    public boolean isValidShoot(GameManager gameManager) {
        return ShootHandler.isValidShoot(this,gameManager);
    }

    /**
     * trivially calls the RunHandler method
     * @return true if run is valid
     */
    public boolean isValidMovement(){
            return RunHandler.isValidMovement(this);
    }

    /**
     * trivially calls the GrabHandler method
     * @param gameManager
     * @param t1 the Tile selected
     */
    public void grabActionAfterTile(GameManager gameManager, Tile t1) {
        GrabHandler.grabActionAfterTile(gameManager,this,t1);
    }

    /**
     * trivially calls the GrabHandler method
     * @param gameManager
     */
    public void chooseTileGrabAction(GameManager gameManager){
        GrabHandler.chooseTileGrabAction(gameManager,this);
    }

    /**
     * trivially calls the RunHandler method
     * @param gameManager
     */
    public void chooseTileMovementAction(GameManager gameManager){
        RunHandler.chooseTileMovementAction(gameManager,this);
    }

    /**
     * trivially calls the RunHandler method
     * @param gameManager
     * @param t1 the Tile selected
     */
    public void movementActionAfterTile(GameManager gameManager,Tile t1){
       RunHandler.movementActionAfterTile(gameManager,t1,this);
    }

    /**
     * trivially calls the RunHandler method
     * @param gameManager
     * @param t1 the tileView selected
     * @return true if the tile selected is valid
     * @throws MovementException if the tile selected is not valid
     */
    public Tile isAValidMovementAction(GameManager gameManager,TileView t1) throws MovementException{
        return RunHandler.isAValidMovementAction(gameManager,t1);
}

    /**
     * trivially calls the ShootHandler
     * @param gameManager
     * @param t1 the tileView selected
     * @return the corresponding Tile in model
     * @throws ShootingException if the tileView selected is not valid
     */
    public Tile isAValidShootingAction(GameManager gameManager,TileView t1) throws ShootingException {
        return ShootHandler.isAValidShootingAction(this,gameManager,t1);
    }

    /**
     * trivially calls the GrabHandler
     * @param gameManager
     * @param t1 the tileView selected
     * @return the corresponding Tile in model
     * @throws GrabException if the TileView selected is not valid
     */
    public Tile isAValidGrabAction(GameManager gameManager,TileView t1) throws GrabException{
        return GrabHandler.isAValidGrabAction(gameManager,this,t1);
    }

    /**
     * trivially calls the ShootHandler
     * @param gameManager
     */
    public void chooseTileShootingAction(GameManager gameManager){
        ShootHandler.chooseTileShootingAction(this,gameManager);
    }

    /**
     * trivially calls the ShootHandler
     * @param t1 the Tile selected to move before shooting
     * @param gameManager
     */
    public void shootingActionAfterTile(Tile t1,GameManager gameManager){
        ShootHandler.ShootingActionAfterTile(gameManager,this,t1);
    }

    /**
     * method used to check if a weapon is available and valid to shoot
     * @param gameManager
     * @param w the weapon selected
     * @return the corresponding Weapon if the selection is valid
     * @throws GameException if the Weapon selected is not valid
     */
    public Weapon isAValidWeaponToUse(GameManager gameManager,Weapon w) throws GameException{
        for (Weapon weapon : gameManager.getCurrentTurn().getPossibleChoices().getSelectableWeapons())
            if (weapon.equals(w))
                return weapon;
        throw  new WeaponsException("can't shoot with this weapon");
    }

    /**
     * method that checks if a weapon is available for reload
     * @param gameManager
     * @param w the weapon selected
     * @return the corresponding weapon in model
     * @throws GameException if the weapon selected is not valid
     */
    public Weapon isAValidWeaponToReload(GameManager gameManager,Weapon w) throws  GameException{
        for (Weapon weapon : gameManager.getCurrentTurn().getPossibleChoices().getSelectableWeapons())
            if (weapon.equals(w))
                return weapon;
        throw  new WeaponsException("can't reload this weapon");
    }

    /**
     * method that checks if a weapon is available to shoot
     * @param gameManager
     * @param weapon the weapon selected
     * @return  the corresponding weapon in model
     * @throws WeaponsException if the weapon selected is not valid
     */
    public Weapon isAValidWeaponToShoot(GameManager gameManager,Weapon weapon) throws WeaponsException{
        for(Weapon w:gameManager.getCurrentTurn().getPossibleChoices().getSelectableWeapons())
            if(w.equals(weapon))
                return w;
      throw new WeaponsException("Not valid weapon to shoot");
    }

    /**
     * method to get the ammo the player can pay for a cost
     * @param gameManager
     */
    public void chooseAmmoToDiscard(GameManager gameManager){
        List<AmmoColor> ret= new ArrayList<>();
        if(ammo.getBlueValue()>0)
            ret.add(AmmoColor.BLUE);
        if(ammo.getRedValue()>0)
            ret.add(AmmoColor.RED);
        if(ammo.getYellowValue()>0)
            ret.add(AmmoColor.YELLOW);
        gameManager.getCurrentTurn().getPossibleChoices().setSelectableAmmo(ret);
        setTurnState(TurnState.CHOOSE_AMMOS);
        notifyAmmos(gameManager);
    }

    /**
     * method to check if the ammo selected is available for cost
     * @param gameManager
     * @param ammoColor the selected ammo
     * @return true if the ammo is available to pay
     * @throws AmmoException
     */
     public boolean isAValidAmmo(GameManager gameManager,AmmoColor ammoColor) throws AmmoException{
        if(gameManager.getCurrentTurn().getPossibleChoices().getSelectableAmmo().contains(ammoColor))
            return true;
        throw new AmmoException("error caused by choosing the wrong type of ammo");
     }


    /**
     * method called after the player has selected the powerUp action.
     * It notifies the client about the possible powerUps he can play
     * @param gameManager
     */
     public void separatedPowerUpAction(GameManager gameManager){
        gameManager.getCurrentTurn().getPossibleChoices().setSelectablePowerUps(getValidPowerUps(gameManager));
        setTurnState(CHOOSE_POWERUP);
        notifyPowerUps(gameManager);
     }

    /**
     * method to start the powerUp effect after player has selected one
     * @param gameManager
     * @param powerUp the powerUp selected for action
     */
     public void afterChosenPowerUp(GameManager gameManager,PowerUp powerUp){
            inUsePowerUp=powerUp;
            powerUps.remove(powerUp);
            gameManager.getPowerUpDeck().addToDiscardPile(powerUp);
            gameManager.notifyOnPowerUpUsed(this,powerUp);
            powerUp.payCost(gameManager,this);
    }

    /**
     * method to call after the ammo is selected to pay cost
     * @param gameManager
     * @param ammoColor the ammo paid
     */
    public void afterChooseAmmo(GameManager gameManager,AmmoColor ammoColor) {
        switch(ammoColor){
            case RED:
                ammo.setRedValue(ammo.getRedValue()-1);
                break;
            case BLUE:
                ammo.setBlueValue(ammo.getBlueValue()-1);
                break;
            case YELLOW:
                ammo.setYellowValue(ammo.getYellowValue()-1);
                break;
        }
        if (gameManager.getCurrentTurn().getCurrentWeaponToReload() != null) {
            if (gameManager.getCurrentTurn().topEffect() != null) {
                if (inUsePowerUp != null){
                    inUsePowerUp.startEffect(gameManager,this);
                }
                else
                    gameManager.getCurrentTurn().topEffect().startingExecution(gameManager.getCurrentTurn().topActions().get(0), gameManager, this);
            } else {
                reloadWeapon(gameManager,gameManager.getCurrentTurn().getCurrentWeaponToReload());
            }
        }
        else {
            inUsePowerUp.startEffect(gameManager,this);
        }
    }

    /**
     * method to retrieve the possible weapons to shoot
     * @param gameManager
     */
    public void chooseWeaponToShoot(GameManager gameManager){
        gameManager.getCurrentTurn().getPossibleChoices().setSelectableWeapons(getValidWeapons(gameManager));
        setTurnState(CHOOSE_WEAPON_HAND);
        notifyWeapons(gameManager);
    }

    /**
     * method to retrieve (in final frenzy) if a player needs to recharge a weapon if he wants to shoot
     * This choice is mandatory, because if none of this weapons are selected the player would just
     * skip the shoot action
     * @param gameManager
     * @return the list of mandatory weapons
     */
    public List<Weapon> getMandatoryWeapon(GameManager gameManager) {
        List<Weapon> ret = new ArrayList<>();
        LoadedState loadedState;
        for (Weapon w : weapons) {
            if (w.isValid(this, gameManager))
                return new ArrayList<>();
        }
        for (Weapon w : weapons) {
            loadedState = w.getLoaded();
            w.setLoaded(LoadedState.LOADED);
            if ((loadedState == LoadedState.UNLOADED) && (w.isValid(this, gameManager)))
                ret.add(w);
            w.setLoaded(loadedState);
        }
        return ret;
    }

    /**
     * method called after the player selection for a weapon to choose. It calls the weapon
     * method to start with the possible effects
     * @param gameManager
     * @param w the weapon selected to shoot
     */
    public void startShootingWithAWeapon(GameManager gameManager,Weapon w){
        gameManager.getCurrentTurn().setCurrentWeapon(w);
        w.setLoaded(LoadedState.UNLOADED);
        gameManager.notifyOnWeaponUsed(this,w);
        w.beforeBaseEffect(gameManager,this);
    }

    /**
     * the possible weapons to shoot
     * @param gameManager
     * @return the list of possible weapons to shoot
     */
    public List<Weapon> getValidWeapons(GameManager gameManager){
        List<Weapon> ret= new ArrayList<>();
        for(Weapon w: weapons)
            if(w.isValid(this,gameManager))
                ret.add(w);
        return ret;
    }

    /**
     * method to retrieve the possible weapons a player can grab
     * @param t1 the tile where the player wants to grab
     * @return the list of possible weapons
     */
    public List <Weapon> getPossibleWeaponToGrab(Tile t1){
        List <Weapon> ret= new ArrayList<>();
        for (Weapon w: t1.getWeapons())
            if (getPotentialAmmos().hasCorrectCost(w.getPartiallyLoadedCost()))
                ret.add(w);
        return ret;
    }

    /**
     * trivially calls the GrabHandler method.
     * @param gameManager
     * @param newWeapon the weapon selected
     * @return the corresponding weapon in model
     * @throws AmmoException if the selected weapon is not valid (no ammos)
     * @throws WeaponsException if the selected weapon is not valid
     */
    public Weapon isAnAvailableWeapon(GameManager gameManager,Weapon newWeapon) throws AmmoException, WeaponsException{
       return GrabHandler.isAnAvailableWeapon(gameManager,newWeapon,this);
    }

    /**
     * trivially calls the GrabHandler method
     * @param newWeapon the weapon picked
     * @param gameManager
     */
    public void pickWeapon( Weapon newWeapon,GameManager gameManager) {
        GrabHandler.pickWeapon(newWeapon,gameManager,this);
    }

    /**
     * method called after a player has decided how to pay the cost of the weapon he's grabbing
     * the weapon is picked up and removed from the tile
     * @param newWeapon the weapon grabbed
     * @param gameManager
     * @param removedWeapon the weapon dropped (can be null)
     */
    public void loadWeaponGrabbed(Weapon newWeapon,GameManager gameManager,Weapon removedWeapon){
        payCost(gameManager,newWeapon.getPartiallyLoadedCost());
        gameManager.getCurrentTurn().setCurrentWeapon(null);
        newWeapon.setLoaded(LoadedState.LOADED);
        gameManager.notifyOnWeaponGrab(this,newWeapon,removedWeapon);
        handleActions(gameManager);

    }

    /**
     * this method is called after an action in finished
     * @param gameManager
     */
    public void handleActions(GameManager gameManager){
        setTurnState(TurnState.READY_FOR_ACTION);
        gameManager.getCurrentTurn().setNOfActionMade(gameManager.getCurrentTurn().getNOfActionMade()+1);
        if(getPossibleActions(gameManager).isEmpty())
            handleReload(gameManager);
        else {
            gameManager.getCurrentTurn().getPossibleChoices().setSelectableActions(getPossibleActions(gameManager));
            notifyOnActions(gameManager);
        }
    }


    /**
     * method to retreive the valid powerUps that can be played
     * @param gameManager
     * @return the list of valid powerUps
     */
    public List<PowerUp> getValidPowerUps(GameManager gameManager){
        List<PowerUp> ret = new ArrayList<PowerUp>();
        for (PowerUp powerUp : powerUps){
            if (powerUp.isValid(gameManager,this)){
                ret.add(powerUp);
            }
        }
        return ret;
    }

    /**
     * method used to check if a player has valid powerUp he can use
     * @param gameManager
     * @return
     */
    public boolean hasValidPowerUp(GameManager gameManager){
        for (PowerUp powerUp : powerUps){
            if (powerUp.isValid(gameManager,this))
                return true;
        }
        return false;
    }

    public List<Weapon> dropAllWeapons(){
        List<Weapon> returnList = new ArrayList<>(this.weapons);
        this.weapons.clear();
        return returnList;
    }

    /**
     * it trivially calls the Grab Handler method
     * @param gameManager
     * @param w the weapon chosen
     * @return the Weapon selected
     * @throws WeaponsException if the weapon is not valid
     */
    public Weapon isAValidWeaponToDrop(GameManager gameManager,Weapon w) throws WeaponsException{
        return GrabHandler.isAValidWeaponToDrop(gameManager,w);
    }

    /**
     * method called after a weapon has been dropped to grab a new one
     * @param removedWeapon the removed weapon
     * @param gameManager
     */
    public void swapWeapon(Weapon removedWeapon,GameManager gameManager) {
     GrabHandler.swapWeapon(removedWeapon,gameManager,this);
    }

    /**
     * method called after a weapon has been grabbed
     * @param gameManager
     */
    public void afterWeaponGrabbed(GameManager gameManager){
        GrabHandler.afterWeaponGrabbed(this,gameManager);
    }

    /**
     * the player adds a powerUp in his hand
     * this can surpass the limit of 3 powerUps in the hand.
     * But this is only when the player needs to spawn so he needs to discard one powerUp
     * to complete the operation
     * @param gameManager
     * @param powerUp
     */
    public void addPowerUp(GameManager gameManager,PowerUp powerUp){
        powerUps.add(powerUp);
        gameManager.notifyOnDrawnPowerUp(this, powerUp);
    }

    /**
     * the player draws a powerUp
    * if another power up cannot be drawn, it returns the power up that has removed from the deck or it will be lost
    * return null otherwise
    * */
    public PowerUp drawPowerUp(GameManager gameManager,PowerUp powerUp){
        if(powerUps.size() >= 3) {
            return powerUp;
        }
        powerUps.add(powerUp);
        gameManager.notifyOnDrawnPowerUp(this, powerUp);
        return null;
    }

    /**
     * the player discards a powerUp basing on the index
     * @param index the index we want to be removed
     * @return the powerUp discarded (null if not found)
     */
    public PowerUp discardPowerUp(int index){
        if(index < 0 || index >= powerUps.size())
            return null;
        return powerUps.remove(index);
    }

    public void discardPowerUp(GameManager gameManager,PowerUp p){
        powerUps.remove(p);
        gameManager.getPowerUpDeck().addToDiscardPile(p);
        gameManager.notifyOnDiscardPowerUp(this,p);
    }

    /**
     * this method is used to see the potential ammos that a player has, considering all powerUps
     * but not considering the powerUp p that the player wants to use
     * @param p the powerUp the player wants to use
     * @return
     */
    public Ammo getPotentialAmmos(PowerUp p){
       Ammo ret=getPotentialAmmos();
       switch (p.getColor()){
           case BLUE: {
               if(ret.getBlueValue()>0)
                   ret.setBlueValue(ret.getBlueValue()-1);
               break;}
           case RED: {
               if(ret.getRedValue()>0)
                   ret.setRedValue(ret.getRedValue()-1);
               break;}
           case YELLOW: {
               if(ret.getYellowValue()>0)
                   ret.setYellowValue(ret.getYellowValue()-1);
               break;}
           default: break;
       }
       return ret;
    }

    /**
     * this method calculates the possible ammos (considering powerUps) that a player has
     * to pay a cost
     * @return the potential ammo
     */
    public Ammo getPotentialAmmos(){
        Ammo ret=new Ammo(ammo.getRedValue(),ammo.getBlueValue(),ammo.getYellowValue());
        for(PowerUp p: powerUps){
            switch (p.getColor()){
                case RED:{
                    ret.setRedValue(ammo.getRedValue()+1);
                    break;}
                case BLUE:{
                    ret.setBlueValue(ammo.getBlueValue()+1);
                    break;}
                case YELLOW:{
                    ret.setYellowValue(ammo.getYellowValue()+1);
                    break;}
                default:
                    break;
            }}
        return ret;
    }

    /**
     * method called to pay a cost
     * @param gameManager
     * @param cost the cost to pay
     */
    public void payCost(GameManager gameManager,Ammo cost){
        ammo.setYellowValue(ammo.getYellowValue()-cost.getYellowValue());
        ammo.setRedValue(ammo.getRedValue()-cost.getRedValue());
        ammo.setBlueValue(ammo.getBlueValue()-cost.getBlueValue());
        gameManager.getCurrentTurn().resetPowerUps();
    }

    /**
     * method used after the player has chosen to discard a powerUp for the cost.
     * it temporarily add another ammo related to the color of the powerUp
     * IMPORTANT: this method can surpass the limit of 3 ammo per color but it's just temporary.
     * after paying the cost, everything will be under the rules
     * @param p  the powerUp discarded
     * @param gameManager
     */
    public void discardPowerUpForAmmos(PowerUp p,GameManager gameManager){
        if (turnState==DISCARD_POWERUP_FOR_COST_POWERUP) {
            discardPowerUp(gameManager, p);
            if(inUsePowerUp.getPowerUpEffect().getCost().getYellowValue()<0){
                inUsePowerUp.startEffect(gameManager,this);
                return;}
        }
        switch (p.getColor()){
            case YELLOW:{
                ammo.setYellowValue(ammo.getYellowValue()+1);
                break;}
            case RED:{
                ammo.setRedValue(ammo.getRedValue()+1);
                break;}
            case BLUE:{
                ammo.setBlueValue(ammo.getBlueValue()+1);
                break;}
            default:
                break;
        }
        discardPowerUp(gameManager,p);
        gameManager.getCurrentTurn().addPowerUpForCost(p);
        }

    /**
     * this method calculates the powerUp that an be discarded to pay a cost (not for a powerUp cost)
     * @param cost the cost to pay
     * @param gameManager
     * @return the list of possible powerUps
     */
    public List<PowerUp> getPowerUpToDiscardForCost(Ammo cost,GameManager gameManager){
       if(gameManager.getCurrentTurn().getAlreadyPaidCost().hasCorrectCost(cost))
           return new ArrayList<>();
       Ammo alreadyPaid=gameManager.getCurrentTurn().getAlreadyPaidCost();
       int redValueToFill=cost.getRedValue()-alreadyPaid.getRedValue();
       int yellowValueToFill=cost.getYellowValue()-alreadyPaid.getYellowValue();
       int blueValueToFill=cost.getBlueValue()-alreadyPaid.getBlueValue();
       if((cost.getBlueValue()<0)){
           return powerUps;}
       return computePowerUps(redValueToFill,blueValueToFill,yellowValueToFill);
    }

    /**
     * this method calculates if there are powerUp that are mandatory in order to pay a cost
     * @param cost the cost the player needs to pay
     * @param gameManager
     * @return the list of mandatory powerUps (empty if not mandatory)
     */
    public List<PowerUp> getMandatoryPowerUpsToDiscard(Ammo cost,GameManager gameManager){
        Ammo alreadyPaid=gameManager.getCurrentTurn().getAlreadyPaidCost();
        int redValueToFill=-(ammo.getRedValue()-cost.getRedValue()+alreadyPaid.getRedValue());
        int yellowValueToFill=-(ammo.getYellowValue()-cost.getYellowValue()+alreadyPaid.getYellowValue());
        int blueValueToFill=-(ammo.getBlueValue()-cost.getBlueValue()+alreadyPaid.getBlueValue());
       return computePowerUps(redValueToFill,blueValueToFill,yellowValueToFill);
    }

    private List<PowerUp> computePowerUps(int redValueToFill,int blueValueToFill,int yellowValueToFill){
        List<PowerUp> ret=new ArrayList<>();
        for(PowerUp p: powerUps){
            switch (p.getColor()){
                case RED:{
                    if(redValueToFill>0) ret.add(p);
                    break;}
                case YELLOW:{
                    if (yellowValueToFill>0) ret.add(p);
                    break;}
                case BLUE: {
                    if(blueValueToFill>0) ret.add(p);
                    break;}
                default: break;
            }
        }
        return ret;
    }

    /**
     * this method calculates the powerUps that can be discarded to pay the cost of a powerUp
     * @param cost the cost to pay
     * @param p the powerUp the player wants to use
     * @param gameManager
     * @return
     */
    public List<PowerUp> getPowerUpToDiscardForCost(Ammo cost, PowerUp p,GameManager gameManager){
        List<PowerUp> ret=getPowerUpToDiscardForCost(cost,gameManager);
        ret.remove(p);
        return ret;
    }

    /**
     * this method calculates the possible weapons that can be reloaded
     * @return
     */
    public List<Weapon> getPossibleWeaponsToReload(){
        List<Weapon> ret= new ArrayList<>();
        for (Weapon w: weapons){
            if (getPotentialAmmos().hasCorrectCost(w.getReloadCost())&&(w.getLoaded()==LoadedState.UNLOADED)){
                ret.add(w);}}
        return ret;}

    /**
     * method called when a player has decided to reload a weapon. It reloads it or asks the client about powerUps he want to use
     * @param gameManager
     * @param w the weapon chosen
     */
    public void startReloading(GameManager gameManager,Weapon w){
        gameManager.getCurrentTurn().setCurrentWeaponToReload(w);
        if(getPowerUpToDiscardForCost(w.getReloadCost(),gameManager).isEmpty()) {
            reloadWeapon(gameManager, w);
            TurnStateHandler.handleAfterReload(gameManager,this);
        }
        else {
            gameManager.getCurrentTurn().getPossibleChoices().setSelectablePowerUps(getPowerUpToDiscardForCost(w.getReloadCost(),gameManager));
            if(gameManager.getCurrentTurn().getNOfActionMade()>=currentTurnAction.getMaxNOfActions())
                setTurnState(DISCARD_POWERUP_FOR_COST_WEAPON);
            else
                setTurnState(DISCARD_POWERUP_FOR_COST_WEAPON_IN_ACTION);
            notifyPowerUps(gameManager);
        }
    }

    /**
     * method used to reload a specific weapon
     * @param gameManager
     * @param weapon the weapon reloaded
     */
    public void reloadWeapon(GameManager gameManager,Weapon weapon) {
        payCost(gameManager, weapon.getReloadCost());
        weapon.setLoaded(LoadedState.LOADED);
        gameManager.notifyOnReloadWeapon(this, weapon);
    }

    /**
     * this method create the list of the possible actions that can be performed by a player
     * @param gameManager
     * @return
     */
    public  List<TypeOfAction> getPossibleActions(GameManager gameManager){
        List<TypeOfAction> ret=new ArrayList<>();
        if(isValidMovement())
            ret.add(TypeOfAction.RUN);
        if(isValidShoot(gameManager))
            ret.add(TypeOfAction.SHOOT);
        if(isValidGrab(gameManager))
            ret.add(TypeOfAction.GRAB);
        if(hasValidPowerUp(gameManager))
            ret.add(TypeOfAction.POWER_UP);
        if(gameManager.getCurrentTurn().getNOfActionMade() >= getCurrentTurnAction().getMaxNOfActions()){
            ret.remove(TypeOfAction.RUN);
            ret.remove(TypeOfAction.GRAB);
            ret.remove(TypeOfAction.SHOOT);
        }
        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if(obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Player player = (Player) obj;
        if(playerID == null) {
            return player.getPlayerID() == null;
        }
        return playerID.equals(player.getPlayerID());
    }

    @Override
    public int hashCode() {
        if(playerID == null) {
            return 0;
        }
        return playerID.hashCode();
    }

    /**
     * method used to notify the player about the powerUps he can choose
     * @param gameManager
     */
    public void notifyPowerUps(GameManager gameManager){
        PlayerView playerView = createPlayerView();
        TileView tileView = GameManager.createTileView(currentTile);
        playerView.setTileView(tileView);
        notifyPrintHelp(gameManager);
        viewPlayer.onPowerUps(gameManager.getCurrentTurn().getPossibleChoices().getSelectablePowerUps());
        viewPlayer.onUpdateCurrentPlayer(playerView, true);
    }

    /**
     * methd used to create a PlayerView from this Player object
     * @return the corresponding PlayerView
     */
    public PlayerView createPlayerView() {
        Map<PlayerColor, Integer> colorMarks = new HashMap<>();
        for(Player p : marks.keySet()) {
            colorMarks.put(p.getPlayerColor(), marks.get(p));
        }
        List<PlayerColor> damages = new ArrayList<>();
        for(Player p : damageTaken) {
            damages.add(p.getPlayerColor());
        }
        return new PlayerView(getPlayerID(), getPlayerColor(), getPlayerState(), getAmmo(), getNOfKills(), getNOfPowerUps(),
                colorMarks, damages, getNOfDeaths(), null, getUnloadedWeapons(),getNOfLoadedWeapons(), getCurrentTurnAction(), getBoard());
    }

    /**
     * notify the player about the ammo he can choose
     * @param gameManager
     */
    public void notifyAmmos(GameManager gameManager){
        notifyPrintHelp(gameManager);
        viewPlayer.onAmmos(gameManager.getCurrentTurn().getPossibleChoices().getSelectableAmmo());
    }

    /**
     * notify the client about the weapons he can choose
     * @param gameManager
     */
    public void notifyWeapons(GameManager gameManager){
        notifyPrintHelp(gameManager);
        viewPlayer.onWeapons(gameManager.getCurrentTurn().getPossibleChoices().getSelectableWeapons());
    }

    /**
     * notify the client about the tiles he can choose
     * @param gameManager
     */
    public void notifyTiles(GameManager gameManager){
        List<TileView> tileViews = new ArrayList<>();
        for(Tile t: gameManager.getCurrentTurn().getPossibleChoices().getSelectableTiles()) {
            tileViews.add(GameManager.createTileView(t));
        }
        notifyPrintHelp(gameManager);
        viewPlayer.onTiles(tileViews);
    }

    /**
     * notify the client about the action he can choose
     * @param gameManager
     */
    public void notifyOnActions(GameManager gameManager){
        gameManager.getCurrentTurn().getPossibleChoices().setSelectableActions(getPossibleActions(gameManager));
        List<TypeOfAction> ret=gameManager.getCurrentTurn().getPossibleChoices().getSelectableActions();
        notifyOnLog(currentTurnAction.getMaxNOfActions()-gameManager.getCurrentTurn().getNOfActionMade()+" action(s) remaining!");
        notifyPrintHelp(gameManager);
        viewPlayer.onActions(ret);
    }

    /**
    * method used to notify the client about the possible commands
    */
    public void notifyPossibleCommands() {
        viewPlayer.requestPossibleCommands();
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    /**
     * method to handle the reload after having finished the possible action for the turn
     * @param gameManager
     */
    public void handleReload(GameManager gameManager) {
            if (getPossibleWeaponsToReload().isEmpty()) {
                turnState = TurnState.END_OF_TURN;
                gameManager.changeTurn();
            } else {
                gameManager.getCurrentTurn().getPossibleChoices().setSelectableWeapons(getPossibleWeaponsToReload());
                turnState = TurnState.CHOOSE_WEAPON_TO_RELOAD;
                notifyOnLog("Choose a weapon to reload");
                notifyWeapons(gameManager);
            }
    }

    /**
     *
     * @param p the chosen powerUp
     * @param gameManager
     * @return the corresponding powerUp in possible choices
     * @throws GameException if it's not a selectable powerUp
     */
    public PowerUp isASelectablePowerUp(PowerUp p,GameManager gameManager) throws GameException {
        for(PowerUp powerUp: gameManager.getCurrentTurn().getPossibleChoices().getSelectablePowerUps())
            if(powerUp.getId() == p.getId())
                return powerUp;
        for(PowerUp powerUp1:powerUps)
            if((powerUp1.getId() == p.getId())&&(!p.isValid(gameManager,this)))
                throw new PowerUpException("can't be used now");
        throw  new PowerUpException("you don't own this powerUp!");
    }

    /**
     * method used to check if it's a valid action or not
     * @param typeOfAction the typeofAction chosen
     * @param gameManager
     * @return true if it's valid, false otherwise
     * @throws GameException if it's not a valid action
     */
    public boolean isValidAction(TypeOfAction typeOfAction,GameManager gameManager) throws GameException{
        for(TypeOfAction t:gameManager.getCurrentTurn().getPossibleChoices().getSelectableActions())
            if(t.equals(typeOfAction))
                return true;
        throw  new WrongChoiceException("can't do this action now!");
    }

    public void notifyOnLog(String logUpdate) {
        viewPlayer.onText(logUpdate);
    }

    public void notifyPrintHelp(GameManager gameManager) {
        viewPlayer.onPrintHelp(gameManager,this);
    }

    public void setDeaths(int newDeath) {
        this.nOfDeaths=0;
    }
}

