package network.requests;

import model.player.PlayerView;
import network.ClientHandler;
import network.ServerController;

public class ChooseTargetRequest extends Request {

    private final PlayerView player;

    public ChooseTargetRequest(String token,PlayerView player) {
        super(token);
        this.player=player;
    }

    @Override
    public void handleRequest(ClientHandler clientHandler, ServerController remoteController) {
        remoteController.chooseTarget(getToken(),player);
    }
}
