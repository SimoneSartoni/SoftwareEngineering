import controller.TurnStateController;
import model.board.*;
import model.enums.LoadedState;
import model.enums.PlayerColor;
import model.enums.PlayerState;
import model.enums.TurnState;
import model.gamemodes.BoardStructure;
import model.gamemodes.Gamemodes;
import model.player.Player;
import model.powerup.PowerUp;
import model.weapon.InitWeapons;
import model.weapon.Weapon;
import network.ViewProxy;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class GameManagerTest {
    private static GameManager gameManager;
    private static TurnStateController turnStateController;
    private static void setTiles(GameManager gameManager) {
        gameManager.getPlayerOrderTurn().get(0).setCurrentTile(gameManager.getTiles().get(1).get(0));
        gameManager.getTiles().get(1).get(0).addPlayer(gameManager.getPlayerOrderTurn().get(0));
        gameManager.getPlayerOrderTurn().get(1).setCurrentTile(gameManager.getTiles().get(1).get(0));
        gameManager.getTiles().get(1).get(0).addPlayer(gameManager.getPlayerOrderTurn().get(1));
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
        Integer [] scores = {8, 6, 4, 2 ,1 ,1};
        List<Player> players = new ArrayList<>();
        players.add(new Player("Carmelo", PlayerColor.YELLOW, PlayerState.NORMAL, null, null));
        players.add(new Player("Unaltro", PlayerColor.GREEN, PlayerState.NORMAL, null, null));
        KillShotTrack killshotTrack = new KillShotTrack(Arrays.asList(scores), 8, new ArrayList<>(), 1);
        Gamemodes gamemodes = new Gamemodes(new BoardStructure(pointsFile));
        gameManager = new GameManager(new Deck<Weapon>(InitWeapons.initAllWeapons(weaponFile)),new Deck<PowerUp>(InitMap.initPowerUps(powFile)),
                new Deck<AmmoTile>(InitMap.initAmmoTiles(ammoFile)), players, InitMap.initMap(mapFile).get(0).getMap(),
                gamemodes.getNormalMode(), killshotTrack, false, gamemodes, InitMap.initMap(mapFile).get(0).getMapWidth(), InitMap.initMap(mapFile).get(0).getMapHeight());
        setTiles(gameManager);
        turnStateController= new TurnStateController(commandsFile);
        for(Player p : gameManager.getPlayerOrderTurn()) {
            p.setCurrentTurnAction(gameManager.getCurrentGameMode().getBaseTurnAction());
            p.setViewPlayer(new ViewProxy(new ViewMock(), "NOT INIT",turnStateController));
        }
    }

    @Test
    void weaponsDeckTest() {
        int nOfWeapon = gameManager.getWeaponsDeck().pileSize();
        for(int i = 0; i < nOfWeapon; i++) {
            Weapon weapon = gameManager.getWeaponsDeck().draw();
            gameManager.getWeaponsDeck().addToDiscardPile(weapon);
        }
        Deck powUpDeck = gameManager.getPowerUpDeck();
        Deck ammoTileDeck = gameManager.getAmmoTileDeck();
        assertEquals(0, gameManager.getWeaponsDeck().pileSize());
        assertTrue(gameManager.getWeaponsDeck().isEmpty());
        assertEquals(nOfWeapon,gameManager.getWeaponsDeck().discardPileSize());
        assertNotNull(powUpDeck);
        assertNotNull(ammoTileDeck);
        gameManager.getWeaponsDeck().reShuffleAll();
    }

    @Test
    void weaponsEffectTest() {
        int nOfWeapon = gameManager.getWeaponsDeck().pileSize();
        List<Weapon> supportArray = new ArrayList<>();
        for(int i = 0; i < nOfWeapon; i++) {
            Weapon weapon = gameManager.getWeaponsDeck().draw();
            assertNotNull(weapon.getBaseEffect());
            assertEquals(LoadedState.PARTIALLY_LOADED, weapon.getLoaded());
            if(weapon.getTypeEffect() == 0) {
                assertEquals(0, weapon.getOptionalEffect().size());
                assertNull(weapon.getAlternativeEffect());
            }
            else if(weapon.getTypeEffect() == 1) {
                assertNotNull(weapon.getOptionalEffect());
                assertTrue(! weapon.getOptionalEffect().isEmpty());
                assertNull(weapon.getAlternativeEffect());
            }
            else {
                assertEquals(0, weapon.getOptionalEffect().size());
                assertNotNull(weapon.getAlternativeEffect());
            }
            supportArray.add(weapon);
        }
        gameManager.getWeaponsDeck().addToPile(supportArray);
        assertEquals(nOfWeapon, gameManager.getWeaponsDeck().pileSize());
        assertEquals(0, gameManager.getWeaponsDeck().discardPileSize());
    }

    @Test
    void shuffleDeckTest() {
        int pileSize = gameManager.getWeaponsDeck().pileSize();
        int discardPileSize = gameManager.getWeaponsDeck().discardPileSize();
        gameManager.getWeaponsDeck().shuffle();
        assertEquals(pileSize, gameManager.getWeaponsDeck().pileSize());
        assertEquals(discardPileSize, gameManager.getWeaponsDeck().discardPileSize());
        gameManager.getWeaponsDeck().reShuffleAll();
        assertEquals(pileSize, gameManager.getWeaponsDeck().pileSize());
        assertEquals(discardPileSize, gameManager.getWeaponsDeck().discardPileSize());
        Weapon weapon = gameManager.getWeaponsDeck().draw();
        gameManager.getWeaponsDeck().addToDiscardPile(weapon);
        assertEquals(pileSize - 1, gameManager.getWeaponsDeck().pileSize());
        assertEquals(discardPileSize + 1, gameManager.getWeaponsDeck().discardPileSize());
        gameManager.getWeaponsDeck().reShuffleAll();
        assertEquals(pileSize, gameManager.getWeaponsDeck().pileSize());
        assertEquals(discardPileSize, gameManager.getWeaponsDeck().discardPileSize());

        for(int i = 0; i < pileSize; i++) {
            Weapon w = gameManager.getWeaponsDeck().draw();
            gameManager.getWeaponsDeck().addToDiscardPile(w);
        }
        gameManager.getWeaponsDeck().reShuffleAll();
        Weapon w1 = null;
        for(int i = 0; i < pileSize; i++) {
            Weapon w = gameManager.getWeaponsDeck().draw();
            if(w1 != null)
                assertNotEquals(w1.getIdName(), w.getIdName());
            w1 = w;
            gameManager.getWeaponsDeck().addToDiscardPile(w);
        }
        gameManager.getWeaponsDeck().reShuffleAll();
    }

    @Test
    void spawnInteractionsTest() {
        gameManager.startGame();
        assertEquals(gameManager.getFirstPlayerToPlay(), gameManager.getCurrentPlayerTurn());
        assertEquals(2, gameManager.getNOfPlayers());
        gameManager.spawnDrawPhase(gameManager.getCurrentPlayerTurn(),2);
        spawnDrawPhaseTest();

        spawnSetPosTest();

        changeTurnTest();

    }

    @Test
    private void spawnDrawPhaseTest() {
        Player currentPlayer = gameManager.getCurrentPlayerTurn();
        assertNotNull(currentPlayer);
        assertEquals(TurnState.DISCARD_POWERUP_FOR_SPAWN, currentPlayer.getTurnState());
        assertEquals(2, currentPlayer.getPowerUps().size());
        //test with power up deck empty
        List<PowerUp> powerUps = new ArrayList<>();
        while(gameManager.getPowerUpDeck().pileSize() > 0) {
            powerUps.add(gameManager.getPowerUpDeck().draw());
        }
        gameManager.getPowerUpDeck().addToDiscardPile(powerUps);
        gameManager.spawnDrawPhase(gameManager.getPlayerOrderTurn().get(1), 1);
        assertEquals(TurnState.DISCARD_POWERUP_FOR_SPAWN, gameManager.getPlayerOrderTurn().get(1).getTurnState());
        assertEquals(1, gameManager.getPlayerOrderTurn().get(1).getPowerUps().size());
    }

    @Test
    private void spawnSetPosTest() {
        Player currentPlayer = gameManager.getCurrentPlayerTurn();
        PowerUp discarded = currentPlayer.discardPowerUp(0);
        gameManager.spawnSetPosition(currentPlayer, discarded);
        Tile currentTile = currentPlayer.getCurrentTile();
        assertTrue(currentTile.getRoom().name().toLowerCase().matches(discarded.getColor().name().toLowerCase()));
        assertTrue(currentTile.getPlayers().contains(currentPlayer));
        assertEquals(TurnState.READY_FOR_ACTION, currentPlayer.getTurnState());
        assertEquals(PlayerState.NORMAL, currentPlayer.getPlayerState());
    }

    @Test
    private void changeTurnTest() {
        Player currentPlayer = gameManager.getCurrentPlayerTurn();
        gameManager.changeTurn();
        assertNotEquals(gameManager.getCurrentPlayerTurn(), currentPlayer);
        gameManager.changeTurn();
        assertEquals(gameManager.getCurrentPlayerTurn(), currentPlayer);
    }

    @Test
    void turnActionTest() {
        final String fileToParse = "fileToParse/";
        final String pointsFile = fileToParse + "points.txt";
        Player placeholderPlayer = new Player("Placeholder", PlayerColor.YELLOW, PlayerState.NORMAL, null, gameManager.getCurrentGameMode().getBaseTurnAction());
        assertTrue(placeholderPlayer.isValidMovement());
        placeholderPlayer.setCurrentTurnAction(new Gamemodes(new BoardStructure(pointsFile)).getFinalFrenzyMode().getFrenzyAfterTurnAction());
        assertFalse(placeholderPlayer.isValidMovement());
    }


    @Test
    void ListTest(){
       List<Player> turn=new ArrayList<>(gameManager.getPlayerOrderTurn());
       turn.remove(0);
       assertTrue(2==gameManager.getPlayerOrderTurn().size());
    }
}

