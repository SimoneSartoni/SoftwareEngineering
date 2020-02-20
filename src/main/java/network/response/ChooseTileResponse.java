package network.response;

import model.board.TileView;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class ChooseTileResponse implements Response {

    private final List<TileView> tileList;

    public ChooseTileResponse(List<TileView> tileList) {
        this.tileList =new ArrayList(tileList);
    }

    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);
    }

    public List<TileView> getTileList() {
        return tileList;
    }
}
