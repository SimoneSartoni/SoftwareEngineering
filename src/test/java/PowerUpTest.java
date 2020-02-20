import controller.TurnStateController;
import model.board.AmmoTile;
import model.board.Deck;
import model.board.GameManager;
import model.board.InitMap;
import model.enums.*;
import model.gamemodes.BoardStructure;
import model.gamemodes.Gamemodes;
import model.player.Player;
import model.powerup.PowerUp;
import model.powerup.PowerUpEffect;
import model.utility.Ammo;
import model.weapon.InitWeapons;
import model.weapon.Weapon;
import network.ViewProxy;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


class PowerUpTest {

    private static GameManager gameManager;
    private static TurnStateController turnStateController;

    private static void setTiles(GameManager gameManager) {
        gameManager.getPlayerOrderTurn().get(0).setCurrentTile(gameManager.getTiles().get(1).get(1));
        gameManager.getTiles().get(1).get(1).addPlayer(gameManager.getPlayerOrderTurn().get(0));
        gameManager.getPlayerOrderTurn().get(1).setCurrentTile(gameManager.getTiles().get(0).get(1));
        gameManager.getTiles().get(0).get(1).addPlayer(gameManager.getPlayerOrderTurn().get(1));
        gameManager.getPlayerOrderTurn().get(2).setCurrentTile(gameManager.getTiles().get(2).get(1));
        gameManager.getTiles().get(2).get(1).addPlayer(gameManager.getPlayerOrderTurn().get(2));
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
        List<Player> players = new ArrayList<>();
        Gamemodes gamemodes = new Gamemodes(new BoardStructure(pointsFile));
        players.add(new Player("a", PlayerColor.GREEN, PlayerState.NORMAL, null, null));
        players.add(new Player("b", PlayerColor.GREY,PlayerState.NORMAL, null, null));
        players.add(new Player("c", PlayerColor.BLUE,PlayerState.NORMAL, null, null));
        players.add(new Player("d", PlayerColor.PURPLE,PlayerState.NORMAL, null, null));
        players.add(new Player("e",PlayerColor.YELLOW,PlayerState.NORMAL, null, null));
        gameManager = new GameManager(new Deck<Weapon>(InitWeapons.initAllWeapons(weaponFile)), new Deck<PowerUp>(InitMap.initPowerUps(powFile)),
                new Deck<AmmoTile>(InitMap.initAmmoTiles(ammoFile)), players, InitMap.initMap(mapFile).get(0).getMap(), gamemodes.getNormalMode(),
                null, false, gamemodes, InitMap.initMap(mapFile).get(0).getMapWidth(), InitMap.initMap(mapFile).get(0).getMapHeight());
        setTiles(gameManager);
        for(Player p : gameManager.getPlayerOrderTurn()) {
            p.setCurrentTurnAction(gameManager.getCurrentGameMode().getBaseTurnAction());
        }
        turnStateController= new TurnStateController(commandsFile);
    }

    @Test
    void executionTest() {
        Player player = gameManager.getPlayerOrderTurn().get(0);
        player.setViewPlayer(new ViewProxy(new ViewMock(), "ciao",new TurnStateController()));
        Player other = gameManager.getPlayerOrderTurn().get(1);
        List<PowerUp> powerUps = new ArrayList<>();
        for(int i = 0; i < 24; i++) {
            powerUps.add(gameManager.getPowerUpDeck().draw());
        }
        PowerUp pow1 = powerUps.get(6);
        PowerUp pow2 = powerUps.get(19);

        int damage = player.getDamageTaken().size();
        pow1.startEffect(gameManager, player);
        assertEquals(damage, player.getDamageTaken().size());
        assertEquals(TurnState.CHOOSE_POWERUP_TARGET, player.getTurnState());

        pow2.startEffect(gameManager, player);

        player.setTurnState(TurnState.READY_FOR_ACTION);

        PowerUp pow3 = powerUps.get(0);
        Weapon weapon = gameManager.getWeaponsDeck().draw();
        gameManager.getCurrentTurn().pushEffect(weapon.getBaseEffect(), weapon.getBaseEffect().getActions());
        gameManager.getCurrentTurn().getAlreadyHitPlayer().put(weapon.getBaseEffect(), new ArrayList<>());
        pow3.startEffect(gameManager, player);
        assertEquals(TurnState.CHOOSE_POWERUP_TARGET, player.getTurnState());

        PowerUp fakePow1 = new PowerUp(AmmoColor.RED, PowerUpUse.AFTER_DAMAGE, "FakePow1",
                new PowerUpEffect(new Ammo(0,0,0), 1, 0, 0, 1, false, true,false));

        fakePow1.startEffect(gameManager, player);
        assertEquals(TurnState.CHOOSE_POWERUP_TARGET, player.getTurnState());

        gameManager.setCurrentPlayer(other);
        PowerUp fakePow2 = new PowerUp(AmmoColor.RED, PowerUpUse.AFTER_TAKEN_DAMAGE, "FakePow2",
                new PowerUpEffect(new Ammo(0,0,0), 2, 0, 1, 1, false, true,false));
        fakePow2.startEffect(gameManager, player);
        assertEquals(fakePow2.getPowerUpEffect().getDmg(), other.getDamageTaken().size());
        assertEquals(player, other.getDamageTaken().get(0));
        assertEquals(player, other.getDamageTaken().get(1));
        //assertEquals(fakePow2.getPowerUpEffect().getMarks(), other.getMarks().size());
        //assertEquals(player, other.getMarks().get(player));

        gameManager.getPowerUpDeck().addToPile(powerUps);
    }

}
