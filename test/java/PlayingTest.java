import controller.TurnStateController;
import model.board.*;
import model.enums.*;
import model.exceptions.ShootingException;
import model.exceptions.WeaponsException;
import model.gamemodes.BoardStructure;
import model.gamemodes.Gamemodes;
import model.player.Player;
import model.player.ShootHandler;
import model.powerup.PowerUp;
import model.powerup.PowerUpEffect;
import model.utility.Ammo;
import model.utility.MapInfo;
import model.utility.MapInfoView;
import model.utility.TurnStateHandler;
import model.weapon.Effect;
import model.weapon.InitWeapons;
import model.weapon.Weapon;
import network.ViewProxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import view.cli.MapDrawing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static model.enums.Direction.EAST;
import static model.enums.Direction.SOUTH;
import static org.junit.jupiter.api.Assertions.*;

class PlayingTest {

    private GameManager gameManager;
    final String fileToParse = "fileToParse/";
    final String powFile = fileToParse + "powerUpToParse.txt";
    final String ammoFile = fileToParse + "ammoTilesToParse.txt";
    final String mapFile = fileToParse + "map.txt";
    final String pointsFile = fileToParse + "points.txt";
    final String weaponFile = fileToParse + "weaponsToParse.txt";
    final String commandsFile = fileToParse + "state_commands.txt";
    private static TurnStateController turnStateController;
    private MapInfo map;
    private KillShotTrack killShotTrack;
    @BeforeEach
    void before() {
        final Player [] arrayPlayers = {new Player("Yellow", PlayerColor.YELLOW, PlayerState.NORMAL, null, null),
                new Player("Purple", PlayerColor.PURPLE, PlayerState.NORMAL, null, null),
                new Player("Green", PlayerColor.GREEN, PlayerState.NORMAL, null, null)
        };
        List<MapInfo> allMaps = InitMap.initMap(mapFile);
        map = new MapInfo(0, 0, "sudden death",
                allMaps.get(0).getMapWidth(), allMaps.get(0).getMapHeight(),
                allMaps.get(0).getMap(), allMaps.get(0).getAllowedPlayerColors(), allMaps.get(0).getAllowedEndModes(),
                allMaps.get(0).getMaxNumberOfPlayer(), allMaps.get(0).getMinNumberOfPlayer(), new ArrayList<>());
        final List<Player> players = new ArrayList<>(Arrays.asList(arrayPlayers));
        List<ViewProxy> views=new ArrayList<>();
            final Integer [] scores = {8, 6, 4, 2, 1, 1};
        final Integer [] frenzyScores = {2, 1, 1, 1};
        killShotTrack = new KillShotTrack(Arrays.asList(scores), 8, new ArrayList<>(), 1);
        Gamemodes gamemodes = new Gamemodes(new BoardStructure(pointsFile));
        gameManager = new GameManager(new Deck<Weapon>(InitWeapons.initAllWeapons(weaponFile)), new Deck<PowerUp>(InitMap.initPowerUps(powFile)),
                new Deck<AmmoTile>(InitMap.initAmmoTiles(ammoFile)), players, allMaps.get(0).getMap(), gamemodes.getNormalMode(), killShotTrack,
                false, gamemodes, InitMap.initMap(mapFile).get(0).getMapWidth(), InitMap.initMap(mapFile).get(0).getMapHeight());
        for(Player p : gameManager.getPlayerOrderTurn()) {
            p.setCurrentTurnAction(gameManager.getCurrentGameMode().getBaseTurnAction());
        }
        gameManager.setNotifyObservers(views);
        turnStateController= new TurnStateController(commandsFile);
        for(Player p:players) {
            p.setViewPlayer(new ViewProxy(new ViewMock(), "ciao",turnStateController));
            views.add(p.getViewPlayer());
        }
    }

    private void spawn() {
        Player currentPlayer = gameManager.getCurrentPlayerTurn();
        Player enemy1 = gameManager.getPlayerOrderTurn().get(1);
        Player enemy2 = gameManager.getPlayerOrderTurn().get(2);
        //FOR TEST PURPOSES ONLY!: make start game until the current player is the yellow one
        while (currentPlayer == null || !currentPlayer.getPlayerID().equals("Yellow")){
            if(currentPlayer != null) {
                currentPlayer.discardPowerUp(0);
                currentPlayer.discardPowerUp(1);
            }
            gameManager.startGame();
            currentPlayer = gameManager.getCurrentPlayerTurn();
            gameManager.spawnDrawPhase(currentPlayer,2);
            while(gameManager.getPowerUpDeck().pileSize() > 0) {
                gameManager.getPowerUpDeck().draw();
            }
            List<PowerUp> pows = new ArrayList<>(InitMap.initPowerUps(powFile));
            List<PowerUp> supportPows = new ArrayList<>(pows);
            int count = 0;
            for(PowerUp pow : supportPows) {
                if(pow.equals(currentPlayer.getPowerUps().get(0)) || pow.equals(currentPlayer.getPowerUps().get(1))) {
                    pows.remove(pow);
                    count ++;
                    if(count == 2)
                        break;
                }
            }
            gameManager.getPowerUpDeck().addToPile(pows);
        }
        //set two players as if spawned
        enemy1.setCurrentTile(gameManager.getTiles().get(2).get(3));
        gameManager.getTiles().get(2).get(3).addPlayer(enemy1);
        enemy2.setCurrentTile(gameManager.getTiles().get(0).get(2));
        gameManager.getTiles().get(0).get(2).addPlayer(enemy2);

        //make the player discard a pow
        PowerUp toDiscard = currentPlayer.discardPowerUp(0);
        gameManager.spawnSetPosition(currentPlayer, toDiscard);
        gameManager.getPowerUpDeck().addToDiscardPile(toDiscard);
        //make the other to "spawn"
        enemy1.drawPowerUp(gameManager,gameManager.getPowerUpDeck().draw());
        enemy1.drawPowerUp(gameManager,gameManager.getPowerUpDeck().draw());
        enemy2.drawPowerUp(gameManager,gameManager.getPowerUpDeck().draw());
        enemy2.drawPowerUp(gameManager,gameManager.getPowerUpDeck().draw());
        toDiscard = enemy1.discardPowerUp(0);
        gameManager.getPowerUpDeck().addToDiscardPile(toDiscard);
        toDiscard = enemy2.discardPowerUp(0);
        gameManager.getPowerUpDeck().addToDiscardPile(toDiscard);
        toDiscard = null;
        enemy1.setTurnState(TurnState.READY_FOR_ACTION);
        enemy2.setTurnState(TurnState.READY_FOR_ACTION);

        //"brute" player moves
        currentPlayer.getCurrentTile().removePlayer(currentPlayer);
        gameManager.getTiles().get(1).get(0).addPlayer(currentPlayer);
        currentPlayer.setCurrentTile(gameManager.getTiles().get(1).get(0));
        enemy2.setCurrentTile(gameManager.getTiles().get(0).get(0));
    }

    private void fill() {
        gameManager.getWeaponsDeck().shuffle();
        gameManager.getPowerUpDeck().shuffle();
        gameManager.getAmmoTileDeck().shuffle();
        gameManager.fillAllAmmoTiles();
        gameManager.fillWeaponSpawnPoint();

    }

    @Test
    void shootingTest() {
        spawn();

        Player currentPlayer = gameManager.getCurrentPlayerTurn();
        Player enemy1 = gameManager.getPlayerOrderTurn().get(1);
        Player enemy2 = gameManager.getPlayerOrderTurn().get(2);
        List<Weapon> weapons = new ArrayList<>(InitWeapons.initAllWeapons(weaponFile));

        currentPlayer.setViewPlayer(new ViewProxy(new ViewMock(), "ciao",turnStateController));

        //JUST FOR TEST PURPOSES
        //ELIMINATE THE POWER UP OF THE OTHER PLAYERS AND MINE
        PowerUp powMine = currentPlayer.discardPowerUp(0);
        PowerUp powD1 = enemy1.discardPowerUp(0);
        PowerUp powD2 = enemy2.discardPowerUp(0);

        //to make the easiest case take a weapon which have only the base effect
        //"brute" pick a weapon
        //TODO
        //make he picks in the right way
        Weapon weapon = weapons.get(0);
        weapons.remove(0);
        while(gameManager.getWeaponsDeck().pileSize() > 0)
            gameManager.getWeaponsDeck().draw();
        gameManager.getWeaponsDeck().addToPile(weapons);
        currentPlayer.pickWeapon(weapon,gameManager);
        assertEquals(TurnState.READY_FOR_ACTION,currentPlayer.getTurnState());
        assertEquals(0,currentPlayer.getPowerUps().size());
        currentPlayer.chooseTileShootingAction(gameManager);
        ShootHandler.chooseTileShootingAction(currentPlayer,gameManager);
        List<Tile> tiles=new ArrayList<>();
        tiles.add(currentPlayer.getCurrentTile());
        gameManager.getCurrentTurn().getPossibleChoices().setSelectableTiles(tiles);
        try {
            Tile tile=currentPlayer.isAValidShootingAction(gameManager, GameManager.createTileView(currentPlayer.getCurrentTile()));
        } catch (ShootingException e){

        }
        currentPlayer.shootingActionAfterTile(currentPlayer.getCurrentTile(),gameManager);
        try {
            Weapon w=currentPlayer.isAValidWeaponToShoot(gameManager, weapon);
        } catch(WeaponsException weaponsException){assertTrue(false);};
        currentPlayer.startShootingWithAWeapon(gameManager, weapon);
        assertEquals(TurnState.CHOOSE_TARGET, currentPlayer.getTurnState());
        gameManager.getCurrentTurn().topActions().get(0).afterChooseTargetExecute(gameManager.getCurrentTurn().topEffect(), gameManager, currentPlayer, enemy2);
        assertEquals(2, enemy2.getDamageTaken().size());
        //assertEquals(currentPlayer.getTurnState(), TurnState.CAN_BE_COUNTER_ATTACKED);
        for(Player p : enemy2.getDamageTaken()) {
            assertEquals(currentPlayer, p);
        }
    }

    @Test
    void grabTest() {
        spawn();
        fill();

    }

    private void fillSetUp(){
        gameManager.fillAllAmmoTiles();
        Weapon w;
        List<Tile> spawnTiles=new ArrayList<>();
        assertEquals(21,gameManager.getWeaponsDeck().pileSize());
        while(true) {
            w = gameManager.getWeaponsDeck().draw();
            if (w.getIdName().equals("LOCK_RIFLE"))
                break;
            gameManager.getWeaponsDeck().addToDiscardPile(w);
            gameManager.getWeaponsDeck().reShuffleAll();
        }
        assertEquals(20,gameManager.getWeaponsDeck().pileSize());
        spawnTiles= gameManager.getSpawnTiles();
        for(Tile t:spawnTiles)
            if(t.getRoom()== RoomColor.RED)
                t.addWeapon(w);
        spawnTiles.clear();
            while (true) {
                w = gameManager.getWeaponsDeck().draw();
                if (w.getIdName().equals("ELECTROSCYTHE"))
                    break;
                gameManager.getWeaponsDeck().addToDiscardPile(w);
                gameManager.getWeaponsDeck().reShuffleAll();
            }
        assertEquals(19,gameManager.getWeaponsDeck().pileSize());
        spawnTiles = gameManager.getSpawnTiles();
            for (Tile t : spawnTiles)
                if (t.getRoom() == RoomColor.RED)
                    t.addWeapon(w);
        while (true) {
            w = gameManager.getWeaponsDeck().draw();
            if (w.getIdName().equals("SHOCKWAVE"))
                break;
            gameManager.getWeaponsDeck().addToDiscardPile(w);
            gameManager.getWeaponsDeck().reShuffleAll();
        }
        assertEquals(18,gameManager.getWeaponsDeck().pileSize());
        spawnTiles.clear();
            spawnTiles = gameManager.getSpawnTiles();
            for (Tile t : spawnTiles)
                if (t.getRoom() == RoomColor.RED)
                    t.addWeapon(w);
        PowerUp powerUp= new PowerUp(AmmoColor.RED,PowerUpUse.SEPARATED,"Teletrasporto",new PowerUpEffect(new Ammo(0,0,0),1,0,100,0,false,false,false));
        PowerUp powerUp1= new PowerUp(AmmoColor.RED,PowerUpUse.SEPARATED,"Teletrasporto",new PowerUpEffect(new Ammo(0,0,0),1,0,100,0,false,false,false));
        gameManager.getPowerUpDeck().removeAll();
        gameManager.getPowerUpDeck().addToDiscardPile(powerUp);
        gameManager.getPowerUpDeck().addToDiscardPile(powerUp1);
        gameManager.getPowerUpDeck().reShuffleAll();
        for(List<Tile> tiles:gameManager.getTiles())
            for(Tile t:tiles){
                if(!t.isSpawnPoint()) {
                    t.removeAmmo();
                    t.setAmmo(new AmmoTile(new Ammo(1,1,1),0));
                }
            }
    }


    @Test
    void spawnTest() {

    }


    @Test
    void testPersonInRoom() {
        for(Player p:gameManager.getPlayerOrderTurn()) {
            p.setCurrentTile(gameManager.getTiles().get(0).get(0));
            p.getCurrentTile().addPlayer(p);
        }
        assertEquals(3,gameManager.getPlayerOrderTurn().get(0).getCurrentTile().getPlayers().size());
        List<Player> ret=new ArrayList<>();
        for(Tile t:gameManager.getTileOfRoom(gameManager.getPlayerOrderTurn().get(0).getCurrentTile()))
            ret.addAll(t.getPlayers());
        ret.remove(gameManager.getPlayerOrderTurn().get(0));
        assertEquals(2,ret.size());
    }

    @Test
    void playingWithSetUpText() {
        fillSetUp();
        assertEquals(2, gameManager.getPowerUpDeck().pileSize());
        Collections.shuffle(gameManager.getPlayerOrderTurn());
        gameManager.setCurrentPlayer(gameManager.getPlayerOrderTurn().get(0));
        for (Player p : gameManager.getPlayerOrderTurn()) {
            if (!p.equals(gameManager.getCurrentPlayerTurn())) {
                p.setTurnState(TurnState.READY_TO_SPAWN);
            }
        }
        Player player = gameManager.getCurrentPlayerTurn();
        gameManager.spawnDrawPhase(player, 2);
        assertEquals(2, player.getPowerUps().size());
        assertEquals(player.getTurnState(), TurnState.DISCARD_POWERUP_FOR_SPAWN);
        gameManager.spawnSetPosition(player, player.getPowerUps().get(0));
        assertEquals(1, player.getPowerUps().size());
        assertEquals(3, gameManager.getCurrentTurn().getPossibleChoices().getSelectableActions().size());
        assertEquals(RoomColor.RED, player.getCurrentTile().getRoom());
        player.chooseTileGrabAction(gameManager);
        player.grabActionAfterTile(gameManager, player.getCurrentTile());
        player.pickWeapon(player.getCurrentTile().getWeapons().get(0), gameManager);
        assertEquals(1, player.getWeapons().size());
        assertEquals(TurnState.READY_FOR_ACTION, player.getTurnState());
        fillPowerUpDeck();
        fillBlueSpawnWeapon();
        player.chooseTileGrabAction(gameManager);
        player.grabActionAfterTile(gameManager, gameManager.getTiles().get(player.getCurrentTile().getX()).get(player.getCurrentTile().getY() + 1));
        assertEquals(2, player.getAmmo().getRedValue());
        assertEquals(2, player.getAmmo().getYellowValue());
        assertEquals(1, player.getAmmo().getBlueValue());
        assertEquals(TurnState.READY_FOR_ACTION, player.getTurnState());
        gameManager.changeTurn();
        player = gameManager.getCurrentPlayerTurn();
        assertEquals(AmmoColor.BLUE, player.getPowerUps().get(0).getColor());
        assertEquals(AmmoColor.BLUE, player.getPowerUps().get(1).getColor());
        assertEquals(TurnState.DISCARD_POWERUP_FOR_SPAWN, player.getTurnState());
        assertEquals(2, gameManager.getCurrentTurn().getPossibleChoices().getSelectablePowerUps().size());
        gameManager.spawnSetPosition(player, player.getPowerUps().get(0));
        fillPowerUpDeck();
        assertEquals(RoomColor.BLUE, player.getCurrentTile().getRoom());
        assertEquals(2, gameManager.getCurrentTurn().getPossibleChoices().getSelectableActions().size());
        /*  player.chooseTileGrabAction(gameManager);
        player.grabActionAfterTile(gameManager,player.getCurrentTile());*/
        fillYellowAmmoTile(gameManager.getTiles().get(player.getCurrentTile().getX() + 1).get(player.getCurrentTile().getY()));
        player.chooseTileGrabAction(gameManager);
        player.grabActionAfterTile(gameManager, gameManager.getTiles().get(player.getCurrentTile().getX() + 1).get(player.getCurrentTile().getY()));
        assertEquals(3, player.getAmmo().getYellowValue());
        player.chooseTileGrabAction(gameManager);
        player.grabActionAfterTile(gameManager, gameManager.getTiles().get(player.getCurrentTile().getX() - 1).get(player.getCurrentTile().getY()));
        assertEquals(3, gameManager.getCurrentTurn().getPossibleChoices().getSelectableWeapons().size());
        MapInfoView mapView = MapInfo.createMapView(map);
        MapDrawing.drawMap(mapView,killShotTrack);
        player.pickWeapon(player.getCurrentTile().getWeapons().get(0), gameManager);
        assertEquals("ROCKET_LAUNCHER", player.getWeapons().get(0).getIdName());
        assertEquals(0, gameManager.getCurrentTurn().getNOfActionMade());
        assertEquals(TurnState.WAIT_FOR_OTHER_PLAYERS_TURN, player.getTurnState());
        player = gameManager.getCurrentPlayerTurn();
        assertEquals(TurnState.DISCARD_POWERUP_FOR_SPAWN, player.getTurnState());
        gameManager.spawnSetPosition(player, player.getPowerUps().get(0));
        player.chooseTileGrabAction(gameManager);
        player.grabActionAfterTile(gameManager, player.getCurrentTile());
        player.pickWeapon(player.getCurrentTile().getWeapons().get(0), gameManager);
        assertEquals("RAILGUN", player.getWeapons().get(0).getIdName());
        player.discardPowerUpForAmmos(player.getPowerUps().get(0), gameManager);
        TurnStateHandler.handleAfterDiscardPowerUp(player,gameManager);
        assertEquals(1, gameManager.getCurrentTurn().getNOfActionMade());
        assertTrue(player.isValidShoot(gameManager));
        player.chooseTileShootingAction(gameManager);
        player.shootingActionAfterTile(player.getCurrentTile(), gameManager);
        assertTrue(player.getWeapons().get(0).isValid(player, gameManager));
        player.chooseWeaponToShoot(gameManager);
        player.startShootingWithAWeapon(gameManager, player.getWeapons().get(0));
        Weapon w = player.getWeapons().get(0);
        assertEquals(TurnState.CHOOSE_TYPE_OF_EFFECT, player.getTurnState());
        Effect e = w.getBaseEffect();
        w.baseEffectExecute(gameManager, player);
        assertEquals(TurnState.CHOOSE_DIRECTION, player.getTurnState());
        (e.getActions().get(0)).afterChooseDirectionExecute(e, gameManager, player, SOUTH);
        assertEquals(1, gameManager.getCurrentTurn().getPossibleChoices().getSelectableTargets().size());
        Player enemyTargeted = gameManager.getCurrentTurn().getPossibleChoices().getSelectableTargets().get(0);
        assertNotEquals(enemyTargeted, player);
        e.getActions().get(0).afterChooseTargetExecute(e, gameManager, player, gameManager.getCurrentTurn().getPossibleChoices().getSelectableTargets().get(0));
        assertEquals(3, enemyTargeted.getDamageTaken().size());
        assertEquals(TurnState.CHOOSE_COUNTER_ATTACK, enemyTargeted.getTurnState());
        assertEquals(TurnState.CAN_BE_COUNTER_ATTACKED, player.getTurnState());
        Player exPlayer = player;
        enemyTargeted.afterChosenPowerUp(gameManager, enemyTargeted.getPowerUps().get(0));
        assertEquals(TurnState.WAIT_FOR_OTHER_PLAYERS_TURN, enemyTargeted.getTurnState());
        assertEquals(1, exPlayer.getMarks().get(enemyTargeted));
        assertEquals(TurnState.READY_FOR_ACTION, gameManager.getCurrentPlayerTurn().getTurnState());
        player = gameManager.getCurrentPlayerTurn();
        assertEquals("LOCK_RIFLE", player.getWeapons().get(0).getIdName());
        player.chooseTileMovementAction(gameManager);
        player.movementActionAfterTile(gameManager, gameManager.getTiles().get(0).get(0));
        MapInfoView mapInfoView = MapInfo.createMapView(map);
        MapDrawing.drawMap(mapInfoView,killShotTrack);
        player.chooseTileShootingAction(gameManager);
        player.shootingActionAfterTile(player.getCurrentTile(), gameManager);
        for (Player p : gameManager.getPlayerOrderTurn())
            if (p.getPlayerColor() == PlayerColor.PURPLE)
                enemyTargeted = p;
        player.chooseWeaponToShoot(gameManager);
        player.startShootingWithAWeapon(gameManager, player.getWeapons().get(0));
        assertEquals(1, gameManager.getCurrentTurn().getCurrentWeapon().getPossibleTypeOfEffects(gameManager, player).size());
        assertEquals(TurnState.CHOOSE_TARGET, player.getTurnState());
        gameManager.getCurrentTurn().topActions().get(0).afterChooseTargetExecute(gameManager.getCurrentTurn().topEffect(), gameManager, player, enemyTargeted);
        assertEquals(TurnState.CHOOSE_EFFECT, player.getTurnState());
        assertEquals(1, gameManager.getCurrentTurn().getPossibleChoices().getSelectableOptionalEffects().size());
        w.optionalEffectExecute(gameManager.getCurrentTurn().getPossibleChoices().getSelectableOptionalEffects().get(0), gameManager, player);
        assertEquals(TurnState.DISCARD_POWERUP_FOR_COST_EFFECT, player.getTurnState());
        Ammo ammo = player.getAmmo();
        player.discardPowerUpForAmmos(player.getPowerUps().get(0), gameManager);
        TurnStateHandler.handleAfterDiscardPowerUp(player,gameManager);
        assertEquals(TurnState.CHOOSE_TARGET, player.getTurnState());
        assertEquals(PlayerColor.GREEN, gameManager.getCurrentTurn().getPossibleChoices().getSelectableTargets().get(0).getPlayerColor());
        assertEquals(1, gameManager.getCurrentTurn().getPossibleChoices().getSelectableTargets().size());
        for (Player p : gameManager.getPlayerOrderTurn())
            if (p.getPlayerColor() == PlayerColor.GREEN)
                enemyTargeted = p;
        gameManager.getCurrentTurn().topActions().get(0).afterChooseTargetExecute(gameManager.getCurrentTurn().topEffect(), gameManager, player, enemyTargeted);
        for (Player p : gameManager.getPlayerOrderTurn())
            if (p.getPlayerColor() == PlayerColor.PURPLE)
                enemyTargeted = p;
        enemyTargeted.setTurnState(TurnState.CHOOSE_COUNTER_ATTACK);
        assertFalse(enemyTargeted.hasValidPowerUp(gameManager));
        assertEquals(0, gameManager.getCurrentTurn().getCounterAttackingPlayers().size());
        assertEquals(0, player.getPossibleWeaponsToReload().size());
        assertEquals(TurnState.WAIT_FOR_OTHER_PLAYERS_TURN, player.getTurnState());
        for (Player p : gameManager.getPlayerOrderTurn())
            if (p.getPlayerColor() == PlayerColor.PURPLE)
                enemyTargeted = p;
        player = gameManager.getCurrentPlayerTurn();
        player.chooseTileMovementAction(gameManager);
        player.movementActionAfterTile(gameManager, gameManager.getTiles().get(1).get(3));
        player.chooseTileGrabAction(gameManager);
        player.grabActionAfterTile(gameManager,player.getCurrentTile());
        player=gameManager.getCurrentPlayerTurn();
        player.getWeapons().get(0).setLoaded(LoadedState.LOADED);
        player.chooseTileMovementAction(gameManager);
        player.movementActionAfterTile(gameManager,gameManager.getTiles().get(0).get(1));
        gameManager.getCurrentTurn().setNOfActionMade(0);
        player.chooseTileMovementAction(gameManager);
        player.movementActionAfterTile(gameManager,gameManager.getTiles().get(1).get(0));
        player.chooseTileShootingAction(gameManager);
        player.shootingActionAfterTile(player.getCurrentTile(),gameManager);
        for(Weapon weapon:player.getWeapons())
            if(weapon.getIdName().equals("RAILGUN")){
                w=weapon;
                break;}
        assertEquals("RAILGUN",w.getIdName());
        player.startShootingWithAWeapon(gameManager,w);
        assertEquals(TurnState.CHOOSE_TYPE_OF_EFFECT,player.getTurnState());
        w.baseEffectExecute(gameManager,player);
        assertEquals(TurnState.CHOOSE_DIRECTION,player.getTurnState());
        assertEquals(2,gameManager.getCurrentTurn().getPossibleChoices().getSelectableDirections().size());
        gameManager.getCurrentTurn().topActions().get(0).afterChooseDirectionExecute(gameManager.getCurrentTurn().topEffect(),gameManager,player, EAST);
        for (Player p : gameManager.getPlayerOrderTurn())
            if (p.getPlayerColor() == PlayerColor.PURPLE)
                enemyTargeted = p;
        player.setAmmo(new Ammo(2,2,2));
        assertTrue(gameManager.getCurrentTurn().getPossibleChoices().getSelectableTargets().contains(enemyTargeted));
        assertEquals(1,gameManager.getCurrentTurn().getPossibleChoices().getSelectableTargets().size());
        gameManager.getCurrentTurn().topActions().get(0).afterChooseTargetExecute(gameManager.getCurrentTurn().topEffect(),gameManager,player, enemyTargeted);
        System.out.println(""+player.getAmmo().getBlueValue()+""+player.getAmmo().getYellowValue()+""+player.getAmmo().getRedValue());
        player.reloadWeapon(gameManager,player.getWeapons().get(0));
        TurnStateHandler.handleAfterReload(gameManager,player);
        assertEquals(LoadedState.LOADED,player.getWeapons().get(0).getLoaded());
        assertEquals(TurnState.WAIT_FOR_OTHER_PLAYERS_TURN,player.getTurnState());
        MapInfoView viewMap = MapInfo.createMapView(map);
        MapDrawing.drawMap(viewMap,killShotTrack);

    }

    private void fillPowerUpDeck() {
        PowerUp powerUp= new PowerUp(AmmoColor.BLUE,PowerUpUse.AFTER_TAKEN_DAMAGE,"Granata_venom",new PowerUpEffect(new Ammo(0,0,0),0,0,0,1,false,false,false));
        PowerUp powerUp1= new PowerUp(AmmoColor.BLUE,PowerUpUse.AFTER_TAKEN_DAMAGE,"Granata_venom",new PowerUpEffect(new Ammo(0,0,0),0,0,0,1,false,false,false));
        gameManager.getPowerUpDeck().removeAll();
        gameManager.getPowerUpDeck().removeDiscardPile();
        gameManager.getPowerUpDeck().addToDiscardPile(powerUp);
        gameManager.getPowerUpDeck().addToDiscardPile(powerUp1);
        gameManager.getPowerUpDeck().reShuffleAll();
    }

    private void fillYellowAmmoTile(Tile tile){
        tile.removeAmmo();
        tile.setAmmo(new AmmoTile( new Ammo(0,0,3),0));
    }

    private void fillBlueAmmoTile(Tile tile){
        tile.removeAmmo();
        tile.setAmmo(new AmmoTile( new Ammo(0,3,0),0));
    }

    private void fillRedAmmoTile(Tile tile){
        tile.removeAmmo();
        tile.setAmmo(new AmmoTile( new Ammo(3,0,0),0));
    }


    @Test
    private void fillBlueSpawnWeapon(){
        Weapon w;
        List<Tile> spawnTiles=new ArrayList<>();
         while(true) {
            w = gameManager.getWeaponsDeck().draw();
            if (w.getIdName().equals("ROCKET_LAUNCHER"))
                break;
            gameManager.getWeaponsDeck().addToDiscardPile(w);
            gameManager.getWeaponsDeck().reShuffleAll();
        }
        spawnTiles= gameManager.getSpawnTiles();

        for(Tile t:spawnTiles)
            if(t.getRoom()== RoomColor.BLUE)
                t.addWeapon(w);
        spawnTiles.clear();
        while (true) {
            w = gameManager.getWeaponsDeck().draw();
            if (w.getIdName().equals("RAILGUN"))
                break;
            gameManager.getWeaponsDeck().addToDiscardPile(w);
            gameManager.getWeaponsDeck().reShuffleAll();
        }
        spawnTiles = gameManager.getSpawnTiles();
        for (Tile t : spawnTiles)
            if (t.getRoom() == RoomColor.BLUE)
                t.addWeapon(w);

        while (true) {
            w = gameManager.getWeaponsDeck().draw();
           if (w.getIdName().equals("GRENADE_LAUNCHER"))
                break;
            gameManager.getWeaponsDeck().addToDiscardPile(w);
            gameManager.getWeaponsDeck().reShuffleAll();
        }
        spawnTiles.clear();
        spawnTiles = gameManager.getSpawnTiles();
        for (Tile t : spawnTiles)
            if (t.getRoom() == RoomColor.BLUE)
                t.addWeapon(w);
    }
}
