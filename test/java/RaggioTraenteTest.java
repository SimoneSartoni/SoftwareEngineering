import controller.TurnStateController;
import model.board.*;
import model.enums.*;
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
import view.cli.MapDrawing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RaggioTraenteTest {

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
        final Player[] arrayPlayers = {new Player("Yellow", PlayerColor.YELLOW, PlayerState.NORMAL, null, null),
                new Player("Purple", PlayerColor.PURPLE, PlayerState.NORMAL, null, null),
                new Player("Green", PlayerColor.GREEN, PlayerState.NORMAL, null, null),
                new Player("Blue",PlayerColor.BLUE,PlayerState.NORMAL,null,null)};
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
        PowerUp powerUp= new PowerUp(AmmoColor.BLUE, PowerUpUse.SEPARATED,"Teletrasporto",new PowerUpEffect(new Ammo(0,0,0),1,0,100,0,false,false,false));
        PowerUp powerUp1= new PowerUp(AmmoColor.BLUE,PowerUpUse.SEPARATED,"Teletrasporto",new PowerUpEffect(new Ammo(0,0,0),1,0,100,0,false,false,false));
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
    void tryWeapon(){
        fillSetUp();
        assertEquals(2, gameManager.getPowerUpDeck().pileSize());
        Collections.shuffle(gameManager.getPlayerOrderTurn());
        gameManager.setCurrentPlayer(gameManager.getPlayerOrderTurn().get(0));
        for (Player p : gameManager.getPlayerOrderTurn()) {
            if (!p.equals(gameManager.getCurrentPlayerTurn())) {
                p.setTurnState(TurnState.READY_TO_SPAWN);
            }
        }
        Player enemy1=gameManager.getPlayerOrderTurn().get(1);
        Player enemy2=gameManager.getPlayerOrderTurn().get(2);
        gameManager.getPlayerOrderTurn().get(1).setCurrentTile(gameManager.getTiles().get(0).get(0));
        gameManager.getTiles().get(0).get(0).addPlayer(gameManager.getPlayerOrderTurn().get(1));
        gameManager.getTiles().get(2).get(3).addPlayer(gameManager.getPlayerOrderTurn().get(2));
        gameManager.getPlayerOrderTurn().get(2).setCurrentTile(gameManager.getTiles().get(2).get(3));
        Player player = gameManager.getCurrentPlayerTurn();
        player.setCurrentTile(gameManager.getTiles().get(1).get(0));
        gameManager.getTiles().get(1).get(0).addPlayer(gameManager.getCurrentPlayerTurn());
        player.setAmmo(new Ammo(3,3,3));
        player.getWeapons().add(gameManager.getTiles().get(1).get(0).getWeapons().get(1));
        Weapon w=player.getWeapons().get(0);
        MapDrawing.drawMap(MapInfo.createMapView(map),killShotTrack);
        player.startShootingWithAWeapon(gameManager,w);
        assertEquals(TurnState.CHOOSE_TYPE_OF_EFFECT,player.getTurnState());
        w.baseEffectExecute(gameManager,player);
        assertEquals(TurnState.CHOOSE_TARGET,player.getTurnState());
        assertEquals(2,gameManager.getCurrentTurn().getPossibleChoices().getSelectableTargets().size());
        assertTrue(gameManager.getCurrentTurn().getPossibleChoices().getSelectableTargets().contains(enemy1));
        assertTrue(gameManager.getCurrentTurn().getPossibleChoices().getSelectableTargets().contains(enemy2));

    }
}
