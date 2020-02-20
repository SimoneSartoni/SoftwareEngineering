package model.board;

import model.enums.PlayerColor;
import model.player.Player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KillShotTrack implements Serializable {
    /**
     * ArrayList of {@link Integer} representing how many points players score depending on the number of kills.
     */
    private List<Integer> killShotTrackScore;
    /**
     * HashMap between {@link PlayerColor} and {@link Integer} to indicated, for each Player, how many token he put on killshot track.
     * Used to give killshot track scores.
     */
    private Map<PlayerColor,Integer> deathCount;
    /**
     * ArrayList of {@link Player} representing the order with which the players kill.
     */
    private List<PlayerColor> deathOrder;
    /**
     * {@link Integer} representing the max number of lifes on the killshot track. Must be greater than 0.
     */

    private final int maxKills;

    private  List<Boolean> isDoubleKillDeathOrder;
    private int currentKills;
    private int pointsForDoubleKill;

    public KillShotTrack(List<Integer> killShotTrackScore, int maxKills, List<PlayerColor> players, int pointsForDoubleKill){
        this.killShotTrackScore = new ArrayList<>();
        if(killShotTrackScore!=null)
            this.killShotTrackScore.addAll(killShotTrackScore);
        deathCount= new HashMap<>();
        deathOrder=new ArrayList<>();
        isDoubleKillDeathOrder = new ArrayList<Boolean>();
        this.maxKills=maxKills;
        this.currentKills = 0;
        this.pointsForDoubleKill=pointsForDoubleKill;
    }
    public int getPointsForDoubleKill(){
        return pointsForDoubleKill;
    }

    public int getDeathLeft() {
        return maxKills-deathOrder.size();
    }

    public int getCurrentKill(){return deathOrder.size()+1;}

    /**
     * Methods to set a player killed someone. If there is also overkill, his count is increased by 2 instead of 1.
     * @param player {@link Player} doing the kill
     * @param overkill {@link Boolean} indicating if the player overkilled someone too
     */
    public void setKill(PlayerColor player, boolean overkill){
        deathOrder.add(player);
        isDoubleKillDeathOrder.add(overkill);
        if(deathCount.containsKey(player)) {
            int temp=deathCount.get(player);
            if (overkill)
                deathCount.replace(player,temp ,temp + 2);
            else
                deathCount.replace(player, temp, temp + 1);
            currentKills++;
        }
        else{
            if (overkill)
                deathCount.put(player, 2);
            else
                deathCount.put(player, 1);
            currentKills++;

        }

    }

    public List<Boolean> getIsDoubleKillDeathOrder() {
        return isDoubleKillDeathOrder;
    }

    public void setIsDoubleKillDeathOrder(List<Boolean> isDoubleKillDeathOrder) {
        this.isDoubleKillDeathOrder = isDoubleKillDeathOrder;
    }

    public int getCurrentKills() {
        return currentKills;
    }

    public List<Integer> getKillShotTrackScore() {
        return killShotTrackScore;
    }

    public Map<PlayerColor, Integer> getDeathCount() {
        return deathCount;
    }

    public List<PlayerColor> getDeathOrder() {
        return deathOrder;
    }

    int getNOfKillsForPlayer(PlayerColor p){
        int ret=0;
       for(PlayerColor player:deathOrder)
           if(p.equals(player))
               ret++;
       return ret;
    }

    public int getMaxKills() {
        return maxKills;
    }
}
