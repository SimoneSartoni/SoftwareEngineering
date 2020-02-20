package network.notify;

import network.response.Response;
import network.response.ResponseHandler;
import model.player.PlayerView;

import java.rmi.RemoteException;

public class NotifyOnMarks implements Response {

    private int oldMarks;
    private int newMarks;
    private PlayerView marker;
    private PlayerView marked;

    public NotifyOnMarks(int oldMarks, int newMarks, PlayerView marker, PlayerView marked){
        this.oldMarks=oldMarks;
        this.newMarks=newMarks;
        this.marker=marker;
        this.marked=marked;
    }

    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);

    }

    public int getNewMarks() {
        return newMarks;
    }

    public int getOldMarks() {
        return oldMarks;
    }

    public PlayerView getMarked() {
        return marked;
    }

    public PlayerView getMarker() {
        return marker;
    }
}
