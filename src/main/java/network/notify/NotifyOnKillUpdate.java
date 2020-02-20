package network.notify;

import network.response.Response;
import network.response.ResponseHandler;
import model.player.PlayerView;

import java.rmi.RemoteException;

public class NotifyOnKillUpdate implements Response {
    private PlayerView killer;
    private boolean overKill;
    private PlayerView killed;


    public NotifyOnKillUpdate(PlayerView killer,boolean overKill,PlayerView killed){
        this.killer=killer;
        this.killed=killed;
        this.overKill=overKill;
    }

    public PlayerView getKiller() {
        return killer;
    }

    public PlayerView getKilled() {
        return killed;
    }

    public boolean isOverKill() {
        return overKill;
    }

    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);
    }
}
