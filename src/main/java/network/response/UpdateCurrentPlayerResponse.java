package network.response;

import model.player.PlayerView;

import java.rmi.RemoteException;

public class UpdateCurrentPlayerResponse implements Response {
    private final PlayerView player;
    public UpdateCurrentPlayerResponse(PlayerView player) {
        this.player = player;
    }

    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);
    }

    public PlayerView getPlayer() {
        return player;
    }
}
