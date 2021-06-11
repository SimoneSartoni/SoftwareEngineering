import controller.TurnStateController;
import model.board.*;
import model.enums.Direction;
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
import network.ViewProxy;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WeaponTest {

    private static GameManager gameManager;
    private static TurnAction baseTurnAction = new TurnAction(2, 0, false, 3, 1);
    private static TurnStateController turnStateController;

    @BeforeAll
    static void before() {
        final String fileToParse = "fileToParse/";
        final String commandsFile = fileToParse + "state_commands.txt";
        final String powFile = fileToParse + "powerUpToParse.txt";
        final String ammoFile = fileToParse + "ammoTilesToParse.txt";
        final String mapFile = fileToParse + "map.txt";
        final String pointsFile = fileToParse + "points.txt";
        final String weaponFile = fileToParse + "weaponsToParse.txt";
        Gamemodes gamemodes = new Gamemodes(new BoardStructure(pointsFile));
        gameManager = new GameManager(new Deck<Weapon>(InitWeapons.initAllWeapons(weaponFile)), new Deck<PowerUp>(InitMap.initPowerUps(powFile)),
                new Deck<AmmoTile>(InitMap.initAmmoTiles(ammoFile)), new ArrayList<>(), InitMap.initMap(mapFile).get(0).getMap(),
                gamemodes.getNormalMode(), null, false, gamemodes,
                InitMap.initMap(mapFile).get(0).getMapWidth(), InitMap.initMap(mapFile).get(0).getMapHeight());
        turnStateController= new TurnStateController(commandsFile);
    }

    @Test
    void executingEffectTest() {
        Player placeholderPlayer = new Player("Placeholder", PlayerColor.YELLOW, PlayerState.NORMAL, gameManager.getTiles().get(0).get(0), baseTurnAction);
        placeholderPlayer.setTurnState(TurnState.READY_FOR_ACTION);
        placeholderPlayer.setViewPlayer(new ViewProxy(new ViewMock(), "NOT INIT",new TurnStateController()));
        gameManager.getTiles().get(0).get(0).addPlayer(placeholderPlayer);
        Player enemy = new Player("Enemy", PlayerColor.GREEN, PlayerState.NORMAL, gameManager.getTiles().get(1).get(0), baseTurnAction);
        placeholderPlayer.setViewPlayer(new ViewProxy(new ViewMock(),"NOT INIT",turnStateController));
        enemy.setViewPlayer(new ViewProxy(new ViewMock(),"NOT INIT",turnStateController));
        gameManager.getTiles().get(1).get(0).addPlayer(enemy);
        gameManager.getPlayerOrderTurn().add(placeholderPlayer);
        gameManager.getPlayerOrderTurn().add(enemy);
        gameManager.setCurrentPlayer(placeholderPlayer);
        List<Weapon> weapons = new ArrayList<>();
        for(int i = 0; i < 21; i++) {
            weapons.add(gameManager.getWeaponsDeck().draw());
        }
        gameManager.getCurrentTurn().setCurrentPlayerForVisibility(placeholderPlayer);
        weapons.get(0).beforeBaseEffect(gameManager, placeholderPlayer);
        assertEquals(TurnState.CHOOSE_TARGET, placeholderPlayer.getTurnState());
        placeholderPlayer.setTurnState(TurnState.READY_FOR_ACTION);
        weapons.get(3).beforeBaseEffect(gameManager, placeholderPlayer);
        assertEquals(TurnState.CHOOSE_TYPE_OF_EFFECT, placeholderPlayer.getTurnState());

        weapons.get(0).baseEffectExecute(gameManager, placeholderPlayer);
        assertEquals(placeholderPlayer, gameManager.getCurrentTurn().getCurrentPlayerForVisibility());
        assertEquals(weapons.get(0).getBaseEffect().getActions().get(0), gameManager.getCurrentTurn().getCurrentAction());
        assertEquals(TurnState.CHOOSE_TARGET, placeholderPlayer.getTurnState());

        weapons.get(11).baseEffectExecute(gameManager, placeholderPlayer);
        assertEquals(placeholderPlayer, gameManager.getCurrentTurn().getCurrentPlayerForVisibility());
        assertEquals(weapons.get(11).getBaseEffect().getActions().get(0), gameManager.getCurrentTurn().getCurrentAction());
        assertEquals(TurnState.CHOOSE_DIRECTION, placeholderPlayer.getTurnState());
        //set a direction and test afterChooseDirectionAction
        gameManager.getCurrentTurn().setDirection(Direction.EAST);
        weapons.get(11).baseEffectExecute(gameManager, placeholderPlayer);
        assertEquals(TurnState.CHOOSE_TARGET, placeholderPlayer.getTurnState());
        weapons.get(11).alternativeEffectExecute(gameManager, placeholderPlayer);
        weapons.get(8).baseEffectExecute(gameManager, placeholderPlayer);
        assertEquals(TurnState.CHOOSE_ROOM, placeholderPlayer.getTurnState());
        weapons.get(5).baseEffectExecute(gameManager, placeholderPlayer);
        gameManager.getCurrentTurn().setCurrentWeapon(weapons.get(1));
        gameManager.getCurrentTurn().pushEffect(weapons.get(1).getBaseEffect(), weapons.get(1).getBaseEffect().getActions());
        gameManager.getCurrentTurn().getAlreadyHitPlayer().put(weapons.get(1).getBaseEffect(), new ArrayList<>());
        assertFalse(weapons.get(1).getOptionalEffect().get(0).isCanBeUsed());
        assertFalse(weapons.get(1).getOptionalEffect().get(1).isCanBeUsed());
        placeholderPlayer.getAmmo().setYellowValue(2);
        placeholderPlayer.getAmmo().setBlueValue(2);
        placeholderPlayer.getAmmo().setRedValue(2);
        weapons.get(1).afterBaseEffect(gameManager, placeholderPlayer);
        System.out.println(weapons.get(1).getIdName());

        assertEquals(TurnState.CHOOSE_EFFECT, placeholderPlayer.getTurnState());

        weapons.get(2).baseEffectExecute(gameManager, placeholderPlayer);
        //set a player hitten by baseEffect
        //change the turnstate just to test
        placeholderPlayer.setTurnState(TurnState.READY_FOR_ACTION);
        Player dummy = new Player("Dummy", PlayerColor.YELLOW, PlayerState.NORMAL, gameManager.getTiles().get(0).get(0), baseTurnAction);
        dummy.setViewPlayer(new ViewProxy(new ViewMock(),"NOT INIT",turnStateController));
        gameManager.getTiles().get(0).get(0).addPlayer(dummy);
        gameManager.getCurrentTurn().getAlreadyHitPlayer().put(weapons.get(2).getBaseEffect(), Collections.singletonList(dummy));
        weapons.get(2).optionalEffectExecute(weapons.get(2).getOptionalEffect().get(0), gameManager, placeholderPlayer);
        assertFalse(weapons.get(2).getOptionalEffect().get(1).isCanBeUsed());

        gameManager.getTiles().get(0).get(0).removePlayer(dummy);
        gameManager.getWeaponsDeck().addToPile(weapons);
    }

    @Test
    void isAValidEffectTest() {
        Player placeholderPlayer = new Player("Placeholder", PlayerColor.YELLOW, PlayerState.NORMAL, gameManager.getTiles().get(0).get(0), baseTurnAction);
        placeholderPlayer.setAmmo(new Ammo(3,3,3));
        placeholderPlayer.setViewPlayer(new ViewProxy(new ViewMock(), "NOT INIT",turnStateController));
        Player pl1 = new Player("Player1", PlayerColor.GREEN, PlayerState.NORMAL, gameManager.getTiles().get(0).get(1), baseTurnAction);
        pl1.setViewPlayer(new ViewProxy(new ViewMock(), "NOT INIT",turnStateController));
        gameManager.getTiles().get(0).get(1).addPlayer(pl1);
        gameManager.getTiles().get(0).get(0).addPlayer(placeholderPlayer);
        List<Weapon> weapons = new ArrayList<>();
        for(int i = 0; i < 21; i++) {
            weapons.add(gameManager.getWeaponsDeck().draw());
        }
        assertTrue(weapons.get(0).isAValidEffect(weapons.get(0).getBaseEffect(), gameManager, placeholderPlayer));
        placeholderPlayer.setCurrentTile(gameManager.getTiles().get(2).get(3));
        assertFalse(weapons.get(0).isAValidEffect(weapons.get(0).getBaseEffect(), gameManager, placeholderPlayer));
        placeholderPlayer.setCurrentTile(gameManager.getTiles().get(0).get(0));
        assertTrue(weapons.get(3).isAValidEffect(weapons.get(3).getOptionalEffect().get(0), gameManager, placeholderPlayer));

        gameManager.getCurrentTurn().getAlreadyHitPlayer().put(weapons.get(1).getBaseEffect(), Collections.singletonList(placeholderPlayer));
        weapons.get(1).afterBaseEffect(gameManager, placeholderPlayer);
        assertEquals(2, weapons.get(1).getPossibleOptionalEffects(gameManager, placeholderPlayer).size());

        gameManager.getWeaponsDeck().addToPile(weapons);
    }
}
