package network;

import controller.TurnStateController;
import model.board.GameManager;
import model.player.Player;
import model.utility.MapInfo;

import java.io.Serializable;
import java.util.List;
import java.util.TimerTask;

public class NetworkCountdown extends TimerTask implements Serializable {

    private final NetworkManager networkManager;
    private final GameManager gameManager;
    private final int gameId;
    private final TurnStateController turnStateController;

    NetworkCountdown(TurnStateController turnStateController, int gameId, GameManager gameManager) {
        this.networkManager = NetworkManager.get(false);
        this.gameId = gameId;
        this.gameManager = gameManager;
        this.turnStateController = turnStateController;
    }

    /***
     * Timer to start the game. It invoke the start game of the game manager related, and set all the initial parameters of the plyers,
     * notyfing all the views. It also call the spawn phase of the first player.
     */
    @Override
    public void run() {
        gameManager.startGame();
        List<ViewProxy> viewListenersToNotify = networkManager.viewListenersById.get(gameId);
        String currentToken = networkManager.getTokenFromPlayer(gameManager.getCurrentPlayerTurn());
        ViewProxy currentView = networkManager.getTokenProxy(currentToken);
        networkManager.setGameManagerListener(gameId);
        gameManager.setStarted(true);
        networkManager.notifyAllRelatedViews(gameId);
        for (String s : networkManager.printStarting(gameId)) {
            System.out.println(s);
        }
        for(Player p : gameManager.getPlayerOrderTurn()){
            if(p!= null && !p.isDisconnected()) {
                p.getViewPlayer().onPrintHelp(gameManager,p);
            }
        }
        for (ViewProxy v : viewListenersToNotify) {
            v.onGameStarted(true);
            v.onMapInfo(MapInfo.createMapView(networkManager.mapChosenById.get(gameId)),gameManager.getKillShotTrack());
            if (v.equals(currentView)) {
                v.onActiveTurn(true);
            } else {
                v.onActiveTurn(false);
            }
        }
        gameManager.spawnDrawPhase(gameManager.getCurrentPlayerTurn(),2);
        networkManager.startPlayerTimer(gameId);
        networkManager.stopTimer(gameId);
    }
}
