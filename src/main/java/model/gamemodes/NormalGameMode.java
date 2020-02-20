package model.gamemodes;

import model.board.KillShotTrack;
import model.board.PointsBoard;
import model.board.Tile;
import model.board.TurnAction;
import model.enums.PlayerState;
import model.enums.TurnState;
import model.player.Player;
import model.utility.CurrentTurn;
import model.utility.EndObserver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NormalGameMode extends GameMode {
    /**
     * points and action related to the normal board (adrenaline phase 1 and 2 too)
     */
    private TurnAction baseTurnAction;
    private TurnAction adrenaline1TurnAction;
    private TurnAction adrenaline2TurnAction;
    private PointsBoard normalBoard;
    private boolean swapToNewMode;

    public NormalGameMode (BoardStructure b) {
        //parse from file
        super(new EndObserver());
        swapToNewMode=false;
        baseTurnAction = new TurnAction(b.getBaseActions(), b.getBaseMovShoot(), b.isBaseReloadShoot(), b.getBaseOnlyMov(), b.getBaseGrabMov());
        adrenaline1TurnAction = new TurnAction(b.getAdrenaline1Actions(), b.getAdrenaline1MovShoot(), b.isAdrenaline1ReloadShoot(), b.getAdrenaline1OnlyMov(), b.getAdrenaline1GrabMov());
        adrenaline2TurnAction = new TurnAction(b.getAdrenaline2Actions(), b.getAdrenaline2MovShoot(), b.isAdrenaline2ReloadShoot(), b.getAdrenaline2OnlyMov(), b.getAdrenaline2GrabMov());
        normalBoard = new PointsBoard(b.getNormalScores(), b.getNormalFirstBlood());
    }

    @Override
    public PointsBoard getPointsBoard() {
        return normalBoard;
    }

    @Override
    public TurnAction getBaseTurnAction() {
        return baseTurnAction;
    }

    @Override
    public TurnAction getAdrenaline1TurnAction() {
        return adrenaline1TurnAction;
    }

    @Override
    public TurnAction getAdrenaline2TurnAction() {
        return adrenaline2TurnAction;
    }

    @Override
    public TurnAction getFrenzyBeforeTurnAction() {
        return null;
    }

    @Override
    public TurnAction getFrenzyAfterTurnAction() {
        return null;
    }

    /**
     * method used to change turn in normal mode.
     * It calculates points for dead players and sets their deaths. Also this players need to respawn
     * the killshottrack is updated too
     * @param killShotTrack the killShotTrack to update
     * @param currentTurn the current turn of the game
     * @param players the list of all players in the game
     * @param currentPlayer the current player
     * @return 1 if the game has ended, 0 otherwise
     */
    @Override
    public int changeTurn(KillShotTrack killShotTrack, CurrentTurn currentTurn, List<Player> players,Player currentPlayer) {
        for(Player p : currentTurn.getDeadPlayers()) {
            p.setTurnState(TurnState.READY_TO_RESPAWN);
            Tile tile=p.getCurrentTile();
            tile.removePlayer(p);
            p.setCurrentTile(null);
            countPlayerPoints(p, p.getDamageTaken(), p.getBoard(), p.getNOfDeaths());
            p.addNOfDeaths();
            p.getDamageTaken().get(10).addNOfKills();
            if (p.getDamageTaken().size()>11) {
                killShotTrack.setKill(p.getDamageTaken().get(11).getPlayerColor(), true);
                getEndObserver().observeOnKill(p.getDamageTaken().get(11), true, p);
            } else {
                killShotTrack.setKill(p.getDamageTaken().get(10).getPlayerColor(), false);
                getEndObserver().observeOnKill(p.getDamageTaken().get(10), false, p);
            }
            p.resetDamageTaken();
            if(killShotTrack.getDeathOrder().size()>=killShotTrack.getMaxKills()){
                if(!swapToNewMode) {
                    if (getEndObserver().observeEndNormal())
                        return 1;
                    else
                        swapToNewMode = true;
                }

        }
            if (currentTurn.getDeadPlayers().size() >= 2) {
                Map<Player, Integer> point = new HashMap<>();
                currentPlayer.addScore(killShotTrack.getPointsForDoubleKill());
                point.put(currentPlayer, killShotTrack.getPointsForDoubleKill());
                getEndObserver().observeOnPoints(point, true, null);
            }
    }
        return 0;
    }

    @Override
    public void setBoards(List<Player> players,Player currentPlayer) {
        return;
    }

    @Override
    public void setTurnAction(Player player,List<Player> players) {
        if(player.getDamageTaken().size()<3){
            player.setPlayerState(PlayerState.NORMAL);
            player.setCurrentTurnAction(baseTurnAction);}
        if((player.getDamageTaken().size()>=3)&&(player.getDamageTaken().size()<6)){
            player.setPlayerState(PlayerState.ADRENALINE_1);
            player.setCurrentTurnAction(adrenaline1TurnAction);
        }
        if((player.getDamageTaken().size()>=6)&&(player.getDamageTaken().size()<11)) {
            player.setPlayerState(PlayerState.ADRENALINE_2);
            player.setCurrentTurnAction(adrenaline2TurnAction);
        }
    }
}

