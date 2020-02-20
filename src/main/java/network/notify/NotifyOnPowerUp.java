package network.notify;

import network.response.Response;
import network.response.ResponseHandler;
import model.powerup.PowerUp;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class NotifyOnPowerUp implements Response {

    private List<PowerUp> powerUpList;

    public NotifyOnPowerUp(List<PowerUp> powerUps){
        powerUpList=new ArrayList<>(powerUps);

    }
    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);
    }

    public List<PowerUp> getPowerUpList() {
        return powerUpList;
    }
}
