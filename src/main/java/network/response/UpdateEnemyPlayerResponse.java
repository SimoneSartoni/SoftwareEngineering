package network.response;

import model.player.PlayerView;

import java.rmi.RemoteException;

public class UpdateEnemyPlayerResponse implements Response {
    private final PlayerView enemyPlayer;

    public UpdateEnemyPlayerResponse(PlayerView enemyPlayer) {
        this.enemyPlayer = enemyPlayer;
    }

    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);
    }

    public PlayerView getEnemyPlayer() {
        return enemyPlayer;
    }
}
