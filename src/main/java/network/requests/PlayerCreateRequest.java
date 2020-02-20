package network.requests;

import model.enums.PlayerColor;
import network.ClientHandler;
import network.ServerController;

import java.rmi.RemoteException;

public class PlayerCreateRequest extends Request {

    public final int gameId;
    public final String username;
    public final PlayerColor color;

    public PlayerCreateRequest(String token, int gameId, String username, PlayerColor color) {
        super(token);
        this.gameId = gameId;
        this.username = username;
        this.color = color;
    }

    @Override
    public void handleRequest(ClientHandler clientHandler, ServerController remoteController) {
        try {
            remoteController.createPlayer(getToken(), gameId, username, color);
        } catch (RemoteException e) {
            System.err.println(e.getMessage());
        }
    }

}

