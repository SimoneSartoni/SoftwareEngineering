package network.response;

import model.enums.AmmoColor;

import java.rmi.RemoteException;
import java.util.List;

public class ChooseAmmoResponse implements Response {
    private final List<AmmoColor> ammoColorList;

    public ChooseAmmoResponse(List <AmmoColor> ammoColorList) {
        this.ammoColorList = ammoColorList;
    }

    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);
    }

    public List<AmmoColor> getAmmoColorList() {
        return ammoColorList;
    }
}
