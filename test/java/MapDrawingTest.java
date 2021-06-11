import model.board.*;
import model.enums.PlayerColor;
import model.enums.PlayerState;
import model.gamemodes.BoardStructure;
import model.gamemodes.Gamemodes;
import model.player.Player;
import model.powerup.PowerUp;
import model.utility.MapInfo;
import model.weapon.InitWeapons;
import model.weapon.Weapon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import view.cli.MapDrawing;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


class MapDrawingTest {

    private MapInfo map;
    private GameManager gameManager;
    private KillShotTrack killShotTrack;

    final String fileToParse = "fileToParse/";
    final String powFile = fileToParse + "powerUpToParse.txt";
    final String ammoFile = fileToParse + "ammoTilesToParse.txt";
    final String mapFile = fileToParse + "map.txt";
    final String pointsFile = fileToParse + "points.txt";
    final String weaponFile = fileToParse + "weaponsToParse.txt";

    @BeforeEach
    void before() {
        List<MapInfo> allMaps = InitMap.initMap(mapFile);
        map = new MapInfo(0, 0, "sudden death",
                allMaps.get(0).getMapWidth(), allMaps.get(0).getMapHeight(),
                allMaps.get(0).getMap(), allMaps.get(0).getAllowedPlayerColors(), allMaps.get(0).getAllowedEndModes(),
                allMaps.get(0).getMaxNumberOfPlayer(), allMaps.get(0).getMinNumberOfPlayer(), new ArrayList<>());
        final Player[] arrayPlayers = {new Player("Yellow", PlayerColor.YELLOW, PlayerState.NORMAL, null, null),
                new Player("Purple", PlayerColor.PURPLE, PlayerState.NORMAL, null, null),
                new Player("Green", PlayerColor.GREEN, PlayerState.NORMAL, null, null),
                new Player("Grey", PlayerColor.GREY, PlayerState.NORMAL, null, null),
                new Player("Blue", PlayerColor.BLUE, PlayerState.NORMAL, null, null)
        };
        final List<Player> players = new ArrayList<>(Arrays.asList(arrayPlayers));
        final Integer [] scores = {8, 6, 4, 2, 1, 1};
        final Integer [] frenzyScores = {2, 1, 1, 1};
        killShotTrack = new KillShotTrack(Arrays.asList(scores), 8,new ArrayList<>(), 1);
        Gamemodes gamemodes = new Gamemodes(new BoardStructure(pointsFile));
        gameManager = new GameManager(new Deck<Weapon>(InitWeapons.initAllWeapons(weaponFile)), new Deck<PowerUp>(InitMap.initPowerUps(powFile)),
                new Deck<AmmoTile>(InitMap.initAmmoTiles(ammoFile)), players, allMaps.get(0).getMap(), gamemodes.getNormalMode(), killShotTrack,
                false, gamemodes, allMaps.get(0).getMapWidth(), allMaps.get(0).getMapHeight());


        gameManager.fillAllAmmoTiles();
        System.out.println("Empty Map");
        //printEmptyMap
        MapDrawing.drawMap(MapInfo.createMapView(map),killShotTrack);
        System.out.println("-----------------------------------------------------------------");

        for(Player p : gameManager.getPlayerOrderTurn()) {
            p.setCurrentTurnAction(gameManager.getCurrentGameMode().getBaseTurnAction());
            p.setCurrentTile(gameManager.getTiles().get(0).get(0));
            gameManager.getTiles().get(0).get(0).addPlayer(p);
        }
    }

    @Test
    void drawMap() throws RemoteException {
        MapDrawing.drawMap(MapInfo.createMapView(map),killShotTrack);

        //moving players
        gameManager.getPlayerOrderTurn().get(0).setCurrentTile(gameManager.getTiles().get(0).get(1));
        gameManager.getTiles().get(0).get(1).addPlayer(gameManager.getPlayerOrderTurn().get(0));
        gameManager.getTiles().get(0).get(0).removePlayer(gameManager.getPlayerOrderTurn().get(0));

        System.out.println("------------------------------------------------------------------------");

        MapDrawing.drawMap(MapInfo.createMapView(map),killShotTrack);

        //moving players
        gameManager.getPlayerOrderTurn().get(1).setCurrentTile(gameManager.getTiles().get(2).get(3));
        gameManager.getTiles().get(2).get(3).addPlayer(gameManager.getPlayerOrderTurn().get(1));
        gameManager.getTiles().get(0).get(0).removePlayer(gameManager.getPlayerOrderTurn().get(1));

        System.out.println("------------------------------------------------------------------------");

        MapDrawing.drawMap(MapInfo.createMapView(map),killShotTrack);

        //moving players
        gameManager.getPlayerOrderTurn().get(1).setCurrentTile(gameManager.getTiles().get(0).get(2));
        gameManager.getTiles().get(0).get(2).addPlayer(gameManager.getPlayerOrderTurn().get(1));
        gameManager.getTiles().get(2).get(3).removePlayer(gameManager.getPlayerOrderTurn().get(1));

        System.out.println("------------------------------------------------------------------------");

        MapDrawing.drawMap(MapInfo.createMapView(map),killShotTrack);
    }
}
