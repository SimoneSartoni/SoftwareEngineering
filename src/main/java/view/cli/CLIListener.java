package view.cli;

import model.board.AmmoTile;
import model.board.KillShotTrack;
import model.board.TileView;
import model.enums.*;
import model.player.PlayerView;
import model.powerup.PowerUp;
import model.utility.LobbyInfo;
import model.utility.MapInfo;
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

public class CLIListener extends UnicastRemoteObject implements ViewListener {

    private transient final ClientController clientController;
    private transient final ViewClient viewClient;

    CLIListener(ViewClient viewClient, ClientController clientController) throws RemoteException {
        this.viewClient = viewClient;
        this.clientController = clientController;
    }

    @Override
    public void onText(String content) {
        System.out.println(">>> " + content);
    }

    @Override
    public void onMoreText(List<String> content) {
        for (String s : content) {
            onText(s);
        }
    }

    /***
     * Behavior of when the nothing command is available.
     */
    private void checkNothing(){
        if(ClientContext.get().getPossibleCommands().contains("nothing")){
            onText("This choice is optional. You can type 'nothing' to avoid it");
        }
    }

    /***
     * Behavior of when the on points response is received.
     * The points gained are shown and if a double kill is made a message is shown.
     * @param points the points made
     * @param doubleKill boolean to check if a double kill is made
     * @param scoredOn the killed player
     */
    @Override
    public void onPoints(Map<PlayerView, Integer> points, boolean doubleKill, PlayerView scoredOn) {
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
        }
        else {
            if(scoredOn!=null) {
                if (scoredOn.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID()))
                    onText("Here are the points gained for the damage dealt to you!");
                else
                    onText("Here are the points gained for the damage dealt to " + scoredOn.getPlayerID() + "(" + scoredOn.getPlayerColor() + ")!");
            }
        }
        for (PlayerView v : points.keySet()) {
            if (v.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID())) {
                onText("You have gained " + points.get(v) + " point(s)!");
                ClientContext.get().addPoints(points.get(v));
            }
            else
                onText(v.getPlayerID() + "(" + v.getPlayerColor() + ") has gained " + points.get(v) + " point(s)!");
            if (ClientContext.get().getCurrentPlayer().getPlayerID().equals(v.getPlayerID())) {
                onUpdateCurrentPlayer(v);
            } else {
                onUpdateEnemyPlayer(v);
            }
        }
    }


    /***
     * Behavior of when the on movement response in received.
     * @param moved the player that has been moved.
     * @param tile the tile in which the player has been moved
     */
    @Override
    public void onMovement(PlayerView moved, TileView tile) {
        String playerName = moved.getPlayerID() + "(" + moved.getPlayerColor() + ")" + " has ";
        if (ClientContext.get().getCurrentPlayer().getPlayerID().equals(moved.getPlayerID())) {
            playerName = "You have ";
        }
        onText(playerName + "moved to tile(" + tile.getX() + "," + tile.getY() + ") in room " + tile.getRoom());
        if (ClientContext.get().getCurrentPlayer().getPlayerID().equals(moved.getPlayerID())) {
            onUpdateCurrentPlayer(moved);
        } else {
            onUpdateEnemyPlayer(moved);
        }
        onUpdateOldTile(moved.getPlayerID());
        onMapInfo(onModifyMap(tile), ClientContext.get().getKillShotTrack());
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
     * Behavior of when the spawn response is received
     * @param player the player spawned
     * @param tile the tile in which the player has spawned
     */
    @Override
    public void onSpawn(PlayerView player, TileView tile) {
        String playerName = player.getPlayerID() + "(" + player.getPlayerColor() + ")" + " has ";
        if (ClientContext.get().getCurrentPlayer().getPlayerID().equals(player.getPlayerID())) {
            playerName = "You have ";
        }
        onText(playerName + "spawned in tile(" + tile.getX() + "," + tile.getY() + ") in room " + tile.getRoom());
        if (ClientContext.get().getCurrentPlayer().getPlayerID().equals(player.getPlayerID())) {
            onUpdateCurrentPlayer(player);
        } else {
            onUpdateEnemyPlayer(player);
        }

            onUpdateOldTile(player.getPlayerID());
        onMapInfo(onModifyMap(tile), ClientContext.get().getKillShotTrack());

    }

    /***
     * Behavior of when the on change turn response is received
     * @param endOfTurnPlayer the player who has end the turn
     * @param newTurnPlayer the player who will begin the new turn
     * @param tiles the tiles to update in the map
     */
    @Override
    public void onChangeTurn(PlayerView endOfTurnPlayer, PlayerView newTurnPlayer, List<TileView> tiles) {
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
        onMapInfo(map, ClientContext.get().getKillShotTrack());

    }

    /***
     * Behavior when the on target response is received
     * @param possibleTargets the list of the possible target to choose
     */
    @Override
    public void onTargets(List<PlayerView> possibleTargets) {
        int cont = 0;
        onText("Choose one valid Target ('target' command) between these ones:");
        for (PlayerView p : possibleTargets) {
            onText(cont + "." + MapDrawing.drawColor(p.getPlayerColor().toString()));
            cont++;
        }
        ClientContext.get().getPossibleChoices().setSelectableTargets(possibleTargets);
        checkNothing();
    }

    /***
     * Behavior when the on room response is received
     * @param possibleRooms the list of possible rooms to choose
     */
    @Override
    public void onRooms(List<RoomColor> possibleRooms) {
        onText("Choose one valid Room ('room' command) between these ones:");
        int cont = 0;
        for (RoomColor r : possibleRooms) {
            onText(cont + "." + MapDrawing.drawColor(r.toString()));
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
    public void onTiles(List<TileView> possibleTiles) {
        onText("Choose one valid tile ('tile' command) between these ones:");
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
    public void onDirections(List<Direction> possibleDirections) {
        onText("Choose one valid direction ('direction' command) between these ones:");
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
    public void onPowerUps(List<PowerUp> powerUps) {
        onText("Choose one valid power up ('powerup' command) between these ones:");
        int cont = 0;
        for (PowerUp p : powerUps) {
            onText(cont + "." + p.getName() +" "+ MapDrawing.drawColorPoint(p.getColor().toString()));
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
    public void onWeapons(List<Weapon> weapons) {
        onText("Choose one valid weapon ('weapon' command) between these ones:");
        int cont = 0;
        for (Weapon w : weapons) {
            onText(cont + "." + w.getIdName()+" "+ MapDrawing.printCost(w));
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
    public void onAmmos(List<AmmoColor> ammoColors) {
        onText("Choose one valid ammo ('ammo' command) between these ones:");
        int cont = 0;
        for (AmmoColor ammoColor : ammoColors) {
            onText(cont + "." + MapDrawing.drawColor(ammoColor.toString()));
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
    public void onEffects(List<Effect> effects) {

        onText("Choose one valid effect ('effect' command) between these ones:");
        int cont = 0;
        for (Effect e : effects) {
            onText(cont + "." + e.getName()+" "+ MapDrawing.printCostEffect(e));
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
    public void onTypeEffects(List<TypeOfEffect> typeOfEffects) {
        onText("Choose one valid type of effect ('type' command) between these ones:");
        int cont = 0;
        for (TypeOfEffect t : typeOfEffects) {
            onText(cont + "." + t.toString());
            cont++;
        }
        ClientContext.get().getPossibleChoices().setSelectableEffects(typeOfEffects);
        checkNothing();
    }

    /***
     * Behavior when the on powerup discard response is received
     * @param player the player that has discarded the powerup
     * @param p the powerup to discard
     */
    @Override
    public void onPowerUpDiscard(PlayerView player, PowerUp p) {
        List<String> results = new ArrayList<>();
        if (player.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID())) {
            onUpdateCurrentPlayer(player);
            ClientContext.get().removePowerup(p);
            results.add("You" + " have discarded the PowerUp: " + p.getName()+" "+ MapDrawing.drawColorPoint(p.getColor().toString()));
        } else {
            onUpdateEnemyPlayer(player);
            results.add("Player " + player.getPlayerID() + " has discarded a PowerUp: " + p.getName()+" "+MapDrawing.drawColorPoint(p.getColor().toString()));
        }
        onMoreText(results);
    }

    /***
     * Behavior when the on reload weapon response is received
     * @param player the player that has reload the weapon
     * @param weapon the weapon reloaded
     */
    @Override
    public void onReloadWeapon(PlayerView player, Weapon weapon) {
        if (player.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID())) {
            onUpdateCurrentPlayer(player);
            ClientContext.get().addWeapon(weapon);
            onText("You have reloaded a Weapon: " + weapon.getIdName());
        } else {
            onUpdateEnemyPlayer(player);
            onText(player.getPlayerID() + "(" + player.getPlayerColor() + ") has reloaded a Weapon: " + weapon.getIdName());
        }
    }

    /***
     * Behavior when the on weapon grab response is received
     * @param player the player that has reload the weapon
     * @param weaponGrabbed the weapon grabbed from the tile
     * @param weaponDropped the weapon dropped to the tile. Can be null
     */
    @Override
    public void onWeaponGrab(PlayerView player, Weapon weaponGrabbed,Weapon weaponDropped){
        if(weaponDropped!=null) {
            if (player.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID())) {
                onUpdateCurrentPlayer(player);
                ClientContext.get().removeWeapon(weaponDropped);
                onText("You have dropped a Weapon: " + weaponDropped.getIdName());
            } else {
                onUpdateEnemyPlayer(player);
                onText(player.getPlayerID() + "(" + player.getPlayerColor() + ") has dropped a Weapon: " + weaponDropped.getIdName());
            }
        }
        if (player.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID())) {
            onUpdateCurrentPlayer(player);
            ClientContext.get().addWeapon(weaponGrabbed);
            onText("You have grabbed a Weapon: " + weaponGrabbed.getIdName());
        } else {
            onUpdateEnemyPlayer(player);
            onText(player.getPlayerID() + "(" + player.getPlayerColor() + ") has grabbed a Weapon: " + weaponGrabbed.getIdName());
        }
        onMapInfo(onModifyMap(player.getCurrentTile()), ClientContext.get().getKillShotTrack());
    }

    /***
     * Behavior when the on ammo grab response is received
     * @param player the player that has grabbed the ammo tile
     * @param ammoTile the ammoTile grabbed
     */
    @Override
    public void onAmmoGrab(PlayerView player, AmmoTile ammoTile) {
        if (player.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID())) {
            onUpdateCurrentPlayer(player);
            onText("You have grabbed an ammo card, you gain " + ammoTile.getAmmoGained().getBlueValue() + " blue ammo(s), " + ammoTile.getAmmoGained().getRedValue() + " red ammo(s), " + ammoTile.getAmmoGained().getYellowValue() + " yellow ammo(s) and " + ammoTile.getNOfPowerUp() + " powerUp(s)");
        } else {
            onUpdateEnemyPlayer(player);
            onText(player.getPlayerID() + "(" + player.getPlayerColor() + ") has grabbed an ammo card, he gains " + ammoTile.getAmmoGained().getBlueValue() + " blue ammo(s), " + ammoTile.getAmmoGained().getRedValue() + " red ammo(s), " + ammoTile.getAmmoGained().getYellowValue() + " yellow ammo(s) and " + ammoTile.getNOfPowerUp() + " powerUp(s)");
        }
        onMapInfo(onModifyMap(player.getCurrentTile()), ClientContext.get().getKillShotTrack());
    }

    /***
     * Behavior when the on token response is received. The token is written on the file containing all the tokens
     * of the user, one for each game to which he is conencted
     * @param token the token created
     */
    @Override
    public void onToken(String token) {
        ClientContext.get().setCurrentToken(token);
        clientController.getClient().setToken(token);
        viewClient.setToken(token);
        onText("Connected with token: " + token);

        String path = LaunchClient.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        int lastSlashIndex = path.lastIndexOf("/");
        path = path.substring(0, lastSlashIndex + 1);
        File tokenFile = new File(path + "token.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tokenFile, true))) {
            //token file not exist
            if (tokenFile.createNewFile()) {
                writer.write(token);
            }
            //otherwise override it
            else {
                writer.append(System.getProperty("line.separator"));
                writer.append(token);
            }
        } catch (IOException e) {
            onText(e.getMessage());
        }
    }

    /***
     * Behavior when the on new player response is received.
     * @param player the player created
     */
    @Override
    public void onNewPlayer(PlayerView player) {
        if (ClientContext.get().getCurrentPlayer() == null || player.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID())) {
            onUpdateCurrentPlayer(player);
            onText("You have been joined to the game [ " + ClientContext.get().getValidGame() + " ]");
        } else {
            ClientContext.get().getPlayerViews().add(player);
            onText("Player " + player.getPlayerID() + " joined the game");
        }
    }

    @Override
    public void onUpdateCurrentPlayer(PlayerView player) {
        ClientContext.get().setCurrentPlayer(player);
    }

    @Override
    public void onUpdateEnemyPlayer(PlayerView enemyPlayer) {
        ClientContext.get().replaceEnemyPlayer(enemyPlayer);
    }

    @Override
    public void onGameStarted(boolean started) {
        ClientContext.get().setGameStarted(started);
    }

    @Override
    public void onActiveTurn(boolean active) {
        ClientContext.get().setActiveTurn(active);
    }

    @Override
    public void onValidGame(int validGame) {
        ClientContext.get().setValidGame(validGame);
    }

    /***
     * Behavior when the on valid join response is received. A list of available games is shown
     * @param validJoin boolean representing if there is a valid game to join
     * @param toPrint the list of possible games
     * @param mapSize the number of maps
     */
    @Override
    public void onValidJoin(boolean validJoin, List<MapInfoView> toPrint, int mapSize) {
        ClientContext.get().setValidJoin(validJoin);
        ClientContext.get().setNumberOfMaps(mapSize);
        ClientContext.get().setPossibleGames(toPrint);

        if (!validJoin) {
            onText("No valid game join found. Please create a new one" + System.getProperty("line.separator"));
            return;
        }

        List<String> res = new ArrayList<>();
        for (MapInfoView currentMap : toPrint) {
            int count = 0;
            res.add("Game [ " + currentMap.getActualGameId() + " ] is available to join");
            res.add(">>> Map: " + currentMap.getNumber());
            res.add(">>> End mode: " + currentMap.getActualEndMode());
            res.add(">>> Minimum number of players to run the game: " + currentMap.getMinNumberOfPlayer());
            List<PlayerView> playerViews = currentMap.getPlayerViews();
            res.add(">>> Player joined the game: " + playerViews.size() + " out of " + currentMap.getMaxNumberOfPlayer());
            for (PlayerView p : playerViews) {
                res.add(">>>>>> Player " + count + ": " + p.getPlayerID() + " | Color : " + p.getPlayerColor().name());
                count++;
            }
            res.add("----------------------------------");
        }
        onMoreText(res);
    }

    /***
     * Behavior when the on status response is received.
     * @param player the player to give the status
     */
    @Override
    public void onStatus(PlayerView player) {
        List<String> results = new ArrayList<>();
        if (player == null) {
            onText("ERROR WHILE READING PLAYER STATUS");
            return;
        }
        results.add("Player: " + player.getPlayerID());
        results.add("Color: " + player.getPlayerColor().name().toLowerCase());
        results.add("Score: " + ClientContext.get().getPoints());
        results.add("Player state: " + player.getPlayerState());
        results.add("Points board: " + player.getBoard().getPoints() + " | Points for first blood: " + player.getBoard().getPointsFirstBlood());
        results.add("Possible actions: " + player.getCurrentTurnAction().toString());
        results.add(MapDrawing.drawAmmo(player.getAmmo()));
        results.add("Dead " + player.getNOfDeaths() + " times");
        results.add("Killed " + player.getNOfKills() + " times other players");
        results.add(printDamageTaken(player));
        results.add(printMarks(player));
        if (player.getCurrentTile() != null) {
            results.add("Position: " + player.getCurrentTile().getX() + " " + player.getCurrentTile().getY());
        } else {
            results.add("Position: You are not spawned yet");
        }
        results.add("Weapons in hand: ");
        if (ClientContext.get().getWeapons().isEmpty()) {
            results.add(">>> You have not weapons currently");
        } else {
            results.addAll(printWeapons(ClientContext.get().getWeapons()));
        }
        results.add("Powerups in hand: ");
        results.addAll(printPowerUps());
        onMoreText(results);
    }

    /***
     * Behavior when the on other status response is received.
     * @param playerViews the list of all the enemies of which to give status
     */
    @Override
    public void onOtherStatus(List<PlayerView> playerViews) {
        PlayerView current = ClientContext.get().getCurrentPlayer();
        List<String> results = new ArrayList<>();
        for (PlayerView player : playerViews) {
            if (!player.getPlayerID().equals(current.getPlayerID())) {
                StringBuilder posBuilder = new StringBuilder();
                results.add("Enemy: " + player.getPlayerID());
                results.add("Color: " + player.getPlayerColor().name().toLowerCase());
                results.add("Enemy state: " + player.getPlayerState());
                results.add("Points board: " + player.getBoard().getPoints() + " | Points for first blood: " + player.getBoard().getPointsFirstBlood());
                if (player.getAmmo().getYellowValue() == 0 && player.getAmmo().getRedValue() == 0 && player.getAmmo().getBlueValue() == 0)
                    results.add("No ammo Currently");
                else {
                    results.add(MapDrawing.drawAmmo(player.getAmmo()));
                }
                results.add("Dead " + player.getNOfDeaths() + " times");
                results.add("Killed " + player.getNOfKills() + " times other players");
                results.add(printDamageTaken(player));
                results.add(printMarks(player));
                if (player.getCurrentTile() == null) {
                    posBuilder.append("He is not spawned yet");
                } else {
                    posBuilder.append("Tile ");
                    posBuilder.append(player.getCurrentTile().getX());
                    posBuilder.append(" ");
                    posBuilder.append(player.getCurrentTile().getY());
                }
                results.add("Position: " + posBuilder.toString());
                results.add("Unloaded weapons in hand: ");
                if (player.getUnloadedWeapons().isEmpty()) {
                    results.add("He has not unloaded weapons currently");
                } else {
                    printWeapons(player.getUnloadedWeapons());
                }
                results.add("Weapons loaded in hand:");
                if (player.getNOfPowerUps() == 0) {
                    results.add("He has no weapons hidden currently");
                } else {
                    results.add("He has " + player.getnOfLoadedWeapons() + " loaded weapons in hand");
                }
                results.add("Powerups in hand: ");
                if (player.getNOfPowerUps() == 0) {
                    results.add("He has no power ups currently");
                } else {
                    results.add("He has " + player.getNOfPowerUps() + " powerup in hand");
                }
                results.add("-------------------------------------------------------------------------------------");
            }
        }
        results.add("");
        onMoreText(results);
    }

    /***
     * Behavior when the on tile info response is received.
     * @param map the current map
     * @param x the x coordinate of the map
     * @param y the y coordinate of the map
     */
    @Override
    public void onTileInfo(MapInfoView map, int x, int y) {
        if (x >= map.getMapHeight() || x < 0 || y >= map.getMapWidth() || y < 0) {
            onText("ERROR: Not a valid tile");
            return;
        }
        TileView tile = map.getMap().get(x).get(y);

        if (!MapInfo.isPlayable(tile)) {
            onText("ERROR: Not a valid tile");
            return;
        }

        //print players
        if (!tile.getPlayerViews().isEmpty()) {
            onText(">>> This tile contains these players: ");
            for (PlayerView p : tile.getPlayerViews()) {
                onText(p.getPlayerID() + " (" + MapDrawing.drawColor(p.getPlayerColor().toString()) + ")");
            }
        }

        //print weapons
        if (tile.isSpawnPoint() && !tile.getWeapons().isEmpty()) {
            onText(">>> This tile contains these weapons: ");
            for (Weapon w : tile.getWeapons()) {
                onMoreText(w.printStatus());
            }
        }
        //print ammo tile
        else if (tile.getAmmo() != null) {
            onText(">>> This tile contains this ammo tile: Ammo" + MapDrawing.drawAmmo(tile.getAmmo().getAmmoGained()) + ",PowerUps gained: "+tile.getAmmo().getNOfPowerUp() + System.getProperty("line.separator"));
        } else {
            onText(">>> This tile is empty" + System.getProperty("line.separator"));
        }
    }

    /***
     * Function to set possible commands
     */
    @Override
    public void onPrintHelp(List<String> printHelp) {
        ClientContext.get().setPossibleCommands(printHelp);
    }

    /***
     * Function to print possible commands
     * @param printHelp the commands to print
     */
    public void onPrintCommands(List<String> printHelp) {
        if (printHelp.isEmpty()) {
            onText(System.getProperty("line.separator"));
        } else {
            List<String> newPrintHelp = new ArrayList<>();
            newPrintHelp.add("Valid commands are: ");
            newPrintHelp.addAll(printHelp);
            onMoreText(newPrintHelp);
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
    public void onDamage(PlayerView targetPlayer, PlayerView shooter, int newDmg, int marksDown) {
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
    }

    /***
     * Behavior when the on lobby stats response is received. To retrieve information about the possible maps
     * @param lobbyInfo the list of maps
     */
    public void onLobbyStatus(List<LobbyInfo> lobbyInfo) {
        List<MapInfoView> maps = new ArrayList<>();
        int count = 1;
        onText("These are the maps: ");
        for (LobbyInfo l : lobbyInfo) {
            onText("Map " + count);
            MapDrawing.drawMap(l.getMap(), ClientContext.get().getKillShotTrack());
            onText("Player colors allowed in this map: " + l.getPlayerColors());
            onText("End modes allowed in this map: " + l.getMods());
            onText("------------------------------------------------------");
            count++;
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
    public void onMapInfo(MapInfoView mapInfo, KillShotTrack killShotTrack) {
        ClientContext.get().setMap(mapInfo);
        if (ClientContext.get().getKillShotTrack() == null) {
            ClientContext.get().setKillShotTrack(killShotTrack);
        }
        if (ClientContext.get().isGameStarted()) {
            MapDrawing.drawMap(mapInfo, killShotTrack);
        }
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
    public void onAlreadLoggedUser(String alreadyExistingToken, boolean exist, boolean anotherActive) {
        if (exist && anotherActive) {
            ClientContext.get().setAlreadyExisting(null);
        } else if (exist) {
            ClientContext.get().setAlreadyExisting(alreadyExistingToken);
            ClientContext.get().setCurrentToken(alreadyExistingToken);
            clientController.getClient().setToken(alreadyExistingToken);
            viewClient.setToken(alreadyExistingToken);
        } else if (!anotherActive) {
            ClientContext.get().setAlreadyExisting(null);
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
     * Behavior when the on marks response is received.
     * @param marked the player to who the marks are given
     * @param marker the player who gives the marks
     * @param newMarks the marks given in this action
     * @param oldMarks the marks he already holds
     */
    @Override
    public void onMarks(PlayerView marked, PlayerView marker, int newMarks, int oldMarks) {
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
    }

    /***
     * Behavior when the on powerup drawn by enemy response is received.
     * @param player the player who has drawn a powerup
     */
    @Override
    public void onPowerUpDrawnByEnemy(PlayerView player) {
        String playerName = "";
        playerName = player.getPlayerID();
        onText(playerName+"("+ MapDrawing.drawColor(player.getPlayerColor().toString())+") has drawn a powerUp");
        onUpdateEnemyPlayer(player);
    }

    /***
     * Behavior when the on powerup drawn response is received.
     * @param powerUp the powerup drawn
     */
    @Override
    public void onPowerUpDrawn(PowerUp powerUp) {
        onText("You have drawn a powerUp: " + powerUp.getName()+" "+MapDrawing.drawColorPoint(powerUp.getColor().toString()));
        ClientContext.get().addPowerup(powerUp);
    }

    /***
     * Behavior when the on actions response is received. Gives the user his possible actions
     * @param typesOfActions the list of actions that is possible to execute
     */
    @Override
    public void onActions(List<TypeOfAction> typesOfActions) {
        onText("Choose one valid action to perform ('action' command) between these ones :");
        int cont = 0;
        for (TypeOfAction typeOfAction : typesOfActions) {
            onText(cont + "." + typeOfAction.toString() + "(" + typeOfAction.toString() + ")");
            cont++;
        }
        ClientContext.get().getPossibleChoices().setSelectableActions(typesOfActions);
        checkNothing();
    }

    /***
     * Behavior when the on quit response is received. Quit the player who requested it, and notify the others
     * @param close boolean that indicates if the receiver has to be closed
     * @param name the name of the player who quit
     * @param causeOfDisconnection boolean representing if the quit has been caused by a disconnection
     */
    @Override
    public void onQuit(boolean close, String name, boolean causeOfDisconnection) {
        //you have quit
        if (close) {
            onText("Quitting the game");
            clientController.setToClose(true);
        }
        //someone has quit
        else if (!causeOfDisconnection){
            ClientContext.get().removeEnemyPlayer(name);
            onText("The player " + name + " has quit");
        }
        else {
            onText("The player " + name + " has been disconnect");
        }
    }

    @Override
    public void requestPossibleCommands() throws RemoteException, SocketException {
        ClientContext.get().setAskForCommand(true);
    }

    /***
     * Behavior when the on end game response is received. Print the ending rank.
     * @param endRank the list of players in a rank, ordered by their points
     * @param points the list of points for each player
     */
    @Override
    public void onEndGame(List<PlayerView> endRank,List<Integer> points) throws RemoteException, SocketException {
        clientController.getClient().setSynCheckTimer(false);
        int i = 1;
        System.out.println("The game has ended!");
        for (PlayerView p : endRank) {
            switch (i) {
                case 1:
                    if (p.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID()))
                        onText("You are the winner! You scored:"+points.get(i-1)+"points!");
                    else
                        onText("The winner is:" + p.getPlayerID() + "(" + p.getPlayerColor() + ")! He scored:"+points.get(i-1)+"points!");
                    break;
                case 2:
                    if (p.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID()))
                        onText("You placed second! You scored:"+points.get(i-1)+"points!");
                    else
                        onText(p.getPlayerID() + "(" + p.getPlayerColor() + ") placed second! He scored:"+points.get(i-1)+"points!");
                    break;
                case 3:
                    if (p.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID()))
                        onText("You placed third! You scored:"+points.get(i-1)+"points!");
                    else
                        onText(p.getPlayerID() + "(" + p.getPlayerColor() + ") placed third! He scored:"+points.get(i-1)+"points!");
                    break;
                case 4:
                    if (p.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID()))
                        onText("You place fourth! You scored:"+points.get(i-1)+"points!");
                    else
                        onText(p.getPlayerID() + "(" + p.getPlayerColor() + ") placed fourth! He scored:"+points.get(i-1)+"points!");
                    break;
                case 5:
                    if (p.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID()))
                        onText("You placed fifth! You scored:"+points.get(i-1)+"points!");
                    else
                        onText(p.getPlayerID() + "(" + p.getPlayerColor() + ") placed fifth! He scored:"+points.get(i-1)+"points!");
                    break;
                default:
                    break;
            }
            i++;
        }
        onText(" ");
        onText("There's nothing more to do here, closing the client... Open a new one if you wanna play again");
        //closeConnection();
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
        for (Weapon w : weapons) {
            ClientContext.get().addWeapon(w);
        }
        for (PowerUp p : powerUps) {
            ClientContext.get().addPowerup(p);
        }
        ClientContext.get().setPoints(score);
    }

    private String printDamageTaken(PlayerView player) {
        StringBuilder damageBuilder = new StringBuilder();
        damageBuilder.append("Damage taken [color of the player who give it]: ");
        for (PlayerColor p : player.getDamageTakenView()) {
            damageBuilder.append(MapDrawing.drawColor(p.toString()));
            damageBuilder.append(" ");
        }
        return damageBuilder.toString();
    }

    private String printMarks(PlayerView player) {
        StringBuilder marksBuilder = new StringBuilder();
        marksBuilder.append("Marks[color of the player who give it]: ");
        for (PlayerColor p : player.getMarksView().keySet()) {
            marksBuilder.append(player.getMarksView().get(p));
            marksBuilder.append(" ");
            marksBuilder.append(MapDrawing.drawColor(p.name()));
            marksBuilder.append(" | ");
        }
        return marksBuilder.toString();
    }

    private List<String> printWeapons(List<Weapon> weaponList) {
        List<String> res = new ArrayList<>();
        for (Weapon w : weaponList) {
            String load = w.getLoaded().name();
            res.add(">>> "+ w.getIdName()+" "+ MapDrawing.printCost(w)+" :"+ load);
        }
        return res;
    }

    private List<String> printPowerUps() {
        List<String> res = new ArrayList<>();
        if (ClientContext.get().getPowerUps().isEmpty()) {
            res.add(">>> You have not power ups currently");
        } else {
            for (PowerUp p : ClientContext.get().getPowerUps()) {
                res.add(">>> " + p.getName() + " | " + MapDrawing.drawColorPoint(p.getColor().toString()));
                res.add("");
            }
        }
        return res;
    }

    /***
     * Behavior when the on weapons used response is received.
     * @param playerView the player who is shooting
     * @param weapon the weapon the player is using
     */
    @Override
    public void onWeaponUsed(PlayerView playerView, Weapon weapon) throws RemoteException, SocketException {
        if (playerView.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID())) {
            onText("You are shooting with " + weapon.getIdName() + "!");
            ClientContext.get().setUsedWeapon(weapon);
        }
        else {
            onText(playerView.getPlayerID() + "(" + playerView.getPlayerColor() + ") is shooting with " + weapon.getIdName() + "!");
            onUpdateEnemyPlayer(playerView);
        }
    }

    /***
     * Behavior when the on powerup used response is received.
     * @param playerView the player who is shooting
     * @param powerUp the powerup the player is using
     */
    @Override
    public void onPowerUpUsed(PlayerView playerView, PowerUp powerUp) throws RemoteException, SocketException {
        if (playerView.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID())) {
            onText("You have used " + powerUp.getName() + " "+MapDrawing.drawColorPoint(powerUp.getColor().toString())+ "!");
            ClientContext.get().removePowerup(powerUp);
        }
        else {
            onText(playerView.getPlayerID() + "(" + playerView.getPlayerColor() + ") has used " + powerUp.getName() + " "+MapDrawing.drawColorPoint(powerUp.getColor().toString())+ "!");
            onUpdateEnemyPlayer(playerView);
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
        if (killer.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID()))
            if (overKill)
                onText("You put 2 token in the KillShotTrack for killing and overkilling " + killed.getPlayerID() + "(" + killed.getPlayerColor() + ")!");
            else
                onText("You put 1 token in the KillShotTrack for killing " + killed.getPlayerID() + "(" + killed.getPlayerColor() + ")!");
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
        MapDrawing.drawMap(ClientContext.get().getMap(),ClientContext.get().getKillShotTrack());
    }

    /***
     * Behavior when the on final frenzy response is received. Updates all the player (cause they have new boards)
     * @param playerViews all the players to update
     */
    @Override
    public void onFinalFrenzyStart(List<PlayerView> playerViews) throws RemoteException, SocketException {
        onText("Final frenzy triggered!");
        for(PlayerView p: playerViews) {
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
            }
        }
        else {
            ClientContext.get().setDisconnected(false);
            viewClient.ack("Reconnected to the game");
        }
    }

    /**
     * Function to set that a player has lost connection
     * @throws RemoteException
     * @throws SocketException
     */
    @Override
    public void onLostConnection() throws RemoteException, SocketException {
        //nothing to do here
        System.out.println("You have lost connection");
        ClientContext.get().setAlreadyExisting(null);
    }
}