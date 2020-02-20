package network.notify;

import network.response.Response;
import network.response.ResponseHandler;
import model.player.PlayerView;
import model.powerup.PowerUp;

import java.rmi.RemoteException;

public class NotifyOnPowerUpUsed implements Response {
    private final PlayerView playerView;
    private final PowerUp powerUp;

    public NotifyOnPowerUpUsed(PlayerView playerView,PowerUp powerUp){
        this.playerView=playerView;
        this.powerUp=powerUp;
    }

    public PlayerView getPlayerView() {
        return playerView;
    }

    public PowerUp getPowerUp() {
        return powerUp;
    }

    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);
    }
}
