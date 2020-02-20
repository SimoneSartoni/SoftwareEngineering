package model.powerup;

import model.board.GameManager;
import model.player.Player;
import model.utility.Ammo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PowerUpEffect implements Serializable{
    /**
     * the cost to pay
     */
    private Ammo cost;
    /**
     * the damage to deal(can be 0)
     */
    private int dmg;
    /**
     * the min movement for a movement action (greater than 0 is movement is required)
     */
    private int minMovement;
    /**
     * the max movement for a movement action(equals 0 if movement is not to be considered)
     */
    private int maxMovement;
    /**
     * the marks to deal (can be 0)
     */
    private int marks;
    /**
     * if the action regards tiles or targets
     */
    private boolean aoe;
    /**
     * true if enemy needs to be chosen
     */
    private boolean targetEnemy;
    /**
     * if the movement and all related actions are on a direction
     */
    private boolean straightForward;

    public PowerUpEffect(Ammo cost, int dmg, int minMovement, int maxMovement, int marks, boolean aoe, boolean targetEnemy,boolean straightForward) {
        this.cost = cost;
        this.dmg = dmg;
        this.minMovement = minMovement;
        this.maxMovement = maxMovement;
        this.marks = marks;
        this.aoe = aoe;
        this.targetEnemy = targetEnemy;
        this.straightForward=straightForward;
    }

    public boolean isValid(GameManager gameManager, Player player, PowerUp p) {
        List<Player> allPlayers=new ArrayList<>(gameManager.getOnBoardPlayers());
        List<Player> temp=new ArrayList<>(allPlayers);
        for(Player player1:temp)
            if(player1.equals(player))
                allPlayers.remove(player);
        if (!player.getPotentialAmmos(p).hasCorrectCost(cost))
            return false;
        if (targetEnemy)
            if (allPlayers.isEmpty())
                return false;
        return true;
    }


    public Ammo getCost() {
        return cost;
    }

    public boolean isTargetEnemy() {
        return targetEnemy;
    }

    public int getMaxMovement() {
        return maxMovement;
    }

    public int getMinMovement() {
        return minMovement;
    }

    public int getDmg() {
        return dmg;
    }

    public int getMarks() {
        return marks;
    }

    public boolean isAoe() {
        return aoe;
    }

    public boolean isStraightForward() {
        return straightForward;
    }

    @Override
    public boolean equals(Object obj) {
        PowerUpEffect p;
        if(obj instanceof PowerUpEffect){
            p=(PowerUpEffect) obj;
            return p.cost.equals(cost) && p.dmg == dmg && p.minMovement == minMovement && p.maxMovement == maxMovement &&
                    p.marks == marks && p.aoe == aoe && p.targetEnemy == targetEnemy;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}

