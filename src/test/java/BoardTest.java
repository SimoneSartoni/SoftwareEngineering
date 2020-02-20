import controller.TurnStateController;
import model.board.*;
import model.enums.PlayerColor;
import model.enums.PlayerState;
import model.gamemodes.BoardStructure;
import model.gamemodes.Gamemodes;
import model.player.Player;
import model.powerup.PowerUp;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    private static GameManager gameManager;
    private static TurnStateController turnStateController;

    @BeforeAll
    static void createMap() {
        final String fileToParse = "fileToParse/";
        final String powFile = fileToParse + "powerUpToParse.txt";
        final String ammoFile = fileToParse + "ammoTilesToParse.txt";
        final String mapFile = fileToParse + "map.txt";
        final String pointsFile = fileToParse + "points.txt";
        final String commandsFile = fileToParse + "state_commands.txt";
        Player player = new Player("Carmelo", PlayerColor.YELLOW, PlayerState.NORMAL, null, null);
        List<Player> players = new ArrayList<>();
        players.add(player);
        Gamemodes gamemodes = new Gamemodes(new BoardStructure(pointsFile));
        gameManager = new GameManager(new Deck<>(null), new Deck<>(InitMap.initPowerUps(powFile)),
                new Deck<>(InitMap.initAmmoTiles(ammoFile)), players, InitMap.initMap(mapFile).get(0).getMap(),
                gamemodes.getNormalMode(), null, false,
                gamemodes, InitMap.initMap(mapFile).get(0).getMapWidth(), InitMap.initMap(mapFile).get(0).getMapHeight());
        gameManager.getPlayerOrderTurn().get(0).setCurrentTile(gameManager.getTiles().get(0).get(0));
        gameManager.getAmmoTileDeck().shuffle();
        for(Player p : gameManager.getPlayerOrderTurn()) {
            p.setCurrentTurnAction(gameManager.getCurrentGameMode().getBaseTurnAction());
        }

        assertEquals(36, gameManager.getAmmoTileDeck().pileSize());
        assertEquals(0, gameManager.getAmmoTileDeck().discardPileSize());
        gameManager.fillAllAmmoTiles();
        assertEquals(29, gameManager.getAmmoTileDeck().pileSize());
        assertEquals(0, gameManager.getAmmoTileDeck().discardPileSize());
        singleAmmoTileFillTest();

        //dummy Tests
        for(int i = 0; i < gameManager.getAmmoTileDeck().pileSize(); i++) {
            AmmoTile ammoTile = gameManager.getAmmoTileDeck().draw();
            assertNotNull(ammoTile.getAmmoGained());
            assertTrue(ammoTile.getNOfPowerUp() >= 0);
            gameManager.getAmmoTileDeck().addToDiscardPile(ammoTile);
            gameManager.getAmmoTileDeck().reShuffleAll();
        }
        for(int i = 0; i < gameManager.getPowerUpDeck().pileSize(); i++) {
            PowerUp powerUp = gameManager.getPowerUpDeck().draw();
            assertNotNull(powerUp.getPowerUpEffect());
           // assertNotNull(powerUp.getPowerUpEffect().getAction());
            gameManager.getPowerUpDeck().addToDiscardPile(powerUp);
            gameManager.getPowerUpDeck().reShuffleAll();
        }

        assertEquals(24, gameManager.getPowerUpDeck().pileSize());
        assertEquals(0, gameManager.getPowerUpDeck().discardPileSize());
        turnStateController= new TurnStateController(commandsFile);

    }

    @Test
    void MapTest() {
        List<List<Tile>> map = gameManager.getTiles();
        Tile tile = map.get(0).get(0);
        List <Tile> verifiedTiles = new ArrayList<>();
        verifiedTiles.add(map.get(0).get(0));
        verifiedTiles.add(map.get(0).get(1));
        verifiedTiles.add(map.get(0).get(2));
        assertEquals(verifiedTiles, gameManager.getTileOfRoom(tile));

        verifiedTiles.clear();
        tile = map.get(0).get(1);
        verifiedTiles.add(map.get(0).get(0));
        verifiedTiles.add(map.get(0).get(1));
        verifiedTiles.add(map.get(0).get(2));
        assertEquals(verifiedTiles, gameManager.getTileOfRoom(tile));

        verifiedTiles.clear();
        tile = map.get(0).get(2);
        verifiedTiles.add(map.get(0).get(0));
        verifiedTiles.add(map.get(0).get(1));
        verifiedTiles.add(map.get(0).get(2));
        assertEquals(verifiedTiles, gameManager.getTileOfRoom(tile));

        verifiedTiles.clear();
        tile = map.get(1).get(0);
        verifiedTiles.add(map.get(1).get(0));
        verifiedTiles.add(map.get(1).get(1));
        verifiedTiles.add(map.get(1).get(2));
        assertEquals(verifiedTiles, gameManager.getTileOfRoom(tile));

        verifiedTiles.clear();
        tile = map.get(1).get(1);
        verifiedTiles.add(map.get(1).get(0));
        verifiedTiles.add(map.get(1).get(1));
        verifiedTiles.add(map.get(1).get(2));
        assertEquals(verifiedTiles, gameManager.getTileOfRoom(tile));

        verifiedTiles.clear();
        tile = map.get(1).get(2);
        verifiedTiles.add(map.get(1).get(0));
        verifiedTiles.add(map.get(1).get(1));
        verifiedTiles.add(map.get(1).get(2));
        assertEquals(verifiedTiles, gameManager.getTileOfRoom(tile));

        verifiedTiles.clear();
        tile = map.get(1).get(3);
        verifiedTiles.add(map.get(1).get(3));
        verifiedTiles.add(map.get(2).get(3));
        assertEquals(verifiedTiles, gameManager.getTileOfRoom(tile));

        verifiedTiles.clear();
        tile = map.get(2).get(3);
        verifiedTiles.add(map.get(1).get(3));
        verifiedTiles.add(map.get(2).get(3));
        assertEquals(verifiedTiles, gameManager.getTileOfRoom(tile));

        verifiedTiles.clear();
        tile = map.get(2).get(1);
        verifiedTiles.add(map.get(2).get(1));
        verifiedTiles.add(map.get(2).get(2));
        assertEquals(verifiedTiles, gameManager.getTileOfRoom(tile));

        verifiedTiles.clear();
        tile = map.get(2).get(2);
        verifiedTiles.add(map.get(2).get(1));
        verifiedTiles.add(map.get(2).get(2));
        assertEquals(verifiedTiles, gameManager.getTileOfRoom(tile));

        verifiedTiles.clear();
        tile = map.get(0).get(3);
        assertEquals(3, gameManager.getTileOfRoom(tile).size());
        tile = map.get(2).get(0);
        assertEquals(2, gameManager.getTileOfRoom(tile).size());
    }

    @Test
    void ammoTilesFillTest() {
        for(List<Tile> tileRow : gameManager.getTiles()) {
            for(Tile tile : tileRow) {
                if(!tile.isSpawnPoint() && !tile.isHole()) {
                    assertNotNull(tile.getAmmo());
                }
                else {
                    assertNull(tile.getAmmo());
                }
            }
        }
    }

    @Test
    private static void singleAmmoTileFillTest() {
        Tile spawnTile = gameManager.getTiles().get(1).get(0);
        Tile filledTile = gameManager.getTiles().get(0).get(0);

        gameManager.fillAmmoTile(spawnTile);
        assertNull(spawnTile.getAmmo());
        gameManager.fillAmmoTile(filledTile);
        assertEquals(29, gameManager.getAmmoTileDeck().pileSize());

        AmmoTile ammoTile = filledTile.removeAmmo();
        gameManager.fillAmmoTile(filledTile);
        assertEquals(28, gameManager.getAmmoTileDeck().pileSize());
        gameManager.getAmmoTileDeck().addToPile(ammoTile);
    }

    @Test
    void drawPowerUpsTest() {
        Player player = gameManager.getPlayerOrderTurn().get(0);
        PowerUp pow1 = gameManager.getPowerUpDeck().draw();
        PowerUp drawn = player.drawPowerUp(gameManager,pow1);
        assertNull(drawn);
        assertEquals(23, gameManager.getPowerUpDeck().pileSize());
        assertNull(player.drawPowerUp(gameManager,gameManager.getPowerUpDeck().draw()));
        assertNull(player.drawPowerUp(gameManager,gameManager.getPowerUpDeck().draw()));
        assertEquals(21, gameManager.getPowerUpDeck().pileSize());
        drawn = gameManager.getPowerUpDeck().draw();
        assertNotNull(player.drawPowerUp(gameManager,drawn));
        //We should do in the code, like:
        if(player.drawPowerUp(gameManager,drawn) != null) {
            gameManager.getPowerUpDeck().addToPile(drawn);
        }
        assertEquals(21, gameManager.getPowerUpDeck().pileSize());
    }
}
