package model.gamemodes;

import model.board.KillShotTrack;
import model.board.PointsBoard;
import model.board.TurnAction;
import model.player.Player;
import model.utility.CurrentTurn;
import model.utility.EndObserver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * abstract class for different gameModes
 * different boards, different actions, but same way to count the points for a player board
 */
public abstract class GameMode implements Serializable {
/**
 * used to communicate with gameManager (pattern OBSERVER)
 */
    private transient EndObserver endObserver;

    public abstract PointsBoard getPointsBoard();

    public abstract TurnAction getBaseTurnAction();
    public abstract TurnAction getAdrenaline1TurnAction();
    public abstract TurnAction getAdrenaline2TurnAction();
    public abstract TurnAction getFrenzyBeforeTurnAction();
    public abstract TurnAction getFrenzyAfterTurnAction();

    public GameMode(EndObserver observer){
        this.endObserver=observer;
    }

    public abstract int changeTurn(KillShotTrack killShotTrack, CurrentTurn currentTurn, List<Player> players,Player currentPlayer);

    /**
     * this sets the board for all the players
     * @param players all the players in the game
     * @param currentPlayer the current player in turn
     */
   public abstract void setBoards(List<Player> players,Player currentPlayer);

   public abstract void setTurnAction(Player player,List<Player> players);
    /**
     * this method transforms the ArrayList of damages in an hashmap with the points gained by each player,
     * giving each player the correspondent points
     *It also notifies the GameManager that some points are scored and they need to be registered and communicated to user
     */
    public void countPlayerPoints(Player scoredOn,List<Player> damageTaken,PointsBoard board,int nOfDeaths){
            class Temp {
                int max(List<Integer> values) {
                    int max = 0;
                    for (int i : values) {
                        if (i > max)
                            max = i;
                    }
                    return max;
                }
            }
            Map<Player, Integer> ret = new HashMap<>();
            Temp t = new Temp();
            int cont = 0;
            for (int i = 0; i < damageTaken.size(); i++) {
                if (ret.containsKey(damageTaken.get(i))) {
                    ret.replace(damageTaken.get(i), ret.get(damageTaken.get(i)), ret.get(damageTaken.get(i)) + 1);
                } else {
                    ret.put(damageTaken.get(i), 1);
                }
            }
            List<Integer> values = new ArrayList<>(ret.values());
            List<Player> tiebreaker = new ArrayList<>();
            Map<Player, Integer> scoredPoints = new HashMap<>();
            Player maxPointsPlayer = new Player();
            while (!ret.isEmpty()) {
                for (Player p : ret.keySet()) {
                    if ((ret.get(p) == t.max(values))) {
                        maxPointsPlayer = p;
                        tiebreaker.add(maxPointsPlayer);
                        break;
                    }
                }
                if (ret.get(maxPointsPlayer) > 0) {
                    for (Player p2 : ret.keySet()) {
                        if ((!p2.equals(maxPointsPlayer)) && (ret.get(p2) == t.max(values))) {
                            tiebreaker.add(p2);
                        }
                    }
                    int i = 0;
                    while (!tiebreaker.isEmpty()) {
                        while (i < damageTaken.size()) {
                            if (tiebreaker.contains(damageTaken.get(i))) {
                                if (cont + nOfDeaths < board.getPoints().size()) {
                                    scoredPoints.put(tiebreaker.get(tiebreaker.indexOf(damageTaken.get(i))), board.getPoints().get(cont + nOfDeaths));
                                } else {
                                    scoredPoints.put(tiebreaker.get(tiebreaker.indexOf(damageTaken.get(i))), 1);
                                }
                                cont++;
                                tiebreaker.remove(tiebreaker.get(tiebreaker.indexOf(damageTaken.get(i))));
                                ret.remove(damageTaken.get(i));
                                break;
                            }
                            i++;
                        }
                    }
                    Integer max = t.max(values);
                    while (values.contains(max))
                        values.remove(max);
                } else {
                    ret.remove(maxPointsPlayer);
                    tiebreaker.remove(maxPointsPlayer);
                }
            }
            if(!damageTaken.isEmpty()) {
                scoredPoints.replace(damageTaken.get(0), scoredPoints.get(damageTaken.get(0)), scoredPoints.get(damageTaken.get(0)) + board.getPointsFirstBlood());
                for (Player player : scoredPoints.keySet()) {
                    player.addScore(scoredPoints.get(player));
                }
                switch (damageTaken.size()) {
                    case 12:
                        endObserver.observeOnPoints(scoredPoints, false, scoredOn);
                        break;
                    case 11:
                        endObserver.observeOnPoints(scoredPoints,false,scoredOn);
                        break;
                    default:
                        endObserver.observeOnPoints(scoredPoints, false, scoredOn);
                        break;
                    }
                }
            }

    public EndObserver getEndObserver() {
        return endObserver;
    }

}


