package network.notify;

import network.response.Response;
import network.response.ResponseHandler;
import model.board.AmmoTile;
import model.player.PlayerView;

import java.rmi.RemoteException;

public class NotifyOnAmmoTileGrab implements Response {

    private PlayerView player;
    private AmmoTile ammoTile;

    public NotifyOnAmmoTileGrab(PlayerView player,AmmoTile ammoTile){
        this.ammoTile=ammoTile;
        this.player=player;
    }

    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);
    }

    public PlayerView getPlayer() {
        return player;
    }

    public AmmoTile getAmmoTile() {
        return ammoTile;
    }
}
