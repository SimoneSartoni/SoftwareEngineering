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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FinalFrenzyGameMode extends GameMode {
    /**
     * boards and points in final frenzy game mode
     */
    private TurnAction frenzyBeforeTurnAction;
    private TurnAction frenzyAfterTurnAction;
    private PointsBoard frenzyBoard;
    private List<Player> frenzyTurn;
    /**
     * parameter to see when final frenzy has to end
     * Final frenzy ends when this attribute is equal to the nOfPlayers
     */
    private int nFrenzyTurn;

    public FinalFrenzyGameMode(BoardStructure b) {
        //parse from file
        super(new EndObserver());
        frenzyAfterTurnAction = new TurnAction(b.getFrenzyAfterActions(), b.getFrenzyAfterMovShoot(), b.isFrenzyAfterReloadShoot(), b.getFrenzyAfterOnlyMov(), b.getFrenzyAfterGrabMov());
        frenzyBeforeTurnAction = new TurnAction(b.getFrenzyBeforeActions(), b.getFrenzyBeforeMovShoot(), b.isFrenzyBeforeReloadShoot(), b.getFrenzyBeforeOnlyMov(), b.getFrenzyBeforeGrabMov());
        nFrenzyTurn=0;
        frenzyTurn=new ArrayList<>();
        frenzyBoard = new PointsBoard(b.getFrenzyScores(), b.getFrenzyFirstBlood());
    }

    @Override
    public PointsBoard getPointsBoard() {
        return frenzyBoard;
    }

    @Override
    public TurnAction getBaseTurnAction() {
        return null;
    }

    @Override
    public TurnAction getAdrenaline1TurnAction() {
        return null;
    }

    @Override
    public TurnAction getAdrenaline2TurnAction() {
        return null;
    }

    @Override
    public TurnAction getFrenzyBeforeTurnAction() {
        return frenzyBeforeTurnAction;
    }

    @Override
    public TurnAction getFrenzyAfterTurnAction() {
        return frenzyAfterTurnAction;
    }

    /**
     * method used when turn changes in final frenzy
     * It calculates points for dead players and set their new frenzy board for points
     * it updates also the killshottrack, but not giving any skull to any player
     * @param killShotTrack the killshottrack to update
     * @param currentTurn the currentTurn of the game
     * @param players the list of all the players in the game
     * @param currentPlayer the current player in the turn
     * @return 1 if final frenzy is over, 0 otherwise
     */
    @Override
    public int changeTurn(KillShotTrack killShotTrack, CurrentTurn currentTurn, List<Player> players,Player currentPlayer) {
        for(Player p:currentTurn.getDeadPlayers()){
            p.setTurnState(TurnState.READY_TO_RESPAWN);
            Tile tile=p.getCurrentTile();
            tile.removePlayer(p);
            p.setCurrentTile(null);
            countPlayerPoints(p,p.getDamageTaken(),p.getBoard(),p.getNOfDeaths());
            setBoardForPlayer(p);
            if(p.getDamageTaken().size()>11) {
                killShotTrack.setKill(p.getDamageTaken().get(11).getPlayerColor(), true);
                getEndObserver().observeOnKill(p.getDamageTaken().get(11),true,p);
            }
            else {
                killShotTrack.setKill(p.getDamageTaken().get(10).getPlayerColor(), false);
                getEndObserver().observeOnKill(p.getDamageTaken().get(10),false,p);
            }
            p.resetDamageTaken();
        }
        if(currentTurn.getDeadPlayers().size()>=2) {
            Map<Player,Integer> point=new HashMap<>();
            currentPlayer.addScore(killShotTrack.getPointsForDoubleKill());
            point.put(currentPlayer,killShotTrack.getPointsForDoubleKill());
            getEndObserver().observeOnPoints(point,true,null);
        }
        nFrenzyTurn++;
        if(nFrenzyTurn>=players.size()){
            getEndObserver().observeEndFinalFrenzy();
            return 1;}
        return 0;
    }

    private void setBoardForPlayer(Player p){
        p.setBoard(frenzyBoard);
    }

    @Override
    public void setBoards(List<Player> playerOrderTurn,Player currentPlayer) {
        int cont=0;
        int i=playerOrderTurn.indexOf(currentPlayer)+1;
        boolean afterStartingPlayer=false;
        while(cont<playerOrderTurn.size()) {
            if (i == playerOrderTurn.size()) {
                i = 0;
                afterStartingPlayer = true;
            }
            if (afterStartingPlayer) {
                    playerOrderTurn.get(i).setCurrentTurnAction(frenzyAfterTurnAction);
                        playerOrderTurn.get(i).setPlayerState(PlayerState.FRENZY_AFTER);

                } else {
                if (cont == 0 && i == 0) {
                    playerOrderTurn.get(i).setCurrentTurnAction(frenzyAfterTurnAction);
                    playerOrderTurn.get(i).setPlayerState(PlayerState.FRENZY_AFTER);

                }
                else {
                    playerOrderTurn.get(i).setCurrentTurnAction(frenzyBeforeTurnAction);
                    playerOrderTurn.get(i).setPlayerState(PlayerState.FRENZY_BEFORE);
                }
                }
            frenzyTurn.add(playerOrderTurn.get(i));
                cont++;
                i++;
        }
        for (Player p : playerOrderTurn) {
            if ((p.getPlayerState() == PlayerState.DEAD)||(p.getDamageTaken().isEmpty())) {
                p.setBoard(frenzyBoard);
                p.setDeaths(0);
            }
        }
    }

    @Override
    public void setTurnAction(Player player,List<Player> playerOrderTurn) {
        int i=frenzyTurn.indexOf(player);
        if(player.getDamageTaken().size()<11) {
            if (i >= frenzyTurn.indexOf(playerOrderTurn.get(0)))
                player.setPlayerState(PlayerState.FRENZY_AFTER);
            else
                player.setPlayerState(PlayerState.FRENZY_BEFORE);
        }

    }

    public int getnFrenzyTurn() {
        return nFrenzyTurn;
    }
}
