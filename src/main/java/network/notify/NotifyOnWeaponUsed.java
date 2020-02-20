package network.notify;

import network.response.Response;
import network.response.ResponseHandler;
import model.player.PlayerView;
import model.weapon.Weapon;

import java.rmi.RemoteException;

public class NotifyOnWeaponUsed  implements Response {
    private final Weapon weapon;
    private final PlayerView playerView;

    public NotifyOnWeaponUsed(PlayerView playerView,Weapon weapon){
        this.weapon=weapon;
        this.playerView=playerView;
    }

    public Weapon getWeapon() {
        return weapon;
    }

    public PlayerView getPlayerView() {
        return playerView;
    }

    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);
    }
}
