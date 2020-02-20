package view;

import network.ViewListener;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ViewClient extends Remote {
    ViewListener getListener() throws RemoteException;
    void chooseGame() throws RemoteException;
    void playing() throws RemoteException;
    void setToken(String token);
    void refresh() throws RemoteException;
    void ack(String content);
    void errorAck(String content);
    void setFinalFrenzy(boolean finalFrenzy);
    boolean isFinalFrenzy();
    void denyMove();
}
