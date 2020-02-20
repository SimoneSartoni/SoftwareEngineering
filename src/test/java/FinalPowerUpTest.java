import controller.TurnStateController;
import model.board.*;
import model.enums.*;
import model.exceptions.GameException;
import model.gamemodes.BoardStructure;
import model.gamemodes.Gamemodes;
import model.player.Player;
import model.powerup.PowerUp;
import model.powerup.PowerUpEffect;
import model.utility.Ammo;
import model.utility.MapInfo;
import model.weapon.InitWeapons;
import model.weapon.Weapon;
import network.ViewProxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FinalPowerUpTest {
    private GameManager gameManager;
    private static TurnStateController turnStateController;

    final String fileToParse = "fileToParse/";
    final String powFile = fileToParse + "powerUpToParse.txt";
    final String ammoFile = fileToParse + "ammoTilesToParse.txt";
    final String mapFile = fileToParse + "map.txt";
    final String pointsFile = fileToParse + "points.txt";
    final String weaponFile = fileToParse + "weaponsToParse.txt";
    final String turnStateFile = fileToParse + "state_commands.txt";
    private MapInfo map;
    private KillShotTrack killShotTrack;


    @BeforeEach
    void before() {
        final Player[] arrayPlayers = {new Player("Yellow", PlayerColor.YELLOW, PlayerState.NORMAL, null, null),
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
        killShotTrack = new KillShotTrack(Arrays.asList(scores), 1, new ArrayList<>(), 1);
        Gamemodes gamemodes = new Gamemodes(new BoardStructure(pointsFile));
        gameManager = new GameManager(new Deck<Weapon>(InitWeapons.initAllWeapons(weaponFile)), new Deck<PowerUp>(InitMap.initPowerUps(powFile)),
                new Deck<AmmoTile>(InitMap.initAmmoTiles(ammoFile)), players, allMaps.get(0).getMap(), gamemodes.getNormalMode(), killShotTrack,
                true, gamemodes, InitMap.initMap(mapFile).get(0).getMapWidth(), InitMap.initMap(mapFile).get(0).getMapHeight());
        for(Player p : gameManager.getPlayerOrderTurn()) {
            p.setCurrentTurnAction(gameManager.getCurrentGameMode().getBaseTurnAction());
        }
        gameManager.setNotifyObservers(views);
        gamemodes.getNormalMode().getEndObserver().setGameManager(gameManager);
        gamemodes.getFinalFrenzyMode().getEndObserver().setGameManager(gameManager);
        for(Player p : gameManager.getPlayerOrderTurn()) {
            p.setCurrentTurnAction(gameManager.getCurrentGameMode().getBaseTurnAction());
            p.setBoard(new PointsBoard(Arrays.asList(scores),1));
        }
        turnStateController = new TurnStateController(turnStateFile);
        for(Player p:players) {
            p.setViewPlayer(new ViewProxy(new ViewMock(), "ciao",turnStateController));
            views.add(p.getViewPlayer());
        }
    }


    private void fillSetUp(){
        gameManager.fillAllAmmoTiles();
        Weapon w;
        List<Tile> spawnTiles=new ArrayList<>();
        assertEquals(21,gameManager.getWeaponsDeck().pileSize());
        while(true) {
            w = gameManager.getWeaponsDeck().draw();
            if (w.getIdName().equals("VORTEX_CANNON"))
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
            if (w.getIdName().equals("TRACTOR_BEAM"))
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
            if (w.getIdName().equals("ROCKET_LAUNCHER"))
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
        PowerUp powerUp= new PowerUp(AmmoColor.RED, PowerUpUse.SEPARATED,"TELEPORTER",new PowerUpEffect(new Ammo(0,0,0),0,1,100,0,false,false,false));
        PowerUp powerUp1= new PowerUp(AmmoColor.RED,PowerUpUse.SEPARATED,"TELEPORTER",new PowerUpEffect(new Ammo(0,0,0),0,1,100,0,false,false,false));
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
    public void testTeleporter(){
        fillSetUp();
        Collections.shuffle(gameManager.getPlayerOrderTurn());
        gameManager.setCurrentPlayer(gameManager.getPlayerOrderTurn().get(0));
        for (Player p : gameManager.getPlayerOrderTurn()) {
            if (!p.equals(gameManager.getCurrentPlayerTurn())) {
                p.setTurnState(TurnState.READY_TO_SPAWN);
            }
        }
        Player p=gameManager.getCurrentPlayerTurn();
        Player enemy1=gameManager.getPlayerOrderTurn().get(1);
        Player enemy2=gameManager.getPlayerOrderTurn().get(2);
        enemy2.setCurrentTile(gameManager.getTiles().get(0).get(2));
        gameManager.getTiles().get(0).get(2).addPlayer(enemy2);
        enemy1.setCurrentTile(gameManager.getTiles().get(0).get(1));
        gameManager.getTiles().get(0).get(1).addPlayer(enemy1);
        gameManager.spawnDrawPhase(p,2);
        assertEquals(2,p.getPowerUps().size());
        assertEquals("TELEPORTER",p.getPowerUps().get(0).getName());
        gameManager.spawnSetPosition(p,p.getPowerUps().get(0));
        assertEquals(RoomColor.RED,p.getCurrentTile().getRoom());
        assertEquals(TurnState.READY_FOR_ACTION,p.getTurnState());
        try {
            turnStateController.handleActions(gameManager, TypeOfAction.POWER_UP, p);
        }
        catch(GameException e){
            assertTrue(false);

        };
        assertEquals(TurnState.CHOOSE_POWERUP,p.getTurnState());
        try {
            turnStateController.handlePowerUp(p.getPowerUps().get(0), p,gameManager);
        }
        catch(GameException e){
            assertTrue(false);
        };
        assertEquals(TurnState.CHOOSE_POWERUP_TILE,p.getTurnState());
        try{
            turnStateController.handleChooseTile(GameManager.createTileView(gameManager.getTiles().get(0).get(2)),p,gameManager);
        }
        catch(GameException e){
            assertTrue(false);
        };
        assertEquals(TurnState.READY_FOR_ACTION,p.getTurnState());
        assertEquals(0,gameManager.getCurrentTurn().getNOfActionMade());
        try {
            turnStateController.handleActions(gameManager, TypeOfAction.GRAB, p);
        }
        catch(GameException e){
            assertTrue(false);

        };
        try {
            turnStateController.handleChooseTile(GameManager.createTileView(gameManager.getTiles().get(0).get(1)),p,gameManager);
        }
        catch(GameException e){
            assertTrue(false);

        };
        fillpowerUpDeck();
        try {
            turnStateController.handleActions(gameManager, TypeOfAction.RUN, p);
        }
        catch(GameException e){
            assertTrue(false);

        };
        try {
            turnStateController.handleChooseTile(GameManager.createTileView(gameManager.getTiles().get(0).get(0)),p,gameManager);
        }
        catch(GameException e){
            assertTrue(false);

        };
        try {
            turnStateController.handlePowerUp(enemy1.getPowerUps().get(0), enemy1,gameManager);
        }
        catch(GameException e){
            assertTrue(false);
        };
        try {
            turnStateController.handleActions(gameManager, TypeOfAction.POWER_UP, enemy1);
        }
        catch(GameException e){
            assertTrue(false);

        };
        assertEquals(TurnState.CHOOSE_POWERUP,enemy1.getTurnState());
        try {
            turnStateController.handlePowerUp(enemy1.getPowerUps().get(0), enemy1,gameManager);
        }
        catch(GameException e){
            assertTrue(false);
        };
        assertEquals(gameManager.getTiles().get(1).get(0),enemy1.getCurrentTile());
        assertEquals(TurnState.CHOOSE_POWERUP_TARGET,enemy1.getTurnState());
        try{
            turnStateController.handleChooseTarget(p.createPlayerView(),enemy1,gameManager);
        }
        catch(GameException e){
            assertTrue(false);
        }
        try {
            turnStateController.handleChooseTile(GameManager.createTileView(gameManager.getTiles().get(1).get(0)),enemy1,gameManager);
        }
        catch(GameException e){
            assertTrue(false);
        };
        assertEquals(TurnState.READY_FOR_ACTION,enemy1.getTurnState());
        assertEquals(gameManager.getTiles().get(1).get(0),p.getCurrentTile());
    }

    private void fillpowerUpDeck() {
        PowerUp powerUp= new PowerUp(AmmoColor.RED, PowerUpUse.SEPARATED,"NEWTON",new PowerUpEffect(new Ammo(0,0,0),0,1,100,0,false,true,true));
        PowerUp powerUp1= new PowerUp(AmmoColor.RED,PowerUpUse.SEPARATED,"NEWTON",new PowerUpEffect(new Ammo(0,0,0),0,1,100,0,false,true,true));
        gameManager.getPowerUpDeck().removeAll();
        gameManager.getPowerUpDeck().removeDiscardPile();
        gameManager.getPowerUpDeck().addToDiscardPile(powerUp);
        gameManager.getPowerUpDeck().addToDiscardPile(powerUp1);
        gameManager.getPowerUpDeck().reShuffleAll();
    }


}
