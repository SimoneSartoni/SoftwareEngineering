package view.gui;

import javafx.application.Platform;
import model.board.AmmoTile;
import model.board.KillShotTrack;
import model.board.TileView;
import model.enums.*;
import model.player.PlayerView;
import model.powerup.PowerUp;
import model.utility.LobbyInfo;
import model.utility.MapInfoView;
import model.weapon.Effect;
import model.weapon.Weapon;
import network.ClientContext;
import network.ClientController;
import network.ViewListener;
import run.LaunchClient;
import view.ViewClient;

import java.io.*;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GUIListener extends UnicastRemoteObject implements ViewListener {

    private transient final ClientController clientController;
    private transient final ViewClient viewClient;
    private transient final GUIViewJavaFX guiViewJavaFX;

    GUIListener(ViewClient viewClient, ClientController clientController) throws RemoteException{
        this.viewClient = viewClient;
        this.guiViewJavaFX = ((GUIViewJavaFX) viewClient);
        this.clientController = clientController;
    }

    @Override
    public void onMoreText(List<String> content) throws RemoteException, SocketException {
        for(String s : content) {
            onText(s);
        }
    }

    @Override
    public void onText(String content) throws RemoteException, SocketException {
        if(guiViewJavaFX.getPlayingScene() == null && guiViewJavaFX.getActiontarget() != null) {
            guiViewJavaFX.getActiontarget().appendText(content);
            guiViewJavaFX.getActiontarget().appendText(System.getProperty("line.separator"));
        }
        else {
            guiViewJavaFX.onUpdate(PaneToUpdate.LOGGER, content);
        }
    }

    /***
     * Behavior when the on damage response is received
     * @param targetPlayer the player targetted by the damage
     * @param shooter the player who shoot
     * @param newDmg the damage done
     * @param marksDown the marks to transform in damage
     */
    @Override
    public void onDamage(PlayerView targetPlayer, PlayerView shooter, int newDmg, int marksDown) throws RemoteException, SocketException {
        if (shooter.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID())) {
            onText("You have dealt " + newDmg + " damage (+" + marksDown + " marks down) to " + targetPlayer.getPlayerID() + " (" + targetPlayer.getPlayerColor() + ")");
            if (targetPlayer.getPlayerState() == PlayerState.DEAD) {
                onMapInfo(onModifyMap(targetPlayer.getCurrentTile()), ClientContext.get().getKillShotTrack());
                if (targetPlayer.getDamageTakenView().size() == 11)
                    onText("You HAVE KILLED " + targetPlayer.getPlayerID() + " (" + targetPlayer.getPlayerColor() + ")!");
                if (targetPlayer.getDamageTakenView().size() == 12)
                    onText("You HAVE OVERKILLED " + targetPlayer.getPlayerID() + " (" + targetPlayer.getPlayerColor() + ")!");
            }
            onUpdateEnemyPlayer(targetPlayer);
            onUpdateCurrentPlayer(shooter);
            return;
        }
        if (targetPlayer.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID())) {
            onText(shooter.getPlayerID() + "(" + shooter.getPlayerColor() + ") has dealt " + newDmg + " damage (+" + marksDown + " marks down) to you!");
            if (targetPlayer.getPlayerState() == PlayerState.DEAD) {
                onMapInfo(onModifyMap(targetPlayer.getCurrentTile()), ClientContext.get().getKillShotTrack());
                if (targetPlayer.getDamageTakenView().size() == 11)
                    onText(shooter.getPlayerID() + "(" + shooter.getPlayerColor() + ") HAS KILLED you!");
                if (targetPlayer.getDamageTakenView().size() == 12)
                    onText(shooter.getPlayerID() + "(" + shooter.getPlayerColor() + ") HAS OVERKILLED you!");
            }
            onUpdateCurrentPlayer(targetPlayer);
            onUpdateEnemyPlayer(shooter);
            return;
        }
        onText(shooter.getPlayerID() + "(" + shooter.getPlayerColor() + ") has dealt " + newDmg + " damage (+" + marksDown + " marks down) to " + targetPlayer.getPlayerID() + " (" + targetPlayer.getPlayerColor() + ")");
        if (targetPlayer.getPlayerState() == PlayerState.DEAD) {
            onMapInfo(onModifyMap(targetPlayer.getCurrentTile()), ClientContext.get().getKillShotTrack());
            if (targetPlayer.getDamageTakenView().size() == 11)
                onText(shooter.getPlayerID() + "(" + shooter.getPlayerColor() + ") HAS KILLED " + targetPlayer.getPlayerID() + " (" + targetPlayer.getPlayerColor() + ")!");
            if (targetPlayer.getDamageTakenView().size() == 12)
                onText(shooter.getPlayerID() + "(" + shooter.getPlayerColor() + ") HAS OVERKILLED " + targetPlayer.getPlayerID() + " (" + targetPlayer.getPlayerColor() + ")!");
        }
        onUpdateEnemyPlayer(targetPlayer);
        onUpdateEnemyPlayer(shooter);
        Platform.runLater(() -> guiViewJavaFX.playAudio("/sounds/shoot.wav"));
    }

    /***
     * Behavior when the on marks response is received.
     * @param marked the player to who the marks are given
     * @param marker the player who gives the marks
     * @param newMarks the marks given in this action
     * @param oldMarks the marks he already holds
     */
    @Override
    public void onMarks(PlayerView marked, PlayerView marker, int newMarks, int oldMarks) throws RemoteException, SocketException {
        if (marker.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID())) {
            onText("You have given " + (newMarks - oldMarks) + " new mark(s) to " + marked.getPlayerID() + "(" + marked.getPlayerColor() + "), now he has " + newMarks + " mark(s)");
            onUpdateCurrentPlayer(marker);
            onUpdateEnemyPlayer(marked);
        } else {
            if (marked.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID())) {
                onText(marker.getPlayerID() + "(" + marker.getPlayerColor() + ")" + " has given " + (newMarks - oldMarks) + " new mark(s) to you, now you have " + newMarks + " mark(s)");
                onUpdateCurrentPlayer(marked);
                onUpdateEnemyPlayer(marker);
            } else {
                onText(marker.getPlayerID() + "(" + marker.getPlayerColor() + ")" + " has given " + (newMarks - oldMarks) + " new mark(s) to " + marked.getPlayerID() + "(" + marked.getPlayerColor() + "), now he has" + newMarks + " mark(s)");
                onUpdateEnemyPlayer(marked);
                onUpdateEnemyPlayer(marker);
            }
        }
        Platform.runLater(() -> guiViewJavaFX.playAudio("/sounds/shoot.wav"));
    }


    /***
     * Behavior of when the on movement response in received.
     * @param player the player that has been moved.
     * @param tile the tile in which the player has been moved
     */
    @Override
    public void onMovement(PlayerView player, TileView tile) throws RemoteException, SocketException {
        String playerName = player.getPlayerID() + "(" + player.getPlayerColor() + ")" + " has ";
        if (ClientContext.get().getCurrentPlayer().getPlayerID().equals(player.getPlayerID())) {
            playerName = "You have ";
        }
        onText(playerName + "moved to tile(" + tile.getX() + "," + tile.getY() + ") in room " + tile.getRoom());
        if (ClientContext.get().getCurrentPlayer().getPlayerID().equals(player.getPlayerID())) {
            onUpdateCurrentPlayer(player);
        } else {
            onUpdateEnemyPlayer(player);
        }
        onUpdateOldTile(player.getPlayerID());
        onMapInfo(onModifyMap(tile), ClientContext.get().getKillShotTrack());
        Platform.runLater(() -> guiViewJavaFX.playAudio("/sounds/run.wav"));
    }

    /***
     * Behavior when the on weapon grab response is received
     * @param player the player that has reload the weapon
     * @param weaponGrabbed the weapon grabbed from the tile
     * @param weaponDropped the weapon dropped to the tile. Can be null
     */
    @Override
    public void onWeaponGrab(PlayerView player, Weapon weaponGrabbed, Weapon weaponDropped) throws RemoteException, SocketException {
        if(!player.getCurrentTile().getWeapons().isEmpty()){
            ClientContext.get().getMap().getMap().get(player.getCurrentTile().getX()).get(player.getCurrentTile().getY()).getWeapons().remove(weaponGrabbed);
            if(weaponDropped!=null)
                ClientContext.get().getMap().getMap().get(player.getCurrentTile().getX()).get(player.getCurrentTile().getY()).getWeapons().add(weaponDropped);
            guiViewJavaFX.onUpdate(PaneToUpdate.GAMEBOARD);
        }
        if(weaponDropped!=null) {
            if (player.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID())) {
                onUpdateCurrentPlayer(player);
                ClientContext.get().removeWeapon(weaponDropped);
                onText("You have dropped a Weapon: " + weaponDropped.getIdName());
            } else {
                onUpdateEnemyPlayer(player);
                onText(player.getPlayerID() + "(" + player.getPlayerColor() + ") has dropped a Weapon: " + weaponGrabbed.getIdName());
            }
        }
        if (player.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID())) {
            onUpdateCurrentPlayer(player);
            ClientContext.get().addWeapon(weaponGrabbed);
            onText("You have grabbed a Weapon: " + weaponGrabbed.getIdName());
            guiViewJavaFX.onUpdate(PaneToUpdate.WEAPONS);
        } else {
            onUpdateEnemyPlayer(player);
            onText(player.getPlayerID() + "(" + player.getPlayerColor() + ") has grabbed a Weapon: " + weaponGrabbed.getIdName());
        }
        Platform.runLater(() -> guiViewJavaFX.playAudio("/sounds/grab.wav"));
    }

    /***
     * Behavior when the on ammo grab response is received
     * @param player the player that has grabbed the ammo tile
     * @param ammoTile the ammoTile grabbed
     */
    @Override
    public void onAmmoGrab(PlayerView player, AmmoTile ammoTile) throws RemoteException, SocketException {
        if (player.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID())) {
            onUpdateCurrentPlayer(player);
            onText("You have grabbed an ammo card, you gain " + ammoTile.getAmmoGained().getBlueValue() + " blue ammo(s), " + ammoTile.getAmmoGained().getRedValue() + " red ammo(s), " + ammoTile.getAmmoGained().getYellowValue() + " yellow ammo(s) and " + ammoTile.getNOfPowerUp() + " powerUp(s)");
        } else {
            onUpdateEnemyPlayer(player);
            onText(player.getPlayerID() + "(" + player.getPlayerColor() + ") has grabbed an ammo card, he gains " + ammoTile.getAmmoGained().getBlueValue() + " blue ammo(s), " + ammoTile.getAmmoGained().getRedValue() + " red ammo(s), " + ammoTile.getAmmoGained().getYellowValue() + " yellow ammo(s) and " + ammoTile.getNOfPowerUp() + " powerUp(s)");
        }
        onMapInfo(onModifyMap(player.getCurrentTile()),ClientContext.get().getKillShotTrack());
        Platform.runLater(() -> guiViewJavaFX.playAudio("/sounds/grab.wav"));
    }

    /***
     * Behavior of when the spawn response is received
     * @param spawned the player spawned
     * @param tile the tile in which the player has spawned
     */
    @Override
    public void onSpawn(PlayerView spawned, TileView tile) throws RemoteException, SocketException {
        String playerName = spawned.getPlayerID() + "(" + spawned.getPlayerColor() + ")" + " has ";
        if (ClientContext.get().getCurrentPlayer().getPlayerID().equals(spawned.getPlayerID())) {
            playerName = "You have ";
        }
        onText(playerName + "spawned in tile(" + tile.getX() + "," + tile.getY() + ") in room " + tile.getRoom());
        if (ClientContext.get().getCurrentPlayer().getPlayerID().equals(spawned.getPlayerID())) {
            onUpdateCurrentPlayer(spawned);
        } else {
            onUpdateEnemyPlayer(spawned);
        }

        onUpdateOldTile(spawned.getPlayerID());
        onMapInfo(onModifyMap(tile),ClientContext.get().getKillShotTrack());
        guiViewJavaFX.onUpdate(PaneToUpdate.GAMEBOARD);
    }

    /***
     * Behavior of when the on change turn response is received
     * @param endOfTurnPlayer the player who has end the turn
     * @param newTurnPlayer the player who will begin the new turn
     * @param tiles the tiles to update in the map
     */
    @Override
    public void onChangeTurn(PlayerView endOfTurnPlayer,PlayerView newTurnPlayer,List<TileView> tiles) throws RemoteException, SocketException {
        if (newTurnPlayer.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID())) {
            onText(endOfTurnPlayer.getPlayerID() + "(" + endOfTurnPlayer.getPlayerColor() + ") ends his turn. Now it's your turn!");
            ClientContext.get().setActiveTurn(true);
            onUpdateCurrentPlayer(newTurnPlayer);
            onUpdateEnemyPlayer(endOfTurnPlayer);
        } else {
            if (endOfTurnPlayer.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID())) {
                onText(" You end your turn. " + newTurnPlayer.getPlayerID() + "(" + newTurnPlayer.getPlayerColor() + ") now it's your turn!");
                ClientContext.get().setActiveTurn(false);
                onUpdateCurrentPlayer(endOfTurnPlayer);
                onUpdateEnemyPlayer(newTurnPlayer);
            } else {
                onText(endOfTurnPlayer.getPlayerID() + "(" + endOfTurnPlayer.getPlayerColor() + ") ends his turn. " + newTurnPlayer.getPlayerID() + "(" + newTurnPlayer.getPlayerColor() + ") now it's your turn!");
                onUpdateEnemyPlayer(newTurnPlayer);
                onUpdateEnemyPlayer(endOfTurnPlayer);
            }
        }
        MapInfoView map = ClientContext.get().getMap();
        for (TileView tile : tiles) {
            map = onModifyMap(tile);
            ClientContext.get().setMap(map);
        }
        onMapInfo(map,ClientContext.get().getKillShotTrack());
    }

    /***
     * Behavior when the on target response is received
     * @param players the list of the possible target to choose
     */
    @Override
    public void onTargets(List<PlayerView> players) throws RemoteException, SocketException {
        int cont = 0;
        onText("Choose one valid Target");
        for (PlayerView p : players) {
            onText(cont + "." + p.getPlayerColor().toString());
            cont++;
        }
        ClientContext.get().getPossibleChoices().setSelectableTargets(players);
        checkNothing();
    }

    /***
     * Behavior when the on room response is received
     * @param possibleRooms the list of possible rooms to choose
     */
    @Override
    public void onRooms(List<RoomColor> possibleRooms) throws RemoteException, SocketException {
        onText("Choose one valid Room");
        int cont = 0;
        for (RoomColor r : possibleRooms) {
            onText(cont + "." + r.toString());
            cont++;
        }
        ClientContext.get().getPossibleChoices().setSelectableRooms(possibleRooms);
        checkNothing();
    }

    /***
     * Behavior when the on tiles response is received
     * @param possibleTiles the list of possible tiles to choose
     */
    @Override
    public void onTiles(List<TileView> possibleTiles) throws RemoteException, SocketException {
        onText("Choose one valid tile");
        int cont = 0;
        for (TileView tile : possibleTiles) {
            onText(cont + ".tile(" + tile.getX() + "," + tile.getY() + ")");
            cont++;
        }
        ClientContext.get().getPossibleChoices().setSelectableTiles(possibleTiles);
        checkNothing();
    }

    /***
     * Behavior when the on direction response is received
     * @param possibleDirections the list of possible directions to choose
     */
    @Override
    public void onDirections(List<Direction> possibleDirections) throws RemoteException, SocketException {
        onText("Choose one valid direction");
        int cont = 0;
        for (Direction d : possibleDirections) {
            onText(cont + "." + d.toString());
            cont++;
        }
        ClientContext.get().getPossibleChoices().setSelectableDirections(possibleDirections);
        checkNothing();
    }

    /***
     * Behavior when the on powerup response is received
     * @param powerUps the list of possible powerups to choose
     */
    @Override
    public void onPowerUps(List<PowerUp> powerUps) throws RemoteException, SocketException {
        onText("Choose one valid power up");
        int cont = 0;
        for (PowerUp p : powerUps) {
            onText(cont + "." + p.getName() + "(" + p.getColor().name() + ")");
            cont++;
        }
        ClientContext.get().getPossibleChoices().setSelectablePowerUps(powerUps);
        checkNothing();
    }

    /***
     * Behavior when the on weapon response is received
     * @param weapons the list of possible weapons to choose
     */
    @Override
    public void onWeapons(List<Weapon> weapons) throws RemoteException, SocketException {
        onText("Choose one valid weapon");
        int cont = 0;
        for (Weapon w : weapons) {
            onText(cont + "." + w.getIdName());
            cont++;
        }
        ClientContext.get().getPossibleChoices().setSelectableWeapons(weapons);
        checkNothing();
    }

    /***
     * Behavior when the on ammo response is received
     * @param ammoColors the list of possible ammos to choose
     */
    @Override
    public void onAmmos(List<AmmoColor> ammoColors) throws RemoteException, SocketException {
        onText("Choose one valid ammo");
        int cont = 0;
        for (AmmoColor ammoColor : ammoColors) {
            onText(cont + "." + ammoColor.toString());
            cont++;
        }
        ClientContext.get().getPossibleChoices().setSelectableAmmo(ammoColors);
        checkNothing();
    }

    /***
     * Behavior when the on effect response is received
     * @param effects the list of possible effects to choose
     */
    @Override
    public void onEffects(List<Effect> effects) throws RemoteException, SocketException {
        onText("Choose one valid effect");
        int cont = 0;
        for (Effect e : effects) {
            onText(cont + "." + e.getName());
            cont++;
        }
        ClientContext.get().getPossibleChoices().setSelectableOptionalEffects(effects);
        checkNothing();
    }

    /***
     * Behavior when the on type of effect response is received
     * @param typeOfEffects the list of possible type of effects to choose
     */
    @Override
    public void onTypeEffects(List<TypeOfEffect> typeOfEffects) throws RemoteException, SocketException {
        onText("Choose one valid type of effect");
        int cont = 0;
        for (TypeOfEffect t : typeOfEffects) {
            onText(cont + "." + t.toString());
            cont++;
        }
        ClientContext.get().getPossibleChoices().setSelectableEffects(typeOfEffects);
        checkNothing();
    }

    /***
     * Behavior when the on token response is received. The token is written on the file containing all the tokens
     * of the user, one for each game to which he is conencted
     * @param token the token created
     */
    @Override
    public void onToken(String token) throws RemoteException, SocketException {
        ClientContext.get().setCurrentToken(token);
        clientController.getClient().setToken(token);
        viewClient.setToken(token);
        onText("Connected with token: "+ token);

        String path = LaunchClient.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        int lastSlashIndex = path.lastIndexOf("/");
        path = path.substring(0, lastSlashIndex + 1);

        File tokenFile = new File(path + "token.txt");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tokenFile, true)))
        {
            //token file not exist
            if(tokenFile.createNewFile()) {
                writer.write(token);
            }
            //otherwise ovveride it
            else {
                writer.append(System.getProperty("line.separator"));
                writer.append(token);
            }
        } catch (IOException e) {
            onText(e.getCause().getMessage());
        }
    }

    /***
     * Behavior when the on new player response is received.
     * @param player the player created
     */
    @Override
    public void onNewPlayer(PlayerView player) throws RemoteException, SocketException {
        if(ClientContext.get().getCurrentPlayer() == null || player.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID())) {
            onUpdateCurrentPlayer(player);
            onText("You have been joined to the game [ " + ClientContext.get().getValidGame() + " ]");
        }
        else {
            ClientContext.get().getPlayerViews().add(player);
            onText("Player " + player.getPlayerID() + " joined the game" );
        }
        guiViewJavaFX.onUpdate(PaneToUpdate.ENEMYPLAYER);
    }

    @Override
    public void onUpdateCurrentPlayer(PlayerView player) throws RemoteException, SocketException {
        ClientContext.get().setCurrentPlayer(player);
        if(guiViewJavaFX.getStartingPointsSize() == -1 && !player.getBoard().getPoints().isEmpty()) {
            guiViewJavaFX.setStartingPointsSize(player.getBoard().getPoints().size());
        }
        guiViewJavaFX.onUpdate(PaneToUpdate.PLAYER);
    }

    @Override
    public void onUpdateEnemyPlayer(PlayerView enemyPlayer) throws RemoteException, SocketException {
        ClientContext.get().replaceEnemyPlayer(enemyPlayer);
        if(ClientContext.get().getEnemyShownBoard()!= null &&
                ClientContext.get().getEnemyShownBoard().getPlayerID().equals(enemyPlayer.getPlayerID()))
            ClientContext.get().setEnemyShownBoard(enemyPlayer);
        guiViewJavaFX.onUpdate(PaneToUpdate.ENEMYPLAYER);
    }

    @Override
    public void onGameStarted(boolean started) throws RemoteException, SocketException {
        ClientContext.get().setGameStarted(started);
    }

    @Override
    public void onActiveTurn(boolean active) throws RemoteException, SocketException {
        ClientContext.get().setActiveTurn(active);
    }

    @Override
    public void onValidGame(int validGame) throws RemoteException, SocketException {
        ClientContext.get().setValidGame(validGame);
    }

    /***
     * Behavior when the on valid join response is received. A list of available games is shown
     * @param validJoin boolean representing if there is a valid game to join
     * @param toPrint the list of possible games
     * @param mapSize the number of maps
     */
    @Override
    public void onValidJoin(boolean validJoin, List<MapInfoView> toPrint, int mapSize) throws RemoteException, SocketException {
        ClientContext.get().setValidJoin(validJoin);
        ClientContext.get().setNumberOfMaps(mapSize);
        ClientContext.get().setPossibleGames(toPrint);
    }

    /**
     * not used in gui
     */
    @Override
    public void onStatus(PlayerView player) throws RemoteException, SocketException {
        viewClient.ack("On Status");
    }


    /***
     * Function to set possible commands
     */
    @Override
    public void onPrintHelp(List<String> printHelp) throws RemoteException, SocketException {
        if(printHelp.isEmpty()) {
            ClientContext.get().setPossibleCommands(new ArrayList<>());
        }
        else {
            ClientContext.get().setPossibleCommands(printHelp);
        }
    }

    /***
     * Behavior when the on powerup discard response is received
     * @param player the player that has discarded the powerup
     * @param powerUpServer the powerup to discard
     */
    @Override
    public void onPowerUpDiscard(PlayerView player, PowerUp powerUpServer) throws RemoteException, SocketException {
        List<String> results = new ArrayList<>();
        PowerUp powerUpClient = null;
        if (player.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID())) {
            for(PowerUp powerUp: ClientContext.get().getPowerUps())
                if(powerUp.getId()==powerUpServer.getId()){
                    powerUpClient = powerUp;
                }
            ClientContext.get().getPowerUps().remove(powerUpClient);
            onUpdateCurrentPlayer(player);
            guiViewJavaFX.onUpdate(PaneToUpdate.POWERUPS);
            results.add("You" + " have discarded the PowerUp: " + powerUpClient.getName());
        } else {
            onUpdateEnemyPlayer(player);
            results.add("Player " + player.getPlayerID() + " has discarded a PowerUp: " + powerUpServer.getName());
        }
        onMoreText(results);

    }

    /***
     * Behavior when the on powerup drawn by enemy response is received.
     * @param player the player who has drawn a powerup
     */
    @Override
    public void onPowerUpDrawnByEnemy(PlayerView player) throws RemoteException, SocketException {
        String playerName = "";
        playerName = player.getPlayerID() + "(" + player.getPlayerColor() + ") has ";
        onText(playerName + "drawn a powerUp");
        onUpdateEnemyPlayer(player);
    }

    /***
     * Behavior when the on powerup drawn response is received.
     * @param powerUp the powerup drawn
     */
    @Override
    public void onPowerUpDrawn(PowerUp powerUp) throws RemoteException, SocketException {
        onText("You have drawn a powerUp: " + powerUp.getName() + " of color: " + powerUp.getColor());
        ClientContext.get().addPowerup(powerUp);
        guiViewJavaFX.onUpdate(PaneToUpdate.POWERUPS);
    }

    /***
     * Behavior when the on reload weapon response is received
     * @param player the player that has reload the weapon
     * @param weapon the weapon reloaded
     */
    @Override
    public void onReloadWeapon(PlayerView player, Weapon weapon) throws RemoteException, SocketException {
        if (player.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID())) {
            onUpdateCurrentPlayer(player);
            ClientContext.get().addWeapon(weapon);
            onText("You have reloaded a Weapon: " + weapon.getIdName());
            guiViewJavaFX.onUpdate(PaneToUpdate.WEAPONS);
        } else {
            onUpdateEnemyPlayer(player);
            onText(player.getPlayerID() + "(" + player.getPlayerColor() + ") has reloaded a Weapon: " + weapon.getIdName());
        }
        Platform.runLater(() -> guiViewJavaFX.playAudio("/sounds/reload.wav"));
    }

    /**
     * not used in gui
     */
    @Override
    public void onOtherStatus(List<PlayerView> playerViews) throws RemoteException, SocketException {
        viewClient.ack("On Other status");
    }

    /***
     * Behavior when the on lobby stats response is received. To retrieve information about the possible maps
     * @param lobbyInfo the list of maps
     */
    @Override
    public void onLobbyStatus(List<LobbyInfo> lobbyInfo) throws RemoteException, SocketException {
        List<MapInfoView> maps = new ArrayList<>();
        for(LobbyInfo l : lobbyInfo) {
            maps.add(l.getMap());
        }
        ClientContext.get().setPossibleMaps(maps);
    }

    /***
     * Behavior when the on map response is received. To update the map of the player and the killshot track
     * @param mapInfo the updated map
     * @param killShotTrack the updated killshot track
     */
    @Override
    public void onMapInfo(MapInfoView mapInfo, KillShotTrack killShotTrack) throws RemoteException, SocketException {
        ClientContext.get().setMap(mapInfo);
        if (ClientContext.get().getKillShotTrack() == null) {
            ClientContext.get().setKillShotTrack(killShotTrack);
        }
        guiViewJavaFX.onUpdate(PaneToUpdate.GAMEBOARD);
    }

    /**
     * not used in gui
     */
    @Override
    public void onTileInfo(MapInfoView map, int x, int y) throws RemoteException, SocketException {
        viewClient.ack("On Tile info");
    }

    /***
     * Behavior when the on actions response is received. Gives the user his possible actions
     * @param typesOfActions the list of actions that is possible to execute
     */
    @Override
    public void onActions(List<TypeOfAction> typesOfActions) throws RemoteException, SocketException {
        onText("Choose one valid action to perform");
        int cont = 0;
        for (TypeOfAction typeOfAction : typesOfActions) {
            onText(cont + "." + typeOfAction.toString() + "(" + typeOfAction.toString() + ")");
            cont++;
        }
        ClientContext.get().getPossibleChoices().setSelectableActions(typesOfActions);
        checkNothing();
    }

    /***
     * Behavior when the on already logged user response is received. When a user is connected his list of token,
     * in his file of configuration, will be checked to see if he is a disconnected players.
     * If this is not the case, another token will be generated and the entry of the token file delted,
     * otherwise he is riconnected to the game he was in before
     * @param alreadyExistingToken the token for which the validation has been requested
     * @param exist the answer of the validation
     * @param anotherActive the answer of the validation related to the presence of another client with the same token
     */
    @Override
    public void onAlreadLoggedUser(String alreadyExistingToken, boolean exist, boolean anotherActive) throws RemoteException, SocketException {
        if(exist && anotherActive) {
            ClientContext.get().setAlreadyExisting(null);
        }
        else if(exist) {
            ClientContext.get().setAlreadyExisting(alreadyExistingToken);
            ClientContext.get().setCurrentToken(alreadyExistingToken);
            clientController.getClient().setToken(alreadyExistingToken);
            viewClient.setToken(alreadyExistingToken);
        }

        else if(!anotherActive){
            ClientContext.get().setAlreadyExisting(null);
            //e cancello il token dal file
            StringBuilder newContent = new StringBuilder();
            try {
                String path = LaunchClient.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                int lastSlashIndex = path.lastIndexOf("/");
                path = path.substring(0, lastSlashIndex + 1);

                File tokenFile = new File(path + "token.txt");
                try (BufferedReader reader = new BufferedReader(new FileReader(tokenFile))) {

                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        if (!line.trim().equals(alreadyExistingToken) && !line.isEmpty()) {
                            newContent.append(line);
                            newContent.append(System.getProperty("line.separator"));
                        }
                    }
                }
                FileWriter removeLine = new FileWriter(tokenFile);
                BufferedWriter change = new BufferedWriter(removeLine);
                PrintWriter replace = new PrintWriter(change);
                replace.write(newContent.toString());
                replace.close();
            } catch (IOException e) {
                onText(e.getMessage());
            }
        }
    }

    /***
     * Behavior when the on end game response is received. It sets in the gui the ending rank and scores, that will be used to create leaderboard screen.
     * @param endRank the list of players in a rank, ordered by their points
     * @param points the list of points for each player
     */
    @Override
    public void onEndGame(List<PlayerView> endRank,List<Integer> points) throws RemoteException, SocketException {
        clientController.getClient().setSynCheckTimer(false);
        guiViewJavaFX.onUpdate(PaneToUpdate.ALL);
        guiViewJavaFX.setEndRank(endRank);
        guiViewJavaFX.setPointsList(points);
        //closeConnection();
    }

    /***
     * Behavior when the on quit response is received. Quit the player who requested it, and notify the others. Before to close it opens the leaderboard screen
     * @param close boolean that indicates if the receiver has to be closed
     * @param name the name of the player who quit
     * @param causeOfConnection boolean representing if the quit has been caused by a disconnection
     */
    @Override
    public void onQuit(boolean close, String name, boolean causeOfConnection) throws RemoteException, SocketException {
        if (close) {
            System.out.println("Quitting the game");
            clientController.setToClose(true);
            guiViewJavaFX.createScoreBoard();
        }
        //someone has quit
        else if(!causeOfConnection){
            ClientContext.get().removeEnemyPlayer(name);
            guiViewJavaFX.onUpdate(PaneToUpdate.ENEMYPLAYER);
            onText("The player " + name + " has quit");
        }
        else {
            onText("The player " + name + " has been disconnected");
        }
    }

    @Override
    public void requestPossibleCommands() throws RemoteException, SocketException {
        ClientContext.get().setAskForCommand(true);
    }

    /***
     * Behavior when the on update weapons powerup and points response is received. Update the client reference of weapons,
     * powerups and points
     * @param weapons the list of weapons of the player
     * @param powerUps the list of power ups of the player
     * @param score the points of the player
     */
    @Override
    public void onUpdateWeaponsPowPoints(List<Weapon> weapons, List<PowerUp> powerUps, int score) throws RemoteException, SocketException {
        for(Weapon w : weapons) {
            ClientContext.get().addWeapon(w);
        }
        for(PowerUp p : powerUps) {
            ClientContext.get().addPowerup(p);
        }
        ClientContext.get().setPoints(score);
        guiViewJavaFX.onUpdate(PaneToUpdate.POWERUPS);
        guiViewJavaFX.onUpdate(PaneToUpdate.WEAPONS);
        guiViewJavaFX.onUpdate(PaneToUpdate.PLAYER);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    /**
     * not used in gui
     */
    @Override
    public void onPrintCommands(List<String> possibleCommands) throws RemoteException, SocketException {
        //empty
    }

    /***
     * Behavior when the on weapons used response is received.
     * @param playerView the player who is shooting
     * @param weapon the weapon the player is using
     */
    @Override
    public void onWeaponUsed(PlayerView playerView, Weapon weapon) throws RemoteException, SocketException {
        if(playerView.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID())) {
            onText("You are shooting with " + weapon.getIdName() + "!");
            ClientContext.get().addWeapon(weapon);
            guiViewJavaFX.onUpdate(PaneToUpdate.WEAPONS);
            onUpdateCurrentPlayer(playerView);
        }
        else {
            onText(playerView.getPlayerID()+"("+playerView.getPlayerColor()+") is shooting with "+weapon.getIdName()+"!");
            onUpdateEnemyPlayer(playerView);
        }

    }

    /***
     * Behavior when the on powerup used response is received.
     * @param playerView the player who is shooting
     * @param powerUpServer the powerup the player is using
     */
    @Override
    public void onPowerUpUsed(PlayerView playerView, PowerUp powerUpServer) throws RemoteException, SocketException {
        if (playerView.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID())){
            PowerUp powerUpClient = null;
            for(PowerUp powerUpLoop: ClientContext.get().getPowerUps())
                if(powerUpLoop.getId()==powerUpServer.getId()){
                    powerUpClient = powerUpLoop;
                }
            ClientContext.get().getPowerUps().remove(powerUpClient);
            onUpdateCurrentPlayer(playerView);
            guiViewJavaFX.onUpdate(PaneToUpdate.POWERUPS);
            onText("You have used " + powerUpClient.getName() + "!");
        }
        else {
            onText(playerView.getPlayerID()+"("+playerView.getPlayerColor()+") has used "+powerUpServer.getName()+"!");
            onUpdateEnemyPlayer(playerView);
        }
        guiViewJavaFX.onUpdate(PaneToUpdate.POWERUPS);
    }

    /***
     * Behavior of when the on points response is received.
     * The points gained are shown and if a double kill is made a message is shown.
     * @param points the points made
     * @param doubleKill boolean to check if a double kill is made
     * @param scoredOn the killed player
     */
    @Override
    public void onPoints(Map<PlayerView, Integer> points, boolean doubleKill, PlayerView scoredOn) throws RemoteException, SocketException {
        if (doubleKill) {
            String name;
            PlayerColor playerColor;
            for (PlayerView playerView : points.keySet()) {
                name = playerView.getPlayerID();
                playerColor = playerView.getPlayerColor();
                if (name.equals(ClientContext.get().getCurrentPlayer().getPlayerID()))
                    onText("You made a DOUBLEKILL!");
                else
                    onText(name + "(" + playerColor + ") has made a DOUBLEKILL!");
            }
        } else if(scoredOn!=null) {
            if (scoredOn.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID()))
                onText("Here are the points gained for the damage dealt to you!");
            else
                onText("Here are the points gained for the damage dealt to " + scoredOn.getPlayerID() + "(" + scoredOn.getPlayerColor() + ")!");
        }
        for (PlayerView v : points.keySet()) {
            if (v.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID())) {
                onText("You have gained " + points.get(v) + " point(s)!");
            }
            else
                onText(v.getPlayerID() + "(" + v.getPlayerColor() + ") has gained " + points.get(v) + " point(s)!");
            if (ClientContext.get().getCurrentPlayer().getPlayerID().equals(v.getPlayerID())) {
                onUpdateCurrentPlayer(v);
                ClientContext.get().addPoints(points.get(v));
            } else {
                onUpdateEnemyPlayer(v);
            }
        }
    }

    /***
     * Behavior when the on kill update response is received.
     * @param killer the player who killed another one
     * @param overKill boolean representing if a overkill has happenend
     * @param killed the player who has been killed
     */
    @Override
    public void onKillUpdate(PlayerView killer, boolean overKill, PlayerView killed) throws RemoteException, SocketException {
        if (killer.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID())) {
            if (overKill)
                onText("You put 2 token in the KillShotTrack for killing and overkilling " + killed.getPlayerID() + "(" + killed.getPlayerColor() + ")!");
            else
                onText("You put 1 token in the KillShotTrack for killing " + killed.getPlayerID() + "(" + killed.getPlayerColor() + ")!");

            Platform.runLater(() -> guiViewJavaFX.playAudio("/sounds/evil_laugh.wav"));
        }
        else {
            if (killed.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID())) {
                if (overKill)
                    onText(killer.getPlayerID() + "(" + killer.getPlayerColor() + ") put 2 token in the KillShotTrack for killing and overkilling you!");
                else
                    onText(killer.getPlayerID() + "(" + killer.getPlayerColor() + ") put 1 token in the KillShotTrack for killing you!");
            } else {
                if (overKill)
                    onText(killer.getPlayerID() + "(" + killer.getPlayerColor() + ") put 2 token in the KillShotTrack for killing and overkilling " + killed.getPlayerID() + "(" + killed.getPlayerColor() + ")!");
                else
                    onText(killer.getPlayerID() + "(" + killer.getPlayerColor() + ") put 1 token in the KillShotTrack for killing " + killed.getPlayerID() + "(" + killed.getPlayerColor() + ")!");
            }
        }
        ClientContext.get().getKillShotTrack().setKill(killer.getPlayerColor(), overKill);

        guiViewJavaFX.onUpdate(PaneToUpdate.PLAYER);
        guiViewJavaFX.onUpdate(PaneToUpdate.ENEMYPLAYER);
        guiViewJavaFX.onUpdate(PaneToUpdate.GAMEBOARD);
    }

    /***
     * Behavior when the on modify map response is received. The map is updated
     * @param tile the tile of the map to modify
     */
    private MapInfoView onModifyMap(TileView tile) {
        MapInfoView mapInfo = ClientContext.get().getMap();
        int x = tile.getX();
        int y = tile.getY();
        mapInfo.getMap().get(x).set(y, tile);
        return mapInfo;
    }

    /***
     * Method to update a tile in the Client Context moving a player from a tile to another
     * @param playerID the player thas has been moved
     */
    private void onUpdateOldTile(String playerID) {
        List<PlayerView> temp;
        MapInfoView mapInfo = ClientContext.get().getMap();
        for (List<TileView> tileRow : mapInfo.getMap())
            for (TileView tile : tileRow) {
                temp = new ArrayList<>(tile.getPlayerViews());
                for (PlayerView p : temp)
                    if (p.getPlayerID().equals(playerID)) {
                        tile.removePlayer(p);
                        break;
                    }
            }

    }

    /***
     * Behavior when the on final frenzy response is received. Updates all the player (cause they have new boards)
     * @param playerViews all the players to update
     */
    @Override
    public void onFinalFrenzyStart(List<PlayerView> playerViews) throws RemoteException, SocketException {
        onText("Final frenzy triggered!");
        for (PlayerView p : playerViews) {
            if (p.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID()))
                onUpdateCurrentPlayer(p);
            else
                onUpdateEnemyPlayer(p);
        }
        viewClient.setFinalFrenzy(true);
    }

    /***
     * Function to receive syn from server to know that the connection is present
     * @throws RemoteException
     * @throws SocketException
     */
    @Override
    public void onSyn() throws RemoteException, SocketException {
        clientController.onSyn();
    }

    /***
     * Function to set player as inactive if necessary
     * @param inactive boolean representing if the player is inactive or not
     * @throws RemoteException
     * @throws SocketException
     */
    @Override
    public void onInactivity(boolean inactive) throws RemoteException, SocketException {
        if(inactive) {
            if (ClientContext.get().getCurrentPlayer() != null) {
                ClientContext.get().setDisconnected(true);
            }
            if(!clientController.isToClose()){
                viewClient.ack("You have been disconnected cause of your inactivity. Press any button to try to reconnect.. ");
                clientController.getClient().setSynCheckTimer(false);
                guiViewJavaFX.createInactiveWindow();
            }
        }
        else {
            ClientContext.get().setDisconnected(false);
            viewClient.ack("Reconnected to the game");
            guiViewJavaFX.refresh();
        }
    }

    /**
     * Function to set that a player has lost connection
     * @throws RemoteException
     * @throws SocketException
     */
    @Override
    public void onLostConnection() throws RemoteException, SocketException {
        System.out.println("You have lost connection");
        ClientContext.get().setAlreadyExisting(null);

        if(!clientController.isToClose()) {
            Platform.runLater(
                    () -> guiViewJavaFX.start(guiViewJavaFX.getMainStage()));
        }
    }

    /***
     * Behavior of when the nothing command is available.
     */
    private void checkNothing() throws RemoteException, SocketException{
        if(ClientContext.get().getPossibleCommands().contains("nothing")){
            onText("This choice is optional. You can give the nothing command to avoid it");
        }
    }
}
