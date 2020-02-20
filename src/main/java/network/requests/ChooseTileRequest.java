package network.requests;

import model.board.TileView;
import network.ClientHandler;
import network.ServerController;

public class ChooseTileRequest extends Request {

    private final TileView tile;

    public ChooseTileRequest(String token,TileView tile) {
        super(token);
        this.tile=tile;
    }

    @Override
    public void handleRequest(ClientHandler clientHandler, ServerController remoteController) {
        remoteController.chooseTile(getToken(),tile);
    }
}
