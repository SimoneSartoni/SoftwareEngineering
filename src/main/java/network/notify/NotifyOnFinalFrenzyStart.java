package network.notify;

import network.response.Response;
import network.response.ResponseHandler;
import model.player.PlayerView;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class NotifyOnFinalFrenzyStart implements Response {

    List<PlayerView> players;

    public NotifyOnFinalFrenzyStart(List<PlayerView> playerViews){
        this.players=new ArrayList<>(playerViews);
    }

    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
           handler.handle(this);
    }

    public List<PlayerView> getPlayers() {
        return players;
    }
}
