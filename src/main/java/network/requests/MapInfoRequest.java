package network.requests;

import network.ClientHandler;
import network.ServerController;

public class MapInfoRequest extends Request {

    private int map;
    private int gameId;

    public MapInfoRequest(String token, int gameId, int map) {
        super(token);
        this.map = map;
        this.gameId = gameId;
    }

    @Override
    public void handleRequest(ClientHandler clientHandler, ServerController remoteController) {
        remoteController.getMapInfo(getToken(), gameId, map);
    }
}
