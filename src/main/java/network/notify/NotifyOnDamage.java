package network.notify;

import network.response.Response;
import network.response.ResponseHandler;
import model.player.PlayerView;

import java.rmi.RemoteException;

public class NotifyOnDamage implements Response {

    private int dmg;
    private int markDown;
    private PlayerView player;
    private PlayerView damaged;

    public NotifyOnDamage(int dmg,int markDown,PlayerView player,PlayerView damaged){
        this.dmg=dmg;
        this.markDown=markDown;
        this.player=player;
        this.damaged=damaged;
    }

    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);

    }

    public int getDmg() {
        return dmg;
    }

    public int getMarkDown() {
        return markDown;
    }

    public PlayerView getDamaged() {
        return damaged;
    }

    public PlayerView getPlayer() {
        return player;
    }
}
