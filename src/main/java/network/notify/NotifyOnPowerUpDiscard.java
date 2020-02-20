package network.notify;

import network.response.Response;
import network.response.ResponseHandler;
import model.player.PlayerView;
import model.powerup.PowerUp;

import java.rmi.RemoteException;

public class NotifyOnPowerUpDiscard implements Response {

    private PowerUp powerUp;
    private PlayerView player;

    public NotifyOnPowerUpDiscard(PowerUp powerUp,PlayerView player){
        this.player=player;
        this.powerUp=powerUp;
    }

    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);
    }

    public PowerUp getPowerUp() {
        return powerUp;
    }

    public PlayerView getPlayer() {
        return player;
    }
}
