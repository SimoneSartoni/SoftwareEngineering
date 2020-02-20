package model.utility;

import model.board.Tile;
import model.enums.Direction;
import model.player.Player;
import model.powerup.PowerUp;
import model.weapon.Effect;
import model.weapon.Weapon;
import model.weapon.actions.Action;

import java.io.Serializable;
import java.util.*;

public class CurrentTurn implements Serializable {
    /**
     * the selected direction for shooting
     */
    private Direction direction;
    /**
     * the hit players in the effects of the current weapon
     */
    private Map<Effect,List<Player>> alreadyHitPlayers;
    /**
     * the hit tiles in the effects of the current weapons
     */
    private Map<Effect,List<Tile>> alreadyHitTile;
    /**
     * the list of players interacted in current action
     */
    private List<Player> interactedInCurrentAction;
    /**
     * the current weapon to reload
     */
    private Weapon currentWeaponToReload;
    /**
     * the marks to deal at the end of the weapon action
     */
    private Map<Player, Integer> marks;
    /**
     * the current weapon to drop
     */
    private Weapon currentWeaponToDrop;
    /**
     * the paid cost (in powerUps) of the current cost to pay
     */
    private List<PowerUp> alreadyPaidCostPowerUps;

    /**
     * stack of effect used with linked effects it contains the effects with the actions remaining
     * it works as a stack
     */
    public class EffectActions implements Serializable{
        private Effect effect;
        private List<Action> actionsRemaining;

        public EffectActions(Effect effect, List<Action> actions) {
            this.effect = effect;
            this.actionsRemaining = new ArrayList<>();
            actionsRemaining.addAll(actions);
        }

        public List<Action> getActions() { return actionsRemaining; }

        public Effect getEffect() { return effect; }

        public void setEffect(Effect effect) { this.effect = effect; }

        public void setActions(List<Action> actions) { actionsRemaining.addAll(actions); }

        public void removeAction() { actionsRemaining.remove(0); }

        public void addMarks(Player player, int amount){
            if(! marks.containsKey(player)){
                marks.put(player, amount);}
            else{
                marks.replace(player, marks.get(player),marks.get(player)+amount);
            }
        }

    }


    private Stack<EffectActions> stackOfEffects;
    /**
     * the list counter attacking players and their possible counterattacks
     */
    private Map <Player,List<PowerUp>> counterAttackingPlayersPowerUps;
    /**
     * the current n of action made in this turn
     */
    private int nOfActionMade;
    /**
     * the current weapon to shoot or to grab
     */
    private Weapon currentWeapon;
    /**
     * the current powerUp is being used
     */
    private PowerUp currentPowerUp;
    /**
     * the curent action
     */
    private Action currentAction;
    /**
     * the current player used to calculate visibility in the effects
     */
    private Player currentPlayerForVisibility;
    /**
     * possible choices of the player that is taking the choice in this moment
     */
    private PossibleChoices possibleChoices;
    /**
     * the chosen target of an effect
     */
    private Player chosenTarget;
    /**
     * the chosen tile of an effect
     */
    private Tile chosenTile;
    /**
     * the list of dead players in the turn
     */
    private List<Player> deadPlayers;

    public CurrentTurn(Direction direction, Map<Effect,List<Player>> alreadyHitPlayer, Map<Effect,List<Tile>> alreadyHitTile, List<Action> actionsRemaining, int nOfActionMade, Weapon currentWeapon, PowerUp currentPowerUp, Action currentAction) {
        this.direction = direction;
        this.alreadyHitPlayers = alreadyHitPlayer;
        this.alreadyHitTile = alreadyHitTile;
        this.counterAttackingPlayersPowerUps = new HashMap <>();
        this.nOfActionMade = nOfActionMade;
        this.currentWeapon = currentWeapon;
        this.stackOfEffects= new Stack<>();
        this.currentPowerUp = currentPowerUp;
        this.currentAction = currentAction;
        this.possibleChoices=new PossibleChoices();
        this.chosenTarget = null;
        this.chosenTile = null;
        this.deadPlayers = new ArrayList<>();
        this.marks = new HashMap<>();
        this.alreadyPaidCostPowerUps=new ArrayList<>();
    }

    public CurrentTurn(){
        this.direction=null;
        this.alreadyHitPlayers = new HashMap<>();
        this.alreadyHitTile = new HashMap<>();
        this.counterAttackingPlayersPowerUps = new HashMap <>();
        this.nOfActionMade=0;
        this.currentWeapon=null;
        this.currentAction=null;
        this.currentPowerUp=null;
        this.interactedInCurrentAction=new ArrayList<>();
        this.stackOfEffects= new Stack<>();
        this.possibleChoices= new PossibleChoices();
        this.deadPlayers = new ArrayList<>();
        this.marks = new HashMap<>();
        this.alreadyPaidCostPowerUps=new ArrayList<>();
    }



    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public Map<Effect,List<Player>> getAlreadyHitPlayer() {
        return alreadyHitPlayers;
    }

    public void setAlreadyHitPlayer(Map<Effect,List<Player>> alreadyHitPlayers) {
        this.alreadyHitPlayers = alreadyHitPlayers;
    }

    public Weapon getCurrentWeaponToDrop() {
        return currentWeaponToDrop;
    }

    public Ammo getAlreadyPaidCost() {
        Ammo ret = new Ammo(0, 0, 0);
        for (PowerUp p : alreadyPaidCostPowerUps) {
            switch (p.getColor()) {
                case BLUE: {
                    ret.setBlueValue(ret.getBlueValue() + 1);
                    break;
                }
                case YELLOW: {
                    ret.setYellowValue(ret.getYellowValue() + 1);
                    break;
                }
                case RED: {
                    ret.setRedValue(ret.getRedValue() + 1);
                    break;
                }
                default:
                    break;
            }
        }
        return ret;
    }

    public void addPowerUpForCost(PowerUp powerUp){
        alreadyPaidCostPowerUps.add(powerUp);
    }

    public List<Player> getDeadPlayers() {
        return deadPlayers;
    }
    public void addDeadPlayer(Player p){
        deadPlayers.add(p);
    }

    public List<Player> getInteractedInCurrentAction() {
        return interactedInCurrentAction;
    }

    public void addInteractedInCurrentAction(Player p){
        interactedInCurrentAction.add(p);
    }

    public Map<Effect,List<Tile>> getAlreadyHitTile() {
        return alreadyHitTile;
    }

    public void setAlreadyHitTile(Map<Effect,List<Tile>> alreadyHitTile) {
        this.alreadyHitTile = alreadyHitTile;
    }

    public  Map <Player,List<PowerUp>> getCounterAttackingPlayers() {
        return counterAttackingPlayersPowerUps;
    }

    public void addMark(Player p,int marks){
        stackOfEffects.peek().addMarks(p,marks);
    }


    public void popEffect(){
        stackOfEffects.pop();
    }

    public Effect topEffect(){
        if (stackOfEffects.isEmpty())
            return null;
        return stackOfEffects.peek().getEffect();
    }

        public List<Action> topActions(){
        return stackOfEffects.peek().getActions();
    }

    public void pushEffect(Effect effect, List<Action> actions){
        stackOfEffects.add(new EffectActions(effect,actions));
    }
    public int getNOfActionMade() {
        return nOfActionMade;
    }

    public void setNOfActionMade(int nOfActionMade) {
        this.nOfActionMade = nOfActionMade;
    }

    public Weapon getCurrentWeapon() {
        return currentWeapon;
    }

    public Player getCurrentPlayerForVisibility() {
        return currentPlayerForVisibility;
    }

    public void setCurrentPlayerForVisibility(Player currentPlayerForVisibility) {
        this.currentPlayerForVisibility = currentPlayerForVisibility;
    }

    public void setCurrentWeaponToDrop(Weapon currentWeaponToDrop) {
        this.currentWeaponToDrop = currentWeaponToDrop;
    }

    public void setCurrentWeaponToReload(Weapon currentWeaponToReload){
        this.currentWeaponToReload=currentWeaponToReload;
    }

    public void setCurrentWeapon(Weapon currentWeapon) {
        this.currentWeapon = currentWeapon;
    }

    public PowerUp getCurrentPowerUp() {
        return currentPowerUp;
    }

    public void setCurrentPowerUp(PowerUp currentPowerUp) {
        this.currentPowerUp = currentPowerUp;
    }

    public Action getCurrentAction() {
        return currentAction;
    }

    public void setCurrentAction(Action currentAction) {
        this.currentAction = currentAction;
    }

    public PossibleChoices getPossibleChoices() {
        return possibleChoices;
    }

    public Weapon getCurrentWeaponToReload() {
        return currentWeaponToReload;
    }

    public Player getChosenTarget() {
        return chosenTarget;
    }

    public void setChosenTarget(Player choosenTarget) {
        this.chosenTarget = choosenTarget;
    }

    public Tile getChosenTile() {
        return chosenTile;
    }

    public void setChosenTile(Tile choosenTile) {
        this.chosenTile = choosenTile;
    }

    public Map<Player, Integer> getMarks() { return marks; }

    public void resetStack(){
       while(!stackOfEffects.isEmpty())
           stackOfEffects.pop();
    }


    /**
     * resets after a cost has been paid
     */
    public void resetPowerUps(){
        alreadyPaidCostPowerUps.clear();
    }

    /**
     * resets after the weapon has been used
     */
    public void resetAfterWeapon(){
        direction=null;
        alreadyHitPlayers.clear();
        alreadyHitTile.clear();
        interactedInCurrentAction.clear();
        marks.clear();
        resetStack();
        chosenTarget=null;
        chosenTile=null;
        currentWeapon=null;
        currentPowerUp=null;
        currentPlayerForVisibility=null;
        currentAction=null;
        possibleChoices.clear();

    }

    /**
     * resets the current turn at the end of the turn
     */
    public void reset() {
        resetAfterWeapon();
        nOfActionMade=0;
        counterAttackingPlayersPowerUps.clear();
        currentWeaponToReload=null;
        currentWeaponToDrop=null;
        alreadyPaidCostPowerUps.clear();
        possibleChoices.clear();
        deadPlayers.clear();
    }
}
