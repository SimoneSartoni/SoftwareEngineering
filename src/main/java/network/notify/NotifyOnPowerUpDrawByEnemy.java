package network.notify;

import network.response.Response;
import network.response.ResponseHandler;
import model.player.PlayerView;

import java.rmi.RemoteException;

public class NotifyOnPowerUpDrawByEnemy implements Response {

    private PlayerView player;

    public NotifyOnPowerUpDrawByEnemy(PlayerView player){
        this.player=player;
    }
    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);
    }

    public PlayerView getPlayer(){
        return player;
    }
}


