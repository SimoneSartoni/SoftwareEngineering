package network.requests;

import model.enums.AmmoColor;
import network.ClientHandler;
import network.ServerController;

public class ChooseAmmoRequest extends Request{

    private final AmmoColor ammocolor;

    public ChooseAmmoRequest(String token, AmmoColor color) {
        super(token);
        this.ammocolor = color;
    }

    @Override
    public void handleRequest(ClientHandler clientHandler, ServerController remoteController) {
        remoteController.chooseAmmo(getToken(), ammocolor);
    }
}
