package network.requests;

import model.enums.TypeOfEffect;
import network.ClientHandler;
import network.ServerController;

public class ChooseTypeOfEffectRequest extends Request {

    private final TypeOfEffect typeOfEffect;

    public ChooseTypeOfEffectRequest(String token,TypeOfEffect typeOfEffect) {
        super(token);
        this.typeOfEffect=typeOfEffect;
    }

    @Override
    public void handleRequest(ClientHandler clientHandler, ServerController remoteController) {
        remoteController.chooseTypeOfEffect(getToken(),typeOfEffect);
    }
}
