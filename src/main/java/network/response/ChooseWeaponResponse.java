package network.response;

import model.weapon.Weapon;

import java.rmi.RemoteException;
import java.util.List;

public class ChooseWeaponResponse implements Response {
    private final List<Weapon> weaponList;

    public ChooseWeaponResponse(List<Weapon> weaponList) {
        this.weaponList = weaponList;
    }

    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);
    }

    public List<Weapon> getWeaponList() {
        return weaponList;
    }
}
