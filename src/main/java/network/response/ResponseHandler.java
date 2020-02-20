package network.response;

import network.notify.*;

import java.rmi.RemoteException;

public interface ResponseHandler {
    void handle(TextResponse textResponse) throws RemoteException;

    void handle(PlayerCreatedResponse joinedGameResponse) throws RemoteException;

    void handle(NotifyOnEnemyPlayer joinedGameResponse) throws RemoteException;

    void handle(TokenGeneratedResponse tokenGeneratedResponse) throws RemoteException;

    void handle(GameStartedResponse gameStartedResponse) throws RemoteException;

    void handle(ActiveTurnResponse activeTurnResponse) throws RemoteException;

    void handle(ValidGameResponse validGameResponse) throws RemoteException;

    void handle(GamesStatusResponse gamesStatusResponse) throws RemoteException;

    void handle(PrintHelpResponse printHelpResponse) throws RemoteException;

    void handle(ChooseAmmoResponse chooseAmmoResponse) throws RemoteException;

    void handle(ChooseDirectionResponse chooseDirectionResponse) throws RemoteException;

    void handle(ChooseEffectResponse chooseEffectResponse) throws RemoteException;

    void handle(NotifyOnPowerUp notifyOnPowerUp) throws RemoteException;

    void handle(ChooseRoomResponse chooseRoomResponse) throws RemoteException;

    void handle(ChooseTargetResponse chooseTargetResponse) throws RemoteException;

    void handle(ChooseTileResponse chooseTileResponse) throws RemoteException;

    void handle(ChooseTypeOfEffectResponse chooseTypeOfEffectResponse) throws RemoteException;

    void handle(ChooseWeaponResponse chooseWeaponResponse) throws RemoteException;

    void handle(MultiTextResponse multiTextResponse) throws RemoteException;

    void handle(NotifyOnAmmoTileGrab notifyOnAmmoTileGrab) throws RemoteException;

    void handle(NotifyOnWeaponGrab notifyOnWeaponGrab) throws RemoteException;

    void handle(NotifyOnActions notifyOnActions) throws  RemoteException;

    void handle(NotifyOnDamage notifyOnDamage) throws RemoteException;

    void handle(NotifyOnEndTurn notifyOnEndTurn) throws RemoteException;

    void handle(NotifyOnMarks notifyOnMarks) throws  RemoteException;

    void handle(NotifyOnMovement notifyOnMovement) throws RemoteException;

    void handle(NotifyOnWeaponReload notifyOnWeaponReload) throws RemoteException;

    void handle(NotifyOnPowerUpDiscard notifyOnPowerUpDiscard) throws RemoteException;

    void handle(NotifyOnPowerUpDrawByEnemy notifyOnPowerUpDrawByEnemy) throws RemoteException;

    void handle(NotifyOnPowerUpDrawn notifyOnPowerUpDrawn) throws RemoteException;

    void handle(NotifyOnPoints notifyOnPoints) throws  RemoteException;

    void handle(NotifyOnSpawn notifyOnSpawn) throws RemoteException;

    void handle(LobbyStatusResponse lobbyStatusResponse) throws RemoteException;

    void handle(MapInfoResponse mapInfoResponse) throws RemoteException;

    void handle(UpdateCurrentPlayerResponse updateCurrentPlayerResponse) throws RemoteException;

    void handle(UpdateEnemyPlayerResponse updateEnemyPlayerResponse) throws RemoteException;

    void handle(AlreadyExistingTokenResponse alreadyExistingTokenResponse) throws RemoteException;

    void handle(QuitResponse quitResponse) throws RemoteException;

    void handle(NotifyOnEndGame notifyOnEndGame) throws RemoteException;

    void handle(SetNewPossibileCommandsResponse setNewPossibileCommandsResponse) throws  RemoteException;

    void handle(NotifyOnPowerUpUsed notifyOnPowerUpUsed) throws RemoteException;

    void handle(NotifyOnWeaponUsed notifyOnWeaponUsed) throws RemoteException;

    void handle(NotifyOnKillUpdate notifyOnKillUpdate) throws RemoteException;

    void handle(UpdateWeaponsPowPointsResponse updateWeaponsPowPointsResponse) throws RemoteException;

    void handle(NotifyOnFinalFrenzyStart notifyOnFinalFrenzyStart) throws RemoteException;

    void handle(SynPing synPing) throws  RemoteException;

    void handle(InactivityResponse inactivityResponse) throws RemoteException;

    void handle(LostConnectionResponse lostConnectionResponse) throws RemoteException;
}
