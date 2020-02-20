package network;

import network.notify.*;
import network.response.*;

import java.net.SocketException;
import java.rmi.RemoteException;

public class ResponseDecoder implements ResponseHandler {

    private final Client client;
    private final ViewListener viewListener;

    public ResponseDecoder(Client client, ViewListener viewListener) {
        this.client = client;
        this.viewListener = viewListener;
    }

    private void errorAck(String content) {
        System.err.println(content);
    }
    
    //response
    @Override
    public void handle(TextResponse textResponse)  {
        try {
            viewListener.onText(textResponse.toString());
        }
        catch (RemoteException | SocketException e){
            errorAck(e.getMessage());
        }
        client.setReceivedResponse(false);
        synchronized (client){
            client.notifyAll();
        }
    }

    @Override
    public void handle(MultiTextResponse multiTextResponse)  {
        try {
            viewListener.onMoreText(multiTextResponse.getContent());
        }
        catch (RemoteException | SocketException e){
            errorAck(e.getMessage());
        }
        client.setReceivedResponse(false);
        synchronized (client){
            client.notifyAll();
        }
    }

    @Override
    public void handle(PlayerCreatedResponse playerCreatedResponse) {
        try {
            viewListener.onNewPlayer(playerCreatedResponse.getPlayer());
        }
        catch (RemoteException | SocketException e){
            errorAck(e.getMessage());
        }
        client.setReceivedResponse(false);
        synchronized (client) {
            client.notifyAll();
        }
    }

    @Override
    public void handle(QuitResponse quitResponse) {
        try {
            viewListener.onQuit(quitResponse.isClose(), quitResponse.getName(), quitResponse.isCauseOfDisconnection());
        }
        catch (RemoteException | SocketException e){
            errorAck(e.getMessage());
        }
        client.setReceivedResponse(false);
        synchronized (client) {
            client.notifyAll();
        }
    }


    @Override
    public void handle(UpdateCurrentPlayerResponse updateCurrentPlayerResponse)  {
        try {
            viewListener.onUpdateCurrentPlayer(updateCurrentPlayerResponse.getPlayer());
        }
        catch (RemoteException | SocketException e){
            errorAck(e.getMessage());
        }
        client.setReceivedResponse(false);
        synchronized (client) {
            client.notifyAll();
        }
    }

    @Override
    public void handle(UpdateEnemyPlayerResponse updateEnemyPlayerResponse)  {
        try {
            viewListener.onUpdateEnemyPlayer(updateEnemyPlayerResponse.getEnemyPlayer());
        }
        catch (RemoteException | SocketException e){
            errorAck(e.getMessage());
        }
        client.setReceivedResponse(false);
        synchronized (client) {
            client.notifyAll();
        }
    }

    @Override
    public void handle(NotifyOnEnemyPlayer enemyJoinedGameResponse)  {
        try {
            viewListener.onNewPlayer(enemyJoinedGameResponse.getPlayer());
        }
        catch (RemoteException | SocketException e){
            errorAck(e.getMessage());
        }
    }

    @Override
    public void handle(GameStartedResponse gameStartedResponse)  {
        try {
            viewListener.onGameStarted(gameStartedResponse.isStarted());
        }
        catch (RemoteException | SocketException e){
            errorAck(e.getMessage());
        }
        client.setReceivedResponse(false);
        synchronized (client) {
            client.notifyAll();
        }
    }

    @Override
    public void handle(ActiveTurnResponse activeTurnResponse)  {
        try {
            viewListener.onActiveTurn(activeTurnResponse.isActive());
        }
        catch (RemoteException | SocketException e){
            errorAck(e.getMessage());
        }
        client.setReceivedResponse(false);
        synchronized (client) {
            client.notifyAll();
        }
    }

    @Override
    public void handle(ValidGameResponse validGameResponse)  {
        try {
            viewListener.onValidGame(validGameResponse.getValid());
        }
        catch (RemoteException | SocketException e){
            errorAck(e.getMessage());
        }
        client.setReceivedResponse(false);
        synchronized (client) {
            client.notifyAll();
        }
    }

    @Override
    public void handle(GamesStatusResponse gamesStatusResponse)  {
        try {
            viewListener.onValidJoin(gamesStatusResponse.isValidJoin(), gamesStatusResponse.getToPrint(), gamesStatusResponse.getMapSize());
        }
        catch (RemoteException | SocketException e){
            errorAck(e.getMessage());
        }
        client.setReceivedResponse(false);
        synchronized (client) {
            client.notifyAll();
        }
    }


    @Override
    public void handle(TokenGeneratedResponse tokenGeneratedResponse) {
        try {
            viewListener.onToken(tokenGeneratedResponse.getToken());
        } catch (RemoteException | SocketException e){
            errorAck(e.getMessage());
        }
        client.setReceivedResponse(false);
        synchronized (client) { 
            client.notifyAll();
        }
    }

    @Override
    public void handle(AlreadyExistingTokenResponse alreadyExistingTokenResponse)  {
        try {
            viewListener.onAlreadLoggedUser(alreadyExistingTokenResponse.getAlreadyExistingToken(), alreadyExistingTokenResponse.isExist(), alreadyExistingTokenResponse.isAnotherActive());
        }
        catch (RemoteException | SocketException e){
            errorAck(e.getMessage());
        }
        client.setReceivedResponse(false);
        synchronized (client) {
            client.notifyAll();
        }
    }

    @Override
    public void handle(ChooseAmmoResponse chooseAmmoResponse)  {
        try {
            viewListener.onAmmos(chooseAmmoResponse.getAmmoColorList());
        } catch (RemoteException | SocketException e){
            errorAck(e.getMessage());
        }
        client.setReceivedResponse(false);
        synchronized (client) {
            client.notifyAll();
        }
    }

    @Override
    public void handle(ChooseRoomResponse chooseRoomResponse)  {
        try {
            viewListener.onRooms(chooseRoomResponse.getRoomColors());
        } catch (RemoteException | SocketException e){
            errorAck(e.getMessage());
        }
        client.setReceivedResponse(false);
        synchronized (client) {
            client.notifyAll();
        }
    }

    @Override
    public void handle(ChooseDirectionResponse chooseDirectionResponse)  {
        try {
            viewListener.onDirections(chooseDirectionResponse.getDirectionsList());
        } catch (RemoteException | SocketException e){
            errorAck(e.getMessage());
        }
        client.setReceivedResponse(false);
        synchronized (client) {
            client.notifyAll();
        }
    }

    @Override
    public void handle(ChooseEffectResponse chooseEffectResponse)  {
        try {
            viewListener.onEffects(chooseEffectResponse.getEffectList());
        } catch (RemoteException | SocketException e){
            errorAck(e.getMessage());
        }
        client.setReceivedResponse(false);
        synchronized (client) {
            client.notifyAll();
        }
    }

    @Override
    public void handle(NotifyOnPowerUp notifyOnPowerUp)  {
        try {
            viewListener.onPowerUps(notifyOnPowerUp.getPowerUpList());
        } catch (RemoteException | SocketException e){
            errorAck(e.getMessage());
        }
    }

    @Override
    public void handle(NotifyOnPowerUpDrawn notifyOnPowerUpDrawn)  {
        try {
            viewListener.onPowerUpDrawn(notifyOnPowerUpDrawn.getPowerUp());
        } catch (RemoteException | SocketException e){
            errorAck(e.getMessage());
        }
    }

    @Override
    public void handle(ChooseTargetResponse chooseTargetResponse)  {
        try {
            viewListener.onTargets(chooseTargetResponse.getPlayerViewList());
        } catch (RemoteException | SocketException e){
            errorAck(e.getMessage());
        }
        client.setReceivedResponse(false);
        synchronized (client) {
            client.notifyAll();
        }
    }

    @Override
    public void handle(ChooseTileResponse chooseTileResponse)  {
        try {
            viewListener.onTiles(chooseTileResponse.getTileList());
        } catch (RemoteException | SocketException e){
            errorAck(e.getMessage());
        }
        client.setReceivedResponse(false);
        synchronized (client) {
            client.notifyAll();
        }
    }

    @Override
    public void handle(ChooseTypeOfEffectResponse chooseTypeOfEffectResponse)  {
        try {
            viewListener.onTypeEffects(chooseTypeOfEffectResponse.getTypeOfEffectList());
        } catch (RemoteException | SocketException e){
            errorAck(e.getMessage());
        }
        client.setReceivedResponse(false);
        synchronized (client) {
            client.notifyAll();
        }
    }

    @Override
    public void handle(ChooseWeaponResponse chooseWeaponResponse)  {
        try {
            viewListener.onWeapons(chooseWeaponResponse.getWeaponList());
        } catch (RemoteException | SocketException e){
            errorAck(e.getMessage());
        }
        client.setReceivedResponse(false);
        synchronized (client) {
            client.notifyAll();
        }
    }

    @Override
    public void handle(PrintHelpResponse printHelpResponse)  {
        try {
            viewListener.onPrintHelp(printHelpResponse.getPrintHelp());
        } catch (RemoteException | SocketException e){
            errorAck(e.getMessage());
        }
        client.setReceivedResponse(false);
        synchronized (client) {
            client.notifyAll();
        }
    }

    @Override
    public void handle(NotifyOnAmmoTileGrab notifyOnAmmoTileGrab)  {
        try {
            viewListener.onAmmoGrab(notifyOnAmmoTileGrab.getPlayer(),notifyOnAmmoTileGrab.getAmmoTile());
        } catch (RemoteException | SocketException e){
            errorAck(e.getMessage());
        }
    }

    @Override
    public void handle(NotifyOnWeaponGrab notifyOnWeaponGrab)  {
        try {
            viewListener.onWeaponGrab(notifyOnWeaponGrab.getPlayer(),notifyOnWeaponGrab.getWeaponGrabbed(),notifyOnWeaponGrab.getWeaponDropped());
        } catch (RemoteException | SocketException e){
            errorAck(e.getMessage());
        }
    }

    @Override
    public void handle(NotifyOnDamage notifyOnDamage)  {
        try {
            viewListener.onDamage(notifyOnDamage.getDamaged(),notifyOnDamage.getPlayer(),notifyOnDamage.getDmg(),notifyOnDamage.getMarkDown());
        } catch (RemoteException | SocketException e){
            errorAck(e.getMessage());
        }
    }

    @Override
    public void handle(NotifyOnEndTurn notifyOnEndTurn)  {
        try {
            viewListener.onChangeTurn(notifyOnEndTurn.getEndOfTurnPlayer(),notifyOnEndTurn.getNewTurnPlayer(),notifyOnEndTurn.getTileList());
        } catch (RemoteException | SocketException e){
            errorAck(e.getMessage());
        }
    }

    @Override
    public void handle(NotifyOnMarks notifyOnMarks)  {
        try {
            viewListener.onMarks(notifyOnMarks.getMarked(),notifyOnMarks.getMarker(),notifyOnMarks.getNewMarks(),notifyOnMarks.getOldMarks());
        } catch (RemoteException | SocketException e){
            errorAck(e.getMessage());
        }
    }

    @Override
    public void handle(NotifyOnMovement notifyOnMovement)  {
        try {
            viewListener.onMovement(notifyOnMovement.getMovedPlayer(),notifyOnMovement.getTile());
        } catch (RemoteException | SocketException e){
            errorAck(e.getMessage());
        }
    }

    @Override
    public void handle(NotifyOnWeaponReload notifyOnWeaponReload)  {
        try {
            viewListener.onReloadWeapon(notifyOnWeaponReload.getPlayer(),notifyOnWeaponReload.getWeapon());
        } catch (RemoteException | SocketException e){
            errorAck(e.getMessage());
        }
    }

    @Override
    public void handle(NotifyOnPowerUpDiscard notifyOnPowerUpDiscard)  {
        try {
            viewListener.onPowerUpDiscard(notifyOnPowerUpDiscard.getPlayer(), notifyOnPowerUpDiscard.getPowerUp());
        } catch (RemoteException | SocketException e){
            errorAck(e.getMessage());
        }
    }

    @Override
    public void handle(NotifyOnActions notifyOnActions)  {
        try {
            viewListener.onActions(notifyOnActions.actions);
        } catch (RemoteException | SocketException e){
            errorAck(e.getMessage());
        }
    }

    @Override
    public void handle(NotifyOnPowerUpDrawByEnemy notifyOnPowerUpDrawByEnemy)  {
        try {
            viewListener.onPowerUpDrawnByEnemy(notifyOnPowerUpDrawByEnemy.getPlayer());
        } catch (RemoteException | SocketException e){
            errorAck(e.getMessage());
        }
    }

    @Override
    public void handle(NotifyOnPoints notifyOnPoints)  {
        try {
            viewListener.onPoints(notifyOnPoints.getPoints(),notifyOnPoints.isDoubleKill(),notifyOnPoints.getScoredOn());
        } catch (RemoteException | SocketException e){
            errorAck(e.getMessage());
        }
    }

    @Override
    public void handle(NotifyOnSpawn notifyOnSpawn)  {
        try {
            viewListener.onSpawn(notifyOnSpawn.getSpawnPlayer(),notifyOnSpawn.getSpawnTile());
        } catch (RemoteException | SocketException e){
            errorAck(e.getMessage());
        }
    }

    @Override
    public void handle(LobbyStatusResponse lobbyStatusResponse)  {
        try {
            viewListener.onLobbyStatus(lobbyStatusResponse.getLobbyInfo());
        }
        catch (RemoteException | SocketException e){
            errorAck(e.getMessage());
        }
        client.setReceivedResponse(false);
        synchronized (client) {
            client.notifyAll();
        }
    }

    @Override
    public void handle(MapInfoResponse mapInfoResponse)  {
        try {
            viewListener.onMapInfo(mapInfoResponse.getMapInfo(),mapInfoResponse.getKillShotTrack());
        }
        catch (RemoteException | SocketException e){
            errorAck(e.getMessage());
        }
        client.setReceivedResponse(false);
        synchronized (client) {
            client.notifyAll();
        }
    }


    @Override
    public void handle(NotifyOnEndGame notifyOnEndGame) throws RemoteException {
        try{
            viewListener.onEndGame(notifyOnEndGame.getPlayerViewList(),notifyOnEndGame.getPoints());
        }
        catch (RemoteException|SocketException e){
            errorAck(e.getMessage());
        }
    }

    @Override
    public void handle(SetNewPossibileCommandsResponse setNewPossibileCommandsResponse) throws RemoteException {
        try{
            viewListener.requestPossibleCommands();
        }
        catch (RemoteException|SocketException e){
            errorAck(e.getMessage());
        }
    }

    @Override
    public void handle(NotifyOnPowerUpUsed notifyOnPowerUpUsed) throws RemoteException {
        try{
            viewListener.onPowerUpUsed(notifyOnPowerUpUsed.getPlayerView(),notifyOnPowerUpUsed.getPowerUp());
        }
        catch (RemoteException|SocketException e){
            errorAck(e.getMessage());
        }
    }

    @Override
    public void handle(NotifyOnWeaponUsed notifyOnWeaponUsed) throws RemoteException {
        try{
            viewListener.onWeaponUsed(notifyOnWeaponUsed.getPlayerView(),notifyOnWeaponUsed.getWeapon());
        }
        catch (RemoteException|SocketException e){
            errorAck(e.getMessage());
        }
    }

    @Override
    public void handle(NotifyOnKillUpdate notifyOnKillUpdate) throws RemoteException {
        try{
            viewListener.onKillUpdate(notifyOnKillUpdate.getKiller(),notifyOnKillUpdate.isOverKill(),notifyOnKillUpdate.getKilled());
        }
        catch (RemoteException|SocketException e){
            errorAck(e.getMessage());
        }
    }


    @Override
    public void handle(UpdateWeaponsPowPointsResponse updateWeaponsPowPointsResponse) throws RemoteException {
        try{
            viewListener.onUpdateWeaponsPowPoints(updateWeaponsPowPointsResponse.getWeapons(), updateWeaponsPowPointsResponse.getPowerUps(), updateWeaponsPowPointsResponse.getScore());
        }
        catch (RemoteException|SocketException e){
            errorAck(e.getMessage());
        }
    }

    @Override
    public void handle(NotifyOnFinalFrenzyStart notifyOnFinalFrenzyStart) throws RemoteException {
        try{
            viewListener.onFinalFrenzyStart(notifyOnFinalFrenzyStart.getPlayers());
        }
        catch (RemoteException|SocketException e){
            errorAck(e.getMessage());
        }
    }

    @Override
    public void handle(SynPing synPing) throws RemoteException {
        try{
            viewListener.onSyn();
        }
        catch (RemoteException|SocketException e){
            errorAck(e.getMessage());
        }
    }

    @Override
    public void handle(InactivityResponse inactivityResponse) throws RemoteException {
        try{
            viewListener.onInactivity(inactivityResponse.isInactive());
        }
        catch (RemoteException|SocketException e){
            errorAck(e.getMessage());
        }
    }

    @Override
    public void handle(LostConnectionResponse lostConnectionResponse) throws RemoteException {
        try{
            viewListener.onLostConnection();
        }
        catch (RemoteException|SocketException e){
            errorAck(e.getMessage());
        }
    }
}
