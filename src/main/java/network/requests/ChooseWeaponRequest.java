package network.requests;

import model.weapon.Weapon;
import network.ClientHandler;
import network.ServerController;

public class ChooseWeaponRequest extends Request {

    private final Weapon weapon;

    public ChooseWeaponRequest(String token, Weapon weapon) {
        super(token);
        this.weapon = weapon;
    }

    @Override
    public void handleRequest(ClientHandler clientHandler, ServerController remoteController) {
        remoteController.chooseWeapon(getToken(), weapon);
    }
}
