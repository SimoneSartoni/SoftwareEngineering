package network.notify;

import network.response.Response;
import network.response.ResponseHandler;
import model.player.PlayerView;
import model.weapon.Weapon;

import java.rmi.RemoteException;

public class NotifyOnWeaponReload implements Response {

    private Weapon weapon;
    private PlayerView player;

    public NotifyOnWeaponReload(Weapon weapon, PlayerView player){
        this.weapon=weapon;
        this.player=player;
    }
    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);
    }

    public PlayerView getPlayer() {
        return player;
    }

    public Weapon getWeapon() {
        return weapon;
    }
}

