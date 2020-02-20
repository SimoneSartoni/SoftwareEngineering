package network.response;

import model.board.KillShotTrack;
import model.utility.MapInfoView;

import java.rmi.RemoteException;

public class MapInfoResponse implements Response {

    private final MapInfoView mapInfo;
    private final KillShotTrack killShotTrack;

    public MapInfoResponse(MapInfoView mapInfo,KillShotTrack killShotTrack) {
        this.mapInfo = mapInfo;
        this.killShotTrack=killShotTrack;
    }

    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);
    }

    public KillShotTrack getKillShotTrack() {
        return killShotTrack;
    }

    public MapInfoView getMapInfo() {
        return mapInfo;
    }
}
