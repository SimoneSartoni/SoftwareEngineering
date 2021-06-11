import controller.TurnStateController;
import model.board.GameManager;
import model.board.KillShotTrack;
import model.board.Tile;
import model.enums.PlayerColor;
import model.enums.PlayerState;
import model.enums.RoomColor;
import model.enums.TileLinks;
import model.gamemodes.BoardStructure;
import model.gamemodes.Gamemodes;
import model.player.Player;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameManagerVisibilityTest {
    private static GameManager gameManager;
    private static List<Player> players;
    private static List<List<Tile>> miniMap;
    private static TurnStateController turnStateController;

    @BeforeAll
    static void before() {
        final String fileToParse = "fileToParse/";
        final String pointsFile = fileToParse + "points.txt";
        List<Tile> a= new ArrayList<>(0);
        a.add(new Tile(RoomColor.GREEN, 0,0, false, TileLinks.ENDOFMAP, TileLinks.DOOR, TileLinks.ENDOFMAP, TileLinks.DOOR, null));
        a.add(new Tile(RoomColor.YELLOW, 0,1,false, TileLinks.ENDOFMAP, TileLinks.DOOR, TileLinks.DOOR, TileLinks.ENDOFMAP, null));
        List<Tile> b= new ArrayList<>(0);
        b.add(new Tile(RoomColor.RED, 1,0, false, TileLinks.DOOR, TileLinks.ENDOFMAP, TileLinks.ENDOFMAP, TileLinks.DOOR, null));
        b.add(new Tile(RoomColor.PURPLE, 1,1, false, TileLinks.DOOR, TileLinks.ENDOFMAP, TileLinks.DOOR, TileLinks.ENDOFMAP, null));
        List<Tile> c= new ArrayList<>(0);
        c.add(new Tile(RoomColor.GREY,2,0,false,TileLinks.ENDOFMAP,  TileLinks.ENDOFMAP,TileLinks.ENDOFMAP,  TileLinks.ENDOFMAP,null));
        c.add(new Tile(RoomColor.GREEN,2,1,false,TileLinks.HOLE,TileLinks.HOLE,TileLinks.HOLE,TileLinks.HOLE,null));
        miniMap=new ArrayList<>();
        miniMap.add(a);
        miniMap.add(b);
        miniMap.add(c);
        players= new ArrayList<>();
        players.add(new Player("a", PlayerColor.GREEN, PlayerState.NORMAL,miniMap.get(0).get(0), null));
        players.add(new Player("b", PlayerColor.GREY,PlayerState.NORMAL,miniMap.get(0).get(1), null));
        players.add(new Player("c", PlayerColor.BLUE,PlayerState.NORMAL,miniMap.get(1).get(0), null));
        players.add(new Player("d", PlayerColor.PURPLE,PlayerState.NORMAL,miniMap.get(1).get(1), null));
        players.add(new Player("e",PlayerColor.YELLOW,PlayerState.NORMAL,miniMap.get(2).get(0), null));
        miniMap.get(0).get(0).addPlayer(players.get(0));
        miniMap.get(0).get(1).addPlayer(players.get(1));
        miniMap.get(1).get(0).addPlayer(players.get(2));
        miniMap.get(1).get(1).addPlayer(players.get(3));
        miniMap.get(2).get(0).addPlayer(players.get(4));
        assertEquals(5,players.size());
        Gamemodes gamemodes = new Gamemodes(new BoardStructure(pointsFile));
        gameManager = new GameManager(null, null, null, players,
                miniMap, gamemodes.getNormalMode(), new KillShotTrack(gamemodes.getNormalMode().getPointsBoard().getPoints(),8, new ArrayList<>(), 1), false,
                gamemodes, 4, 3);
        for(Player p : gameManager.getPlayerOrderTurn()) {
            p.setCurrentTurnAction(gameManager.getCurrentGameMode().getBaseTurnAction());
        }

    }

    @Test
    void testVisibility(){
     List<List<Tile>> a=new ArrayList<>();
     a.addAll(gameManager.getTiles());
        List<Player> temp=new ArrayList<>();
        Tile tile=new Tile();
         tile=gameManager.getTiles().get(0).get(0);
         assertEquals(3,gameManager.getTiles().size());
         assertEquals("a",gameManager.getTiles().get(0).get(0).getPlayers().get(0).getPlayerID());
         assertEquals("b",gameManager.getTiles().get(0).get(1).getPlayers().get(0).getPlayerID());
         assertEquals("c",gameManager.getTiles().get(1).get(0).getPlayers().get(0).getPlayerID());
         assertEquals("d",gameManager.getTiles().get(1).get(1).getPlayers().get(0).getPlayerID());
         temp.addAll(gameManager.getVisiblePlayers(gameManager.getTiles().get(1).get(1).getPlayers().get(0)));
    String b="";
    for(Player p: temp)
        b=b+p.getPlayerID();
    assertEquals("bc",b);
    }

    @Test
    void testDistance(){
        assertTrue((gameManager.getPossibleTiles(1,2,gameManager.getTiles().get(0).get(0))).contains(gameManager.getTiles().get(1).get(0)));
        assertTrue((gameManager.getPossibleTiles(1,2,gameManager.getTiles().get(0).get(0))).contains(gameManager.getTiles().get(0).get(1)));
        assertTrue((gameManager.getPossibleTiles(1,2,gameManager.getTiles().get(0).get(0))).contains(gameManager.getTiles().get(1).get(1)));
        assertEquals(2, gameManager.getDistanceBetweenTiles(miniMap.get(0).get(0),miniMap.get(1).get(1)));
        assertEquals(0, gameManager.getDistanceBetweenTiles(miniMap.get(0).get(0),miniMap.get(0).get(0)));
        assertEquals(1, gameManager.getDistanceBetweenTiles(miniMap.get(0).get(0),miniMap.get(0).get(1)));
        assertEquals(1, gameManager.getDistanceBetweenTiles(miniMap.get(0).get(0),miniMap.get(1).get(0)));
    }

    @Test
    void killshotTrackScoresTest1() {
        KillShotTrack killshotTrack = gameManager.getKillShotTrack();
        killshotTrack.getDeathOrder().clear();
        killshotTrack.setKill(players.get(1).getPlayerColor(), false);
        killshotTrack.setKill(players.get(3).getPlayerColor(), true);
        killshotTrack.setKill(players.get(3).getPlayerColor(), true);
        killshotTrack.setKill(players.get(3).getPlayerColor(), true);
        killshotTrack.setKill(players.get(0).getPlayerColor(), true);
        killshotTrack.setKill(players.get(2).getPlayerColor(), true);
        killshotTrack.setKill(players.get(2).getPlayerColor(), true);
        killshotTrack.setKill(players.get(2).getPlayerColor(), true);
        killshotTrack.setKill(players.get(2).getPlayerColor(), true);
        killshotTrack.setKill(players.get(4).getPlayerColor(), true);
        killshotTrack.setKill(players.get(4).getPlayerColor(), true);
        killshotTrack.setKill(players.get(4).getPlayerColor(), true);

        gameManager.endOfGame();

        assertEquals(8, players.get(2).getScore());
        assertEquals(6, players.get(3).getScore());
        assertEquals(4, players.get(4).getScore());
        assertEquals(2, players.get(0).getScore());
        assertEquals(1, players.get(1).getScore());
    }

    @Test
    void killshotTrackScoresTest2() {
        KillShotTrack killshotTrack = gameManager.getKillShotTrack();
        for(Player player : players) {
            killshotTrack.getDeathCount().put(player.getPlayerColor(), 0);
            player.setScore(0);
        }
        killshotTrack.getDeathOrder().clear();
        killshotTrack.setKill(players.get(1).getPlayerColor(), true);
        killshotTrack.setKill(players.get(3).getPlayerColor(), true);
        killshotTrack.setKill(players.get(0).getPlayerColor(), true);
        killshotTrack.setKill(players.get(2).getPlayerColor(), true);
        killshotTrack.setKill(players.get(4).getPlayerColor(), true);

        gameManager.endOfGame();

        assertEquals(8, players.get(1).getScore());
        assertEquals(6, players.get(3).getScore());
        assertEquals(4, players.get(0).getScore());
        assertEquals(2, players.get(2).getScore());
        assertEquals(1, players.get(4).getScore());
    }
}
