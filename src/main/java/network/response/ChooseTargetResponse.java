package network.response;

import model.player.PlayerView;

import java.rmi.RemoteException;
import java.util.List;

public class ChooseTargetResponse implements Response {
    private final List<PlayerView> playerViewList;

    public ChooseTargetResponse(List<PlayerView> playerViewList) {
        this.playerViewList = playerViewList;
    }

    @Override
    public void handleResponse(ResponseHandler handler)throws RemoteException {
        handler.handle(this);
    }

    public List<PlayerView> getPlayerViewList() {
        return playerViewList;
    }
}
