package network.requests;

import model.weapon.Effect;
import network.ClientHandler;
import network.ServerController;

public class ChooseEffectRequest extends Request {
    private final Effect effect;

    public ChooseEffectRequest(String token, Effect effect) {
        super(token);
        this.effect = effect;
    }

    @Override
    public void handleRequest(ClientHandler clientHandler, ServerController remoteController) {
        remoteController.chooseEffect(getToken(), effect);
    }
}
