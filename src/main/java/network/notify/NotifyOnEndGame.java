package network.notify;

import network.response.Response;
import network.response.ResponseHandler;
import model.player.PlayerView;

import java.rmi.RemoteException;
import java.util.List;

public class NotifyOnEndGame implements Response {

    List<PlayerView> playerViewList;
    List<Integer> points;

    public NotifyOnEndGame(List<PlayerView> playerViewList,List<Integer> points){
        this.playerViewList=playerViewList;
        this.points=points;
    }

    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);
    }

    public List<PlayerView> getPlayerViewList() {
        return playerViewList;
    }

    public List<Integer> getPoints() {
        return points;
    }
}
