package network.response;

import model.utility.MapInfoView;

import java.rmi.RemoteException;
import java.util.List;

public class GamesStatusResponse implements Response {

    private final boolean validJoin;
    private final List<MapInfoView> toPrint;
    private final int mapSize;

    public GamesStatusResponse(boolean validJoin, List<MapInfoView> toPrint, int mapSize) {
        this.validJoin = validJoin;
        this.toPrint = toPrint;
        this.mapSize = mapSize;
    }

    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);
    }

    public boolean isValidJoin() {
        return validJoin;
    }

    public List<MapInfoView> getToPrint() {
        return toPrint;
    }

    public int getMapSize() {
        return mapSize;
    }
}
