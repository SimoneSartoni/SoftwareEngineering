package network.requests;

import model.enums.TypeOfAction;
import network.ClientHandler;
import network.ServerController;

public class ChooseActionRequest extends Request{
    private final TypeOfAction typeOfAction;

    public ChooseActionRequest(String token, TypeOfAction typeOfAction) {
        super(token);
        this.typeOfAction = typeOfAction;
    }

    @Override
    public void handleRequest(ClientHandler clientHandler, ServerController remoteController) {
        remoteController.chooseAction(getToken(), typeOfAction);
    }
}
