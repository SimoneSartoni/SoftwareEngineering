package network.notify;

import network.response.Response;
import network.response.ResponseHandler;
import model.board.TileView;
import model.player.PlayerView;

import java.rmi.RemoteException;

public class NotifyOnSpawn implements Response {

    private PlayerView spawnPlayer;
    private TileView spawnTile;

    public NotifyOnSpawn(PlayerView spawnPlayer,TileView spawnTile){
        this.spawnPlayer=spawnPlayer;
        this.spawnTile=spawnTile;
    }

    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);
    }

    public PlayerView getSpawnPlayer() {
        return spawnPlayer;
    }

    public TileView getSpawnTile() {
        return spawnTile;
    }
}
