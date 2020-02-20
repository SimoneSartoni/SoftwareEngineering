package network.notify;

import network.response.Response;
import network.response.ResponseHandler;
import model.board.TileView;
import model.player.PlayerView;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class NotifyOnEndTurn implements Response {

    private PlayerView endOfTurnPlayer;
    private PlayerView newTurnPlayer;
    private List<TileView> tileList;

    public NotifyOnEndTurn(PlayerView endOfTurnPlayer,PlayerView newTurnPlayer,List<TileView> tiles){
        this.endOfTurnPlayer=endOfTurnPlayer;
        this.newTurnPlayer=newTurnPlayer;
        this.tileList=new ArrayList<>(tiles);
    }
    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);
    }

    public PlayerView getEndOfTurnPlayer() {
        return endOfTurnPlayer;
    }

    public PlayerView getNewTurnPlayer() {
        return newTurnPlayer;
    }

    public List<TileView> getTileList() {
        return tileList;
    }
}
