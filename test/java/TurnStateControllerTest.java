import controller.TurnStateController;
import model.board.*;
import model.enums.PlayerColor;
import model.enums.PlayerState;
import model.enums.TurnState;
import model.gamemodes.BoardStructure;
import model.gamemodes.Gamemodes;
import model.player.Player;
import model.powerup.PowerUp;
import model.utility.Ammo;
import model.weapon.InitWeapons;
import model.weapon.Weapon;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TurnStateControllerTest {

    private static TurnStateController turnStateController;
    private static GameManager gameManager;

    private static KillShotTrack killShotTrack;

    static final String fileToParse = "fileToParse/";
    static final String powFile = fileToParse + "powerUpToParse.txt";
    static final String ammoFile = fileToParse + "ammoTilesToParse.txt";
    static final String mapFile = fileToParse + "map.txt";
    static final String pointsFile = fileToParse + "points.txt";
    static final String weaponFile = fileToParse + "weaponsToParse.txt";
    static final String turnStateFile = fileToParse + "state_commands.txt";

    private static void setTiles(GameManager gameManager) {
        gameManager.getPlayerOrderTurn().get(0).setCurrentTile(gameManager.getTiles().get(1).get(1));
        gameManager.getTiles().get(1).get(1).addPlayer(gameManager.getPlayerOrderTurn().get(0));
        gameManager.getPlayerOrderTurn().get(1).setCurrentTile(gameManager.getTiles().get(0).get(1));
        gameManager.getTiles().get(0).get(1).addPlayer(gameManager.getPlayerOrderTurn().get(1));
        gameManager.getPlayerOrderTurn().get(2).setCurrentTile(gameManager.getTiles().get(2).get(1));
        gameManager.getTiles().get(2).get(1).addPlayer(gameManager.getPlayerOrderTurn().get(2));
    }

    @BeforeAll
    static void before() {
        final Player[] arrayPlayers = {new Player("Yellow", PlayerColor.YELLOW, PlayerState.NORMAL, null, null),
                new Player("Purple", PlayerColor.PURPLE, PlayerState.NORMAL, null, null),
                new Player("Green", PlayerColor.GREEN, PlayerState.NORMAL, null, null)
        };
        final List<Player> players = new ArrayList<>(Arrays.asList(arrayPlayers));
        final Integer [] scores = {8, 6, 4, 2, 1, 1};
        final Integer [] frenzyScores = {2, 1, 1, 1};
        Gamemodes gamemodes = new Gamemodes(new BoardStructure(pointsFile));

        gameManager = new GameManager(new Deck<Weapon>(InitWeapons.initAllWeapons(weaponFile)), new Deck<PowerUp>(InitMap.initPowerUps(powFile)),
                new Deck<AmmoTile>(InitMap.initAmmoTiles(ammoFile)), players, InitMap.initMap(mapFile).get(0).getMap(), gamemodes.getNormalMode(),
                killShotTrack, false, gamemodes, InitMap.initMap(mapFile).get(0).getMapWidth(), InitMap.initMap(mapFile).get(0).getMapHeight());

        setTiles(gameManager);

        for(Player p : gameManager.getPlayerOrderTurn()) {
            p.setCurrentTurnAction(gameManager.getCurrentGameMode().getBaseTurnAction());
        }

        turnStateController = new TurnStateController(turnStateFile);
    }

    @Test
    void commandsTest() {
        Player player = new Player("Player", PlayerColor.YELLOW, PlayerState.NORMAL, gameManager.getTiles().get(0).get(0), gameManager.getCurrentGameMode().getBaseTurnAction());
        player.setAmmo(new Ammo(3,3,3));
        gameManager.getTiles().get(0).get(0).addPlayer(player);
        gameManager.setCurrentPlayer(player);
        gameManager.getCurrentTurn().setCurrentPlayerForVisibility(player);
        List<Weapon> weapons = new ArrayList<>();
        for (int i = 0; i < 21; i++) {
            weapons.add(gameManager.getWeaponsDeck().draw());
        }

        player.setTurnState(TurnState.READY_TO_SPAWN);
        assertFalse(turnStateController.getValidCommands(gameManager,player).isEmpty());

        player.setTurnState(TurnState.CHOOSE_COUNTER_ATTACK);
        assertEquals(7, turnStateController.getValidCommands(gameManager,player).size());
        assertTrue(turnStateController.getValidCommands(gameManager,player).contains("powerup"));
        assertTrue(turnStateController.getValidCommands(gameManager,player).contains("nothing"));

        player.setTurnState(TurnState.READY_FOR_ACTION);
        assertEquals(6, turnStateController.getValidCommands(gameManager,player).size());
        assertTrue(turnStateController.getValidCommands(gameManager,player).contains("action"));

        player.setTurnState(TurnState.CHOOSE_TYPE_OF_EFFECT);
        gameManager.getCurrentTurn().setCurrentWeapon(weapons.get(4));
        assertEquals(6, turnStateController.getValidCommands(gameManager,player).size());
        assertTrue(turnStateController.getValidCommands(gameManager,player).contains("type"));
        weapons.get(4).getBaseEffect().setExecuted(true);
        assertEquals(6, turnStateController.getValidCommands(gameManager,player).size());
        gameManager.getCurrentTurn().setCurrentWeapon(weapons.get(0));
        assertEquals(6, turnStateController.getValidCommands(gameManager,player).size());
        assertTrue(turnStateController.getValidCommands(gameManager,player).contains("type"));
        weapons.get(0).getBaseEffect().setExecuted(true);
        weapons.get(0).getOptionalEffect().get(0).setCanBeUsed(true);
        assertEquals(6, turnStateController.getValidCommands(gameManager,player).size());
        assertTrue(turnStateController.getValidCommands(gameManager,player).contains("type"));
        weapons.get(0).getOptionalEffect().get(0).setExecuted(true);
        assertEquals(6, turnStateController.getValidCommands(gameManager,player).size());
        assertEquals(6, turnStateController.getValidCommands(gameManager,player).size());
        assertTrue(turnStateController.getValidCommands(gameManager,player).contains("type"));
        weapons.get(20).getBaseEffect().setExecuted(true);
        assertEquals(6, turnStateController.getValidCommands(gameManager,player).size());
        //in case of other tests
        gameManager.getTiles().get(0).get(0).removePlayer(player);
        weapons.get(4).getBaseEffect().setExecuted(false);
        weapons.get(0).getBaseEffect().setExecuted(false);
        weapons.get(0).getOptionalEffect().get(0).setCanBeUsed(false);
        weapons.get(0).getOptionalEffect().get(0).setExecuted(false);
        weapons.get(20).getBaseEffect().setExecuted(true);
        gameManager.getWeaponsDeck().addToPile(weapons);
    }

}
