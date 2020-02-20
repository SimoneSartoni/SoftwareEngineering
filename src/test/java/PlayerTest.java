import controller.TurnStateController;
import model.board.*;
import model.enums.*;
import model.exceptions.GameException;
import model.exceptions.MovementException;
import model.exceptions.ShootingException;
import model.exceptions.WeaponsException;
import model.gamemodes.BoardStructure;
import model.gamemodes.Gamemodes;
import model.player.Player;
import model.powerup.PowerUp;
import model.utility.Ammo;
import model.utility.Visibility;
import model.weapon.Effect;
import model.weapon.InitWeapons;
import model.weapon.Weapon;
import model.weapon.actions.DamageAction;
import network.ViewProxy;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {
    private static GameManager gameManager;
    private static List<Player> players;
    private static KillShotTrack killShotTrack;
    private static TurnStateController turnStateController;
    private static void setTiles(GameManager gameManager) {
        gameManager.getPlayerOrderTurn().get(0).setCurrentTile(gameManager.getTiles().get(1).get(1));
        gameManager.getTiles().get(1).get(1).addPlayer(gameManager.getPlayerOrderTurn().get(0));
        gameManager.getPlayerOrderTurn().get(1).setCurrentTile(gameManager.getTiles().get(0).get(1));
        gameManager.getTiles().get(0).get(1).addPlayer(gameManager.getPlayerOrderTurn().get(1));
        gameManager.getPlayerOrderTurn().get(2).setCurrentTile(gameManager.getTiles().get(1).get(2));
        gameManager.getTiles().get(1).get(2).addPlayer(gameManager.getPlayerOrderTurn().get(2));
        gameManager.getPlayerOrderTurn().get(3).setCurrentTile(gameManager.getTiles().get(1).get(0));
        gameManager.getTiles().get(1).get(0).addPlayer(gameManager.getPlayerOrderTurn().get(3));
        gameManager.getPlayerOrderTurn().get(4).setCurrentTile(gameManager.getTiles().get(1).get(2));
        gameManager.getTiles().get(1).get(2).addPlayer(gameManager.getPlayerOrderTurn().get(4));
    }

    @BeforeAll
    static void before() {
        final String fileToParse = "fileToParse/";
        final String powFile = fileToParse + "powerUpToParse.txt";
        final String ammoFile = fileToParse + "ammoTilesToParse.txt";
        final String mapFile = fileToParse + "map.txt";
        final String pointsFile = fileToParse + "points.txt";
        final String weaponFile = fileToParse + "weaponsToParse.txt";
        final String commandsFile = fileToParse + "state_commands.txt";
        final Integer [] scores = {8, 6, 4, 2, 1, 1};
        Gamemodes gamemodes = new Gamemodes(new BoardStructure(pointsFile));
        players= new ArrayList<>();
        players.add(new Player("a", PlayerColor.GREEN, PlayerState.NORMAL, null, null));
        players.add(new Player("b", PlayerColor.GREY,PlayerState.NORMAL, null, null));
        players.add(new Player("c", PlayerColor.BLUE,PlayerState.NORMAL, null, null));
        players.add(new Player("d", PlayerColor.PURPLE,PlayerState.NORMAL, null, null));
        players.add(new Player("e",PlayerColor.YELLOW,PlayerState.NORMAL, null, null));
        players.get(4).setCurrentTurnAction(null);
        assertEquals(5,players.size());

        killShotTrack = new KillShotTrack(Arrays.asList(scores), 8, new ArrayList<>(), 1);

        gameManager = new GameManager(new Deck<Weapon>(InitWeapons.initAllWeapons(weaponFile)), new Deck<PowerUp>(InitMap.initPowerUps(powFile)),
                new Deck<AmmoTile>(InitMap.initAmmoTiles(ammoFile)), players, InitMap.initMap(mapFile).get(0).getMap(),
                gamemodes.getNormalMode(), killShotTrack, false, gamemodes,
                InitMap.initMap(mapFile).get(0).getMapWidth(), InitMap.initMap(mapFile).get(0).getMapHeight());
        gameManager.getCurrentGameMode().getEndObserver().setGameManager(gameManager);
        setTiles(gameManager);
        Integer [] arrPoints = {8, 6, 4, 2, 1, 1};
        List<Integer> points=new ArrayList<>(Arrays.asList(arrPoints));
        PointsBoard pointsBoard=new PointsBoard(points,1);

        gameManager.fillAllAmmoTiles();
        turnStateController= new TurnStateController(commandsFile);
        for(Player player : gameManager.getPlayerOrderTurn()) {
            player.setBoard(pointsBoard);
            player.setCurrentTurnAction(gameManager.getCurrentGameMode().getBaseTurnAction());
            player.setViewPlayer(new ViewProxy(new ViewMock(), "NOT INIT",turnStateController));
        }
        initializationTest();
    }

    @Test
    private static void initializationTest() {
        for(Player player : gameManager.getPlayerOrderTurn()) {
            assertEquals(0, player.getNOfKills());
            assertEquals(0, player.getNOfDeaths());
            assertTrue(player.getMarks().isEmpty());
            assertEquals(2, player.getCurrentTurnAction().getMaxNOfActions());
            assertEquals(3, player.getCurrentTurnAction().getOnlyMovement());
            assertEquals(1, player.getCurrentTurnAction().getMovementBeforeGrabbing());
            assertEquals(0, player.getCurrentTurnAction().getMovementBeforeShooting());
            assertFalse(player.getCurrentTurnAction().isReloadBeforeShooting());
        }
        //dummy Tests
        Player dummyPlayer = new Player("Dummy", PlayerColor.GREEN, PlayerState.NORMAL, null, null);
        dummyPlayer.addNOfKills();
        assertEquals(1, dummyPlayer.getNOfKills());
        dummyPlayer.setNOfKills(0);
        assertEquals(0, dummyPlayer.getNOfKills());
        dummyPlayer.addNOfDeaths();
        assertEquals(1, dummyPlayer.getNOfDeaths());
    }

    @Test
    void damageTest() {
        for(Player player : gameManager.getPlayerOrderTurn()) {
            assertEquals(0, player.getDamageTaken().size());
        }
        Player enemy1 = gameManager.getPlayerOrderTurn().get(1);
        Player enemy2 = gameManager.getPlayerOrderTurn().get(2);
        Player player = gameManager.getPlayerOrderTurn().get(0);
        assertEquals(PlayerState.NORMAL, player.getPlayerState());
        player.addDamageTaken(enemy1, 3,gameManager);
        player.addDamageTaken(enemy2, 2,gameManager);
        player.addDamageTaken(enemy2, 2,gameManager);
        assertEquals(7, player.getDamageTaken().size());
        player.addDamageTaken(enemy1,4,gameManager);
        assertEquals(11, player.getDamageTaken().size());
        assertEquals(PlayerState.DEAD, player.getPlayerState());
        player.addDamageTaken(enemy1, 5,gameManager);
        player.addDamageTaken(enemy2, 15,gameManager);
        assertEquals(12, player.getDamageTaken().size());
        assertEquals(PlayerState.DEAD, player.getPlayerState());

        gameManager.getCurrentGameMode().countPlayerPoints(gameManager.getCurrentPlayerTurn(),player.getDamageTaken(),player.getBoard(),player.getNOfDeaths());
        assertEquals(9, enemy1.getScore());
        assertEquals(6, enemy2.getScore());
        enemy1.resetDamageTaken();
        enemy1.addDamageTaken(player, 2,gameManager);
        assertEquals(3, enemy1.getDamageTaken().size());
        assertEquals(0, enemy1.getMarks().get(player));
        enemy1.addMarks(3, player,gameManager);
        enemy1.addDamageTaken(player, 5,gameManager);
        assertEquals(11, enemy1.getDamageTaken().size());
        assertEquals(0, enemy1.getMarks().get(player));
        enemy1.addMarks(3, player,gameManager);
        enemy1.addDamageTaken(player, 0,gameManager);
        assertEquals(12, enemy1.getDamageTaken().size());
        assertEquals(0, enemy1.getMarks().get(player));
        assertTrue(player.getMarks().containsKey(enemy1));

        player.getMarks().replace(enemy1, 0);
    }

    @Test
    void markTest() {
        Player targetted = gameManager.getPlayerOrderTurn().get(0);
        Player targeter1 = gameManager.getPlayerOrderTurn().get(1);
        Player targeter2 = gameManager.getPlayerOrderTurn().get(2);

        targetted.addMarks(1, targeter1,gameManager);
        assertEquals(1, targetted.getMarks().size());
        assertEquals(1, targetted.getMarks().get(targeter1));
        targetted.addMarks(5, targeter2,gameManager);
        assertEquals(2, targetted.getMarks().size());
        assertEquals(3, targetted.getMarks().get(targeter2));
        targetted.addMarks(1, targeter1,gameManager);
        targetted.addMarks(1, targeter2,gameManager);
        assertEquals(2, targetted.getMarks().size());
        assertEquals(2, targetted.getMarks().get(targeter1));
        assertEquals(3, targetted.getMarks().get(targeter2));
    }

    @Test
    void ammoGainedTest() {
        Player placeholder = new Player("PlaceHolder", PlayerColor.GREEN, PlayerState.NORMAL, null, null);
        Ammo invalidAmmo = new Ammo(+1,-1,1);
        Ammo ammo = new Ammo(1,1,1);
        assertEquals(ammo, placeholder.getAmmo());
        Ammo ammoGained = new Ammo(3, 1, 3);
        ammo = new Ammo(3,2,3);
        placeholder.addAmmo(ammoGained);
        assertEquals(ammo, placeholder.getAmmo());
        placeholder.addAmmo(ammoGained);
        ammo = new Ammo(3,3,3);
        assertEquals(ammo, placeholder.getAmmo());
        placeholder.addAmmo(new Ammo(3,3,3));
        assertEquals(ammo, placeholder.getAmmo());
    }

    @Test
    private void isValidPowerUpTest() {
        Player player = new Player("PlaceholderPlayer", PlayerColor.GREEN, PlayerState.NORMAL, null, null);
        List<PowerUp> powerUps = new ArrayList<>();
        int dim = gameManager.getPowerUpDeck().pileSize();
        for(int i = 0; i < dim; i++) {
            powerUps.add(gameManager.getPowerUpDeck().draw());
        }
        PowerUp placeholder1 = powerUps.get(5);
        PowerUp placeholder2 = powerUps.get(0);
        PowerUp placeholder3 = powerUps.get(11);
        assertFalse(placeholder1.isValid(gameManager, player));
        player.drawPowerUp(gameManager,placeholder1);
        player.drawPowerUp(gameManager,placeholder2);
        player.drawPowerUp(gameManager,placeholder3);
        player.setTurnState(TurnState.READY_FOR_ACTION);
        assertTrue(placeholder1.isValid(gameManager, player));
        assertFalse(placeholder2.isValid(gameManager, player));
        player.setTurnState(TurnState.CHOOSE_AFTER_DAMAGE_POWERUP);
        assertTrue(placeholder2.isValid(gameManager, player));
        player.setTurnState(TurnState.WAIT_FOR_OTHER_PLAYERS_TURN);
        assertTrue(placeholder3.isValid(gameManager, player));

        //dummy Tests
        assertEquals(PowerUpUse.SEPARATED, placeholder1.getTypeOfUse());
        assertEquals(AmmoColor.RED, placeholder1.getColor());
        assertEquals("Raggio_cinetico", placeholder1.getName());

        gameManager.getPowerUpDeck().addToPile(powerUps);
    }

    @Test
    void isValidShootTest() {
        Tile faketile;
        Player placeholderPlayer = new Player("Carmelo", PlayerColor.YELLOW, PlayerState.NORMAL, gameManager.getTiles().get(2).get(3), gameManager.getCurrentGameMode().getBaseTurnAction());
        placeholderPlayer.setViewPlayer(new ViewProxy(new ViewMock(), "ciao",turnStateController));
        gameManager.getCurrentTurn().setCurrentPlayerForVisibility(placeholderPlayer);
        gameManager.setCurrentPlayer(placeholderPlayer);

        assertTrue(placeholderPlayer.getValidWeapons(gameManager).isEmpty());
        assertFalse(placeholderPlayer.isValidShoot(gameManager));
        //take the "Distruttore"
        Weapon distruttore = gameManager.getWeaponsDeck().draw();
        placeholderPlayer.setAmmo(new Ammo(3,3,3));
        placeholderPlayer.pickWeapon(distruttore,gameManager);
        //too distance, can see no one
        assertFalse(placeholderPlayer.isValidShoot(gameManager));
        //take the "Razzo Termico"
        Weapon razzoTermicoPlaceHolder = new Weapon(0, new Ammo(1,0,1), new Ammo(1,0,0), "Razzo termico", LoadedState.PARTIALLY_LOADED);
        razzoTermicoPlaceHolder.setBaseEffect(new Effect
                ("effetto_base", false,
                        Collections.singletonList(new DamageAction(3,3,false, 0,0,null, null, null,false)),
                        true, 0, 0, null, new Visibility(false, true, false,
                        false, false, false, false,false,false, 2, 100, 0, 0),
                        new Ammo(0,0,0), -1, false, false,false)
        );
        razzoTermicoPlaceHolder.getBaseEffect().setCanBeUsed(true);
        placeholderPlayer.setAmmo(new Ammo(3,3,3));
        placeholderPlayer.pickWeapon(razzoTermicoPlaceHolder,gameManager);
        //With the new "Razzo termico" you should be abel to hit someone
        gameManager.getCurrentTurn().getPossibleChoices().setSelectableWeapons(Collections.singletonList(placeholderPlayer.getWeapons().get(1)));
        assertTrue(placeholderPlayer.isValidShoot(gameManager));
        try{
        Weapon fakeWeapon=placeholderPlayer.isAValidWeaponToUse(gameManager, placeholderPlayer.getWeapons().get(1));
        }
        catch(GameException e){
            assertTrue(false);
        }
        //Without "razzo termico" but nearest the enemy you should not be able to hit;
        placeholderPlayer.removeWeapon(0);
        placeholderPlayer.setCurrentTile(gameManager.getTiles().get(0).get(0));
        assertFalse(placeholderPlayer.isValidShoot(gameManager));
        //In this position, with only the "Razzo termico" you should not be able to hit
        placeholderPlayer.removeWeapon(0);
        gameManager.getWeaponsDeck().addToDiscardPile(distruttore);
        placeholderPlayer.setAmmo(new Ammo(3,3,3));
        placeholderPlayer.pickWeapon(razzoTermicoPlaceHolder,gameManager);
        assertFalse(placeholderPlayer.isValidShoot(gameManager));

        placeholderPlayer.setCurrentTile(gameManager.getTiles().get(0).get(0));
        try {
            faketile=placeholderPlayer.isAValidShootingAction(gameManager, null);
            assertTrue(false);
        } catch (ShootingException e){

        }
        //TODO
        //understand is a valid shooting action


        placeholderPlayer.shootingActionAfterTile(gameManager.getTiles().get(1).get(0),gameManager);
        assertEquals(TurnState.CHOOSE_WEAPON_HAND, placeholderPlayer.getTurnState());
        TurnAction placheholderTurnAction = new TurnAction(2,1,true, 3, 1);
        placeholderPlayer.setCurrentTurnAction(placheholderTurnAction);
        placeholderPlayer.shootingActionAfterTile(placeholderPlayer.getCurrentTile(),gameManager);
        assertEquals(TurnState.CHOOSE_WEAPON_HAND, placeholderPlayer.getTurnState());
        placeholderPlayer.setCurrentTurnAction(gameManager.getCurrentGameMode().getBaseTurnAction());
        //assertFalse(placeholderPlayer.getValidWeapons(gameManager).isEmpty());
        //assertTrue(placeholderPlayer.getValidWeapons(gameManager).contains(placeholderPlayer.getWeapons().get(0)));
    }

    @Test
    void isValidMovementTest() {
        Player placeholderPlayer = new Player("Placeholder", PlayerColor.YELLOW, PlayerState.NORMAL, gameManager.getTiles().get(0).get(0), gameManager.getCurrentGameMode().getBaseTurnAction());
        try { Tile t=placeholderPlayer.isAValidMovementAction(gameManager, GameManager.createTileView(gameManager.getTiles().get(1).get(2))); }
        catch(MovementException movementException){}
        try {
            Tile tile=placeholderPlayer.isAValidMovementAction(gameManager, GameManager.createTileView(gameManager.getTiles().get(2).get(3)));
            assertTrue(false);}
        catch(MovementException movementException){};
        try {
            Tile tile=placeholderPlayer.isAValidMovementAction(gameManager, GameManager.createTileView(placeholderPlayer.getCurrentTile()));
            assertTrue(false); }
        catch(MovementException movementException){};

    }

    @Test
    void powerUpsTest() {
        Player placeholderPlayer = new Player("Carmelo", PlayerColor.YELLOW, PlayerState.NORMAL, gameManager.getTiles().get(2).get(3), gameManager.getCurrentGameMode().getBaseTurnAction());
        placeholderPlayer.setTurnState(TurnState.READY_FOR_ACTION);
        placeholderPlayer.setViewPlayer(new ViewProxy(new ViewMock(), "ciao",turnStateController));
        List<PowerUp> powerUps = new ArrayList<>();
        for(int i = 0; i < 24; i++) {
            powerUps.add(gameManager.getPowerUpDeck().draw());
        }
        PowerUp pow1 = powerUps.get(12);
        PowerUp pow2 = powerUps.get(6);
        placeholderPlayer.drawPowerUp(gameManager,pow1);
        placeholderPlayer.drawPowerUp(gameManager,pow2);
        assertTrue(placeholderPlayer.hasValidPowerUp(gameManager));
        placeholderPlayer.afterChosenPowerUp(gameManager,placeholderPlayer.getPowerUps().get(1));
        assertFalse(placeholderPlayer.hasValidPowerUp(gameManager));

        gameManager.getPowerUpDeck().addToPile(powerUps);
    }

    @Test
    void otherWeaponsTest() {
        Weapon fakeWeapon;
        Player placeholderPlayer = new Player("Carmelo", PlayerColor.YELLOW, PlayerState.NORMAL, gameManager.getTiles().get(2).get(3), gameManager.getCurrentGameMode().getBaseTurnAction());
        placeholderPlayer.setViewPlayer(new ViewProxy(new ViewMock(), "not init",turnStateController));
        Visibility visibility = new Visibility(true, false, false, false, false, false, false, false,false, 0, 1, 0, 1);
        Effect fakeEffect = new Effect("fake", false, new ArrayList<>(), true, 0, 0, null, visibility, new Ammo(1,0,1), 0, false, false,false);
        Weapon weapon1 = new Weapon(fakeEffect, null, new ArrayList<>(), 0, new Ammo(1,0,0), new Ammo(1,0,0), "Weapon1", LoadedState.PARTIALLY_LOADED);
        Weapon weapon2 = new Weapon(fakeEffect, null, new ArrayList<>(), 0, new Ammo(0,1,0), new Ammo(0,1,0), "Weapon2", LoadedState.PARTIALLY_LOADED);
        Weapon weapon3 = new Weapon(fakeEffect, null, new ArrayList<>(), 0, new Ammo(0,0,1), new Ammo(0,0,1), "Weapon3", LoadedState.PARTIALLY_LOADED);
        Weapon invalidWeapon = new Weapon(null, null, new ArrayList<>(), 0, new Ammo(1,1,1), new Ammo(1,0,0), "InvalidWeapon", LoadedState.PARTIALLY_LOADED);
        placeholderPlayer.setAmmo(new Ammo(3,3,3));
        placeholderPlayer.pickWeapon(weapon1,gameManager);
        placeholderPlayer.pickWeapon(weapon2,gameManager);
        placeholderPlayer.pickWeapon(weapon3,gameManager);
        try { fakeWeapon=placeholderPlayer.isAValidWeaponToDrop(gameManager,weapon1); }
        catch(WeaponsException weaponsException){ }
        try {
            fakeWeapon=placeholderPlayer.isAValidWeaponToDrop(gameManager,invalidWeapon);
            assertTrue(false);
        }
        catch(WeaponsException weaponsException){}
        assertTrue(placeholderPlayer.getPossibleWeaponsToReload().isEmpty());
        assertEquals(3, placeholderPlayer.getWeapons().size());
        placeholderPlayer.dropAllWeapons();
        assertTrue(placeholderPlayer.getWeapons().isEmpty());
        assertTrue(placeholderPlayer.getPossibleWeaponsToReload().isEmpty());
        placeholderPlayer.reloadWeapon(gameManager,invalidWeapon);
        assertEquals(LoadedState.LOADED, invalidWeapon.getLoaded());

        placeholderPlayer.setAmmo(new Ammo(3,3,3));
        placeholderPlayer.pickWeapon(weapon1,gameManager);
        placeholderPlayer.setAmmo(new Ammo(3,3,3));
        //placeholderPlayer.swapWeapon(weapon2,gameManager);
        //assertEquals(LoadedState.PARTIALLY_LOADED, weapon1.getLoaded());
        assertEquals(LoadedState.LOADED, weapon2.getLoaded());
    }
}
