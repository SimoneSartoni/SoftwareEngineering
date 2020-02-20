package network.notify;

import network.response.Response;
import network.response.ResponseHandler;
import model.board.TileView;
import model.player.PlayerView;

import java.rmi.RemoteException;

public class NotifyOnMovement implements Response {

    PlayerView movedPlayer;
    TileView tile;

    public NotifyOnMovement(PlayerView movedPlayer,TileView tile){
        this.movedPlayer=movedPlayer;
        this.tile=tile;
    }
    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);
    }

    public PlayerView getMovedPlayer() {
        return movedPlayer;
    }

    public TileView getTile() {
        return tile;
    }
}
