package network.response;

import model.utility.LobbyInfo;

import java.rmi.RemoteException;
import java.util.List;

public class LobbyStatusResponse implements Response {
    private final List<LobbyInfo> lobbyInfo;

    public LobbyStatusResponse(List<LobbyInfo> lobbyInfo) {
        this.lobbyInfo = lobbyInfo;
    }

    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);
    }

    public List<LobbyInfo> getLobbyInfo() {
        return lobbyInfo;
    }
}
