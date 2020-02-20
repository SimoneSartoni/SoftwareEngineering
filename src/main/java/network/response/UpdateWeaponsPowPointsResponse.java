package network.response;

import model.powerup.PowerUp;
import model.weapon.Weapon;

import java.rmi.RemoteException;
import java.util.List;

public class UpdateWeaponsPowPointsResponse implements Response {

    private List<Weapon> weapons;
    private List<PowerUp> powerUps;
    private int score;

    public UpdateWeaponsPowPointsResponse(List<Weapon> weapons, List<PowerUp> powerUps, int score) {
        this.weapons = weapons;
        this.powerUps = powerUps;
        this.score = score;
    }

    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);
    }

    public List<Weapon> getWeapons() {
        return weapons;
    }

    public List<PowerUp> getPowerUps() {
        return powerUps;
    }

    public int getScore() {
        return score;
    }
}
