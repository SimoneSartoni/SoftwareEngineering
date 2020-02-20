import controller.TurnStateController;
import model.board.*;
import model.enums.PlayerColor;
import model.enums.PlayerState;
import model.enums.TurnState;
import model.gamemodes.BoardStructure;
import model.gamemodes.Gamemodes;
import model.player.Player;
import model.utility.MapInfo;
import model.weapon.InitWeapons;
import network.ViewProxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DeathTest {
    private GameManager gameManager;
    final String fileToParse = "fileToParse/";
    final String powFile = fileToParse + "powerUpToParse.txt";
    final String ammoFile = fileToParse + "ammoTilesToParse.txt";
    final String mapFile = fileToParse + "map.txt";
    final String pointsFile = fileToParse + "points.txt";
    final String weaponFile = fileToParse + "weaponsToParse.txt";
    private MapInfo map;
    static KillShotTrack killShotTrack;
    final String commandsFile = fileToParse + "state_commands.txt";
    private TurnStateController turnStateController;

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
        killShotTrack = new KillShotTrack(Arrays.asList(scores), 8, new ArrayList<>(), 1);
        Gamemodes gamemodes = new Gamemodes(new BoardStructure(pointsFile));
        gamemodes.getNormalMode().getEndObserver().setGameManager(gameManager);
        gamemodes.getFinalFrenzyMode().getEndObserver().setGameManager(gameManager);
        gameManager = new GameManager(new Deck<>(InitWeapons.initAllWeapons(weaponFile)), new Deck<>(InitMap.initPowerUps(powFile)),
                new Deck<>(InitMap.initAmmoTiles(ammoFile)), players, allMaps.get(0).getMap(), gamemodes.getNormalMode(), killShotTrack,
                false, gamemodes, InitMap.initMap(mapFile).get(0).getMapWidth(), InitMap.initMap(mapFile).get(0).getMapHeight());
        gamemodes.getNormalMode().getEndObserver().setGameManager(gameManager);
        gamemodes.getFinalFrenzyMode().getEndObserver().setGameManager(gameManager);
        for(Player p : gameManager.getPlayerOrderTurn()) {
            p.setCurrentTurnAction(gameManager.getCurrentGameMode().getBaseTurnAction());
            p.setBoard(new PointsBoard(Arrays.asList(scores),1));
        }
        gameManager.setNotifyObservers(views);
        turnStateController= new TurnStateController(commandsFile);
        for(Player p:players) {
            p.setViewPlayer(new ViewProxy(new ViewMock(), "ciao",turnStateController));
            views.add(p.getViewPlayer());
        }
    }


    private void fill() {
        gameManager.getWeaponsDeck().shuffle();
        gameManager.getPowerUpDeck().shuffle();
        gameManager.getAmmoTileDeck().shuffle();
        gameManager.fillAllAmmoTiles();
        gameManager.fillWeaponSpawnPoint();

    }

    @Test
    void deathAndRespawnTest(){
        fill();
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
        int i=0;
        while(i<11){
            if(i<5)
                enemy2.addDamageTaken(enemy1,1,gameManager);
            else
                enemy2.addDamageTaken(p,1,gameManager);
            gameManager.setPlayerState(enemy2);
            i++;
        }
        i=0;
        assertEquals(11,enemy2.getDamageTaken().size());
        assertEquals(PlayerState.DEAD,enemy2.getPlayerState());
        while(i<12){
            if(i<5)
                enemy1.addDamageTaken(enemy2,1,gameManager);
            else
                enemy1.addDamageTaken(p,1,gameManager);
            gameManager.setPlayerState(enemy1);
            i++;
        }
        assertEquals(12,enemy1.getDamageTaken().size());
        assertEquals(PlayerState.DEAD,enemy1.getPlayerState());
        p.setCurrentTile(gameManager.getTiles().get(0).get(0));
        gameManager.getTiles().get(0).get(0).addPlayer(p);
        enemy1.setCurrentTile(gameManager.getTiles().get(0).get(1));
        gameManager.getTiles().get(0).get(1).addPlayer(enemy1);
        enemy2.setCurrentTile(gameManager.getTiles().get(0).get(2));
        gameManager.getTiles().get(0).get(2).addPlayer(enemy2);
        enemy2.setTurnState(TurnState.READY_TO_RESPAWN);
        enemy2.setTurnState(TurnState.READY_TO_RESPAWN);
        p.grabActionAfterTile(gameManager,gameManager.getTiles().get(0).get(0));
        p.grabActionAfterTile(gameManager,gameManager.getTiles().get(0).get(1));
        assertEquals(1,p.getMarks().get(enemy1));
        p.setScore(18);
        enemy1.setScore(7);
        enemy2.setScore(7);
        assertEquals(18,p.getScore());
        assertEquals(7,enemy1.getScore());
        assertEquals(7,enemy2.getScore());
        if(enemy1.getTurnState()==TurnState.DISCARD_POWERUP_FOR_SPAWN) {
            assertEquals(TurnState.DISCARD_POWERUP_FOR_SPAWN, enemy1.getTurnState());
            assertEquals(TurnState.READY_TO_RESPAWN, enemy2.getTurnState());
            gameManager.spawnSetPosition(enemy1, enemy1.getPowerUps().get(0));
            assertEquals(TurnState.WAIT_FOR_OTHER_PLAYERS_TURN, enemy1.getTurnState());
            assertEquals(TurnState.DISCARD_POWERUP_FOR_SPAWN, enemy2.getTurnState());
            gameManager.spawnSetPosition(enemy2, enemy2.getPowerUps().get(0));
            assertEquals(TurnState.READY_FOR_ACTION, enemy1.getTurnState());
        }
        if(enemy2.getTurnState()==TurnState.DISCARD_POWERUP_FOR_SPAWN){
            assertEquals(TurnState.DISCARD_POWERUP_FOR_SPAWN, enemy1.getTurnState());
            assertEquals(TurnState.READY_TO_RESPAWN, enemy2.getTurnState());
            gameManager.spawnSetPosition(enemy1, enemy1.getPowerUps().get(0));
            assertEquals(TurnState.WAIT_FOR_OTHER_PLAYERS_TURN, enemy1.getTurnState());
            assertEquals(TurnState.DISCARD_POWERUP_FOR_SPAWN, enemy2.getTurnState());
            gameManager.spawnSetPosition(enemy2, enemy2.getPowerUps().get(0));
            assertEquals(TurnState.READY_FOR_ACTION, enemy1.getTurnState());
        }
    }
}
