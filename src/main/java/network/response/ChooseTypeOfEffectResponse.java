package network.response;

import model.enums.TypeOfEffect;

import java.rmi.RemoteException;
import java.util.List;

public class ChooseTypeOfEffectResponse implements Response {
    private final List<TypeOfEffect> typeOfEffectList;

    public ChooseTypeOfEffectResponse(List<TypeOfEffect> typeOfEffectList) {
        this.typeOfEffectList = typeOfEffectList;
    }

    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);
    }

    public List<TypeOfEffect> getTypeOfEffectList() {
        return typeOfEffectList;
    }
}
