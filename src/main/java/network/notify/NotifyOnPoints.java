package network.notify;

import network.response.Response;
import network.response.ResponseHandler;
import model.player.PlayerView;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

public class NotifyOnPoints implements Response {

    private Map<PlayerView,Integer> points;
    private  boolean doubleKill;
    private PlayerView scoredOn;

    public NotifyOnPoints(Map<PlayerView,Integer> points,boolean doubleKill,PlayerView scoredOn){
        this.points=new HashMap<>(points);
        this.doubleKill=doubleKill;
        this.scoredOn=scoredOn;
    }
    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);
    }

    public Map<PlayerView, Integer> getPoints() {
        return points;
    }

    public PlayerView getScoredOn() {
        return scoredOn;
    }

    public boolean isDoubleKill() {
        return doubleKill;
    }
}
