package network.response;

import model.weapon.Effect;

import java.rmi.RemoteException;
import java.util.List;

public class ChooseEffectResponse implements Response {
    private final List<Effect> effectList;

    public ChooseEffectResponse(List<Effect> effectList) {
        this.effectList = effectList;
    }

    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);
    }

    public List<Effect> getEffectList() {
        return effectList;
    }
}
