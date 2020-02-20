package network.requests;

import model.powerup.PowerUp;
import network.ClientHandler;
import network.ServerController;

public class ChoosePowerUpRequest extends Request {

    private final PowerUp powerUp;

    public ChoosePowerUpRequest(String token, PowerUp powerUp) {
        super(token);
        this.powerUp = powerUp;
    }

    @Override
    public void handleRequest(ClientHandler clientHandler, ServerController remoteController) {
        remoteController.choosePowerup(getToken(), powerUp);
    }
}
