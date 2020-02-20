package model.utility;

import model.board.GameManager;
import model.player.Player;
import model.player.PlayerView;

import java.util.HashMap;
import java.util.Map;

/**
 * pattern OBSERVER used to notify the GameManager about the end of a specific game mode or about points that need to be set
 *
 */
public class EndObserver  {
    private GameManager gameManager;

    public void setGameManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    /**
     * method called to notify gameManager about the end of the normal mode
     * @return true if the game needs to end, false otherwise
     */
    public boolean observeEndNormal(){
        gameManager.manageEndOfNormal();
        return !gameManager.isFinalFrenzy();
    }

    /**
     * method used to notify gameManager that points need to be set
     * @param points the map of points
     * @param doubleKill true if it refers to a doubleKill, false otherwise
     * @param scoredOn the player on which the points are scored. Can be null if the points are for doubleKill
     */
    public void observeOnPoints(Map<Player,Integer> points,boolean doubleKill,Player scoredOn){
        Map<PlayerView, Integer> viewPoints = new HashMap<>();
        for(Player p : points.keySet()) {
            PlayerView pV = p.createPlayerView();
            pV.setTileView(GameManager.createTileView(p.getCurrentTile()));
            viewPoints.put(pV, points.get(p));
        }
        gameManager.notifyOnPoints(viewPoints,doubleKill,scoredOn);
    }

    /**
     * method used to notify the gameManager that Final frenzy has ended
     */
    public void observeEndFinalFrenzy(){
        gameManager.manageEndOfFrenzy();
   }

    /**
     * method used to notify the gameManager that a kill has been made and killShotTrack has been updated
     * @param killer the killer
     * @param overKill true if overkill, false otherwise
     * @param killed the dead player
     */
    public void observeOnKill(Player killer,boolean overKill,Player killed) {
        gameManager.notifyOnKillUpdate(killer,overKill,killed);
    }
}
