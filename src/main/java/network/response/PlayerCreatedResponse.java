package network.response;

import model.player.PlayerView;

import java.rmi.RemoteException;

public class PlayerCreatedResponse implements Response{
    private final PlayerView player;

    public PlayerCreatedResponse(PlayerView player) {
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
