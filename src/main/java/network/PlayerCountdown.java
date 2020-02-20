package network;

import model.board.GameManager;
import model.enums.TurnState;
import model.player.Player;
import model.player.PlayerView;

import java.io.Serializable;
import java.util.TimerTask;

public class PlayerCountdown extends TimerTask implements Serializable {
    private final NetworkManager networkManager;
    private final GameManager gameManager;
    private final int gameId;

    PlayerCountdown(GameManager gameManager, int gameId) {
        this.networkManager = NetworkManager.get(false);
        this.gameManager = gameManager;
        this.gameId = gameId;
    }

    /**
     * This is the timer for the active player. When ends the active player, that can be who is playing or who in counter attacking,
     * is disconnected for inactivity and the turn is changed. If the remaining player number is less than the minimum number of players
     * the game is quit
     */
    @Override
    public void run() {
        Player player = gameManager.getCurrentPlayerTurn();

        for(Player p : gameManager.getPlayerOrderTurn()) {
            if(p.getTurnState() == TurnState.CHOOSE_COUNTER_ATTACK) {
                player = p;
                break;
            }
        }

        if(player != null) {
            PlayerView pV = player.createPlayerView();
            pV.setTileView(GameManager.createTileView(player.getCurrentTile()));
            networkManager.viewsProxy.get(player.getToken()).onUpdateCurrentPlayer(pV, false);
            System.out.println("Disconnecting " + player.getPlayerID());
            networkManager.disconnectToken(player.getToken());
            if(networkManager.disconnectedProxyListener.get(player.getToken()) != null) {
                networkManager.disconnectedProxyListener.get(player.getToken()).onInactivity(true);
            }
        } else {
            System.out.println("SOMETHING WRONG WITH CURRENT PLAYER TIMER, BECAUSE IT HAS EXPIRED BUT THE CURREN TPLAYER IS NULL");
        }
        if(gameManager.isGameEnd()) {
            System.out.println("Game " + gameId + " is end cause lack of player");
            networkManager.stopPlayerTimer(gameId);
        }
    }
}
