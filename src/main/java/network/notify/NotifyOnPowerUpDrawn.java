package network.notify;

import network.response.Response;
import network.response.ResponseHandler;
import model.powerup.PowerUp;

import java.rmi.RemoteException;

public class NotifyOnPowerUpDrawn implements Response {
    private PowerUp powerUp;

    public NotifyOnPowerUpDrawn(PowerUp powerUp){
        this.powerUp = powerUp;

    }
    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);
    }

    public PowerUp getPowerUp() {
        return powerUp;
    }

}
