package network.notify;

import network.response.Response;
import network.response.ResponseHandler;
import model.player.PlayerView;
import model.weapon.Weapon;

import java.rmi.RemoteException;

public class NotifyOnWeaponGrab implements Response {

    private Weapon weaponGrabbed;
    private Weapon weaponDropped;
    private PlayerView player;

    public NotifyOnWeaponGrab(Weapon weaponGrabbed, PlayerView player,Weapon weaponDropped){
        this.weaponGrabbed=weaponGrabbed;
        this.weaponDropped=weaponDropped;
        this.player=player;
    }

    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);
    }

    public PlayerView getPlayer() {
        return player;
    }

    public Weapon getWeaponDropped() {
        return weaponDropped;
    }

    public Weapon getWeaponGrabbed() {
        return weaponGrabbed;
    }
}
