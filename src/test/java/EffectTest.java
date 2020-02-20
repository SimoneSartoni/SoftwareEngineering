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
import model.utility.MapInfoView;
import model.utility.Visibility;
import model.weapon.Effect;
import model.weapon.InitWeapons;
import model.weapon.Weapon;
import model.weapon.actions.Action;
import model.weapon.actions.DamageAction;
import model.weapon.actions.MarkAction;
import model.weapon.actions.MovementAction;
import network.ViewProxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import view.cli.MapDrawing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EffectTest {

    private static GameManager gameManager;
    private static List<Player> players;
    private static MapInfo map;
    private KillShotTrack killShotTrack;
    private TurnStateController turnStateController;

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


    @BeforeEach
    void before() {
        final String fileToParse = "fileToParse/";
        final String powFile = fileToParse + "powerUpToParse.txt";
        final String ammoFile = fileToParse + "ammoTilesToParse.txt";
        final String mapFile = fileToParse + "map.txt";
        final String pointsFile = fileToParse + "points.txt";
        final String weaponFile = fileToParse + "weaponsToParse.txt";
        final String commandsFile = fileToParse + "state_commands.txt";
        players= new ArrayList<>();
        players.add(new Player("a", PlayerColor.GREEN, PlayerState.NORMAL, null, null));
        players.add(new Player("b", PlayerColor.GREY,PlayerState.NORMAL, null, null));
        players.add(new Player("c", PlayerColor.BLUE,PlayerState.NORMAL, null, null));
        players.add(new Player("d", PlayerColor.PURPLE,PlayerState.NORMAL, null, null));
        players.add(new Player("e",PlayerColor.YELLOW,PlayerState.NORMAL, null, null));
        players.get(4).setCurrentTurnAction(null);
        assertEquals(5,players.size());
        killShotTrack=new KillShotTrack(new ArrayList<>(),0,new ArrayList<>(),0);
        Gamemodes gamemodes = new Gamemodes(new BoardStructure(pointsFile));
        List<MapInfo> allMaps = InitMap.initMap(mapFile);
        map = new MapInfo(0, 0, "sudden death",
                allMaps.get(0).getMapWidth(), allMaps.get(0).getMapHeight(),
                allMaps.get(0).getMap(), allMaps.get(0).getAllowedPlayerColors(), allMaps.get(0).getAllowedEndModes(),
                allMaps.get(0).getMaxNumberOfPlayer(), allMaps.get(0).getMinNumberOfPlayer(), new ArrayList<>());
        gameManager = new GameManager(new Deck<Weapon>(InitWeapons.initAllWeapons(weaponFile)), new Deck<PowerUp>(InitMap.initPowerUps(powFile)),
                new Deck<AmmoTile>(InitMap.initAmmoTiles(ammoFile)), players, allMaps.get(0).getMap(),
                gamemodes.getNormalMode(), killShotTrack, false, gamemodes,
                InitMap.initMap(mapFile).get(0).getMapWidth(), InitMap.initMap(mapFile).get(0).getMapHeight());
        gameManager.setNotifyObservers(new ArrayList<>());
        setTiles(gameManager);
        gamemodes.getNormalMode().getEndObserver().setGameManager(gameManager);
        for(Player p : gameManager.getPlayerOrderTurn()) {
            p.setCurrentTurnAction(gameManager.getCurrentGameMode().getBaseTurnAction());
        }
        turnStateController= new TurnStateController(commandsFile);
        for(Player p:players)
            p.setViewPlayer(new ViewProxy(new ViewMock(),"0",turnStateController));
    }

    @Test
    void effectTest() {
        List<Weapon> weapons = new ArrayList<>();
        for(int i = 0; i < 21; i++) {
            weapons.add(gameManager.getWeaponsDeck().draw());
        }
        Player player = gameManager.getPlayerOrderTurn().get(0);
        player.setViewPlayer(new ViewProxy(new ViewMock(), "NOT INIT",turnStateController));
        gameManager.setCurrentPlayer(player);
        gameManager.getCurrentTurn().setCurrentPlayerForVisibility(player);
        weapons.get(11).getBaseEffect().startAction((DamageAction) weapons.get(11).getBaseEffect().getActions().get(0), gameManager, player);
        assertEquals(TurnState.CHOOSE_DIRECTION, player.getTurnState());
        gameManager.getCurrentTurn().pushEffect(weapons.get(0).getBaseEffect(),weapons.get(0).getBaseEffect().getActions());
        weapons.get(0).getBaseEffect().startAction((MarkAction) weapons.get(0).getBaseEffect().getActions().get(1), gameManager, player);
        assertEquals(TurnState.CHOOSE_TARGET, player.getTurnState());
        weapons.get(0).getBaseEffect().afterChooseDirectionAction((MarkAction) weapons.get(0).getBaseEffect().getActions().get(1), gameManager, Direction.SOUTH, player);
        assertEquals(TurnState.CHOOSE_TARGET, player.getTurnState());

        weapons.get(10).getBaseEffect().startAction((MarkAction) weapons.get(10).getBaseEffect().getActions().get(1), gameManager, player);
        assertEquals(TurnState.CHOOSE_TILE_FOR_WEAPON_ACTION, player.getTurnState());
        weapons.get(10).getBaseEffect().afterChooseDirectionAction((MarkAction) weapons.get(10).getBaseEffect().getActions().get(1), gameManager, Direction.SOUTH, player);
        assertEquals(TurnState.CHOOSE_TILE_FOR_WEAPON_ACTION, player.getTurnState());

        weapons.get(18).getBaseEffect().startAction((MovementAction) weapons.get(18).getBaseEffect().getActions().get(0), gameManager, player);
        assertEquals(TurnState.CHOOSE_TARGET, player.getTurnState());
        weapons.get(18).getBaseEffect().afterChooseDirectionAction((MovementAction) weapons.get(18).getBaseEffect().getActions().get(0), gameManager, Direction.SOUTH, player);
        assertEquals(TurnState.CHOOSE_TARGET, player.getTurnState());
        gameManager.getCurrentTurn().pushEffect(weapons.get(18).getBaseEffect(), weapons.get(18).getBaseEffect().getActions());
        gameManager.getCurrentTurn().setCurrentWeapon(weapons.get(18));
        gameManager.getCurrentTurn().getAlreadyHitPlayer().put(weapons.get(18).getBaseEffect(),new ArrayList<>());
        gameManager.getCurrentTurn().getAlreadyHitTile().put(weapons.get(18).getBaseEffect(),new ArrayList<>());
        weapons.get(18).getBaseEffect().afterChooseTargetAction((MovementAction) weapons.get(18).getBaseEffect().getActions().get(0), gameManager, player, gameManager.getPlayerOrderTurn().get(1));
        assertEquals(TurnState.CHOOSE_TARGET, player.getTurnState());
        gameManager.getCurrentTurn().setCurrentWeapon(weapons.get(17));
        gameManager.getCurrentTurn().setCurrentPlayerForVisibility(player);
        gameManager.getCurrentTurn().pushEffect(weapons.get(17).getBaseEffect(),weapons.get(17).getBaseEffect().getActions());
        gameManager.getCurrentTurn().getAlreadyHitPlayer().put(weapons.get(17).getBaseEffect(),new ArrayList<>());
        weapons.get(17).getBaseEffect().getAlreadyInteractedPlayers().add(new ArrayList<>());
        gameManager.getWeaponsDeck().addToPile(weapons);
    }

    @Test
    void otherTest() {
        List<Weapon> weapons = new ArrayList<>();
        for(int i = 0; i < 21; i++) {
            weapons.add(gameManager.getWeaponsDeck().draw());
        }
        Player player = new Player("Player", PlayerColor.GREEN, PlayerState.NORMAL, gameManager.getTiles().get(1).get(3), gameManager.getCurrentGameMode().getBaseTurnAction());
        player.setViewPlayer(new ViewProxy(new ViewMock(), "NOT INIT",turnStateController));
        gameManager.getTiles().get(1).get(3).addPlayer(player);
        Player enemy = new Player("Enemy", PlayerColor.YELLOW, PlayerState.NORMAL, gameManager.getTiles().get(1).get(2), gameManager.getCurrentGameMode().getBaseTurnAction());
        enemy.setViewPlayer(new ViewProxy(new ViewMock(), "NOT INIT",turnStateController));
        gameManager.getTiles().get(1).get(2).addPlayer(enemy);

        //take the torpedine
        Weapon torpedine = weapons.get(2);
        gameManager.getCurrentTurn().getAlreadyHitPlayer().put(torpedine.getBaseEffect(), Collections.singletonList(player));
        torpedine.getOptionalEffect().get(0).setCanBeUsed(true);
        MapInfoView mapView = MapInfo.createMapView(map);
        MapDrawing.drawMap(mapView,killShotTrack);
        assertTrue(torpedine.getOptionalEffect().get(0).isValid(gameManager, player));

        //take the onda d'urto
        Weapon onda = weapons.get(19);
        gameManager.getCurrentTurn().getAlreadyHitPlayer().put(onda.getBaseEffect(), Collections.singletonList(player));
        gameManager.getCurrentTurn().getAlreadyHitTile().put(onda.getBaseEffect(), Collections.singletonList(gameManager.getTiles().get(1).get(2)));

        //take the raggio traente
        Weapon raggio = weapons.get(6);
        assertTrue(raggio.getBaseEffect().isValid(gameManager, player));

        //effect with straight forward visibility and movement actions
        MovementAction mov1 = new MovementAction(1, 2, true, ForcedMovement.NOT_RESTRICTED, null, null, null, false);
        MovementAction mov2 = new MovementAction(1, 2, false, ForcedMovement.NOT_RESTRICTED, null, null, null, false);
        MovementAction mov3 = new MovementAction(1, 2, false, ForcedMovement.FORCED_TO_PLAYER, null, null, null, false);
        MovementAction mov4 = new MovementAction(1, 2, true, ForcedMovement.FORCED_TO_PLAYER, null, null, null, false);
        MarkAction mark1 = new MarkAction(1,1,false,1,2,null, null ,null,false);
        MarkAction mark1Aoe = new MarkAction(1,1,true,1,2,null, null ,null,false);
        List<Action> actions = new ArrayList<>();
        actions.add(mov1);
        actions.add(mov2);
        actions.add(mov3);
        actions.add(mov4);
        actions.add(mark1);
        actions.add(mark1Aoe);
        Visibility visibilityStraight = new Visibility(false, false, false, true, false, false, false,false, false,1, 2, 0, 0);
        Visibility visibility = new Visibility(true, false, false, false, false, false, false,false,false, 1, 2, 0, 0);
        Visibility visibilityAnotherRoom = new Visibility(false, false, true, false, false, false, false,false,false, 1, 2, 0, 0);
        Effect fakeEffect1 = new Effect("fake1", false, actions, true, 0, 0, null, visibilityStraight, new Ammo(0,0,0), 0, true, false,false);
        Effect fakeEffect2 = new Effect("fake2", false, actions, true, 0, 0, null, visibility, new Ammo(0,0,0), 0, true, false,false);
        Effect fakeEffect3 = new Effect("fake3", false, actions, true, 0, 0, null, visibilityAnotherRoom, new Ammo(0,0,0), 0, true, false,false);
        assertEquals("fake1", fakeEffect1.getName());
        assertEquals(true, fakeEffect1.isNewTarget());
        assertEquals(0, fakeEffect1.getNOfOptionalTargets());
        assertEquals(0, fakeEffect1.getAlreadyTarget());
        Ammo a = new Ammo(0,0,0);
        assertEquals(a, fakeEffect1.getCost());
        assertEquals(0, fakeEffect1.getChainedToAction());
        assertEquals(false, fakeEffect1.isThroughWalls());
        assertEquals(0, fakeEffect1.getAlreadyInteractedPlayers().size());

        gameManager.getCurrentTurn().setCurrentPlayerForVisibility(player);
        gameManager.setCurrentPlayer(player);
        gameManager.getCurrentTurn().setCurrentWeapon(new Weapon(fakeEffect1, null, new ArrayList<>(), 1, null, null, "Fake Weapon", LoadedState.LOADED));
        gameManager.getCurrentTurn().pushEffect(fakeEffect1,fakeEffect1.getActions());
        fakeEffect1.startAction(mov1, gameManager, player);
        player.setTurnState(TurnState.READY_FOR_ACTION);

        //movement test
        fakeEffect1.startAction(mov3, gameManager, player);

        //movement - chosed direction
        gameManager.getCurrentTurn().setDirection(Direction.WEST);
        fakeEffect1.startAction(mov1, gameManager, player);
        player.setTurnState(TurnState.READY_FOR_ACTION);

        //movement - change visibility
        gameManager.getCurrentTurn().setCurrentWeapon(new Weapon(fakeEffect1, null, new ArrayList<>(), 1, null, null, "Fake Weapon", LoadedState.LOADED));
        fakeEffect2.startAction(mov3, gameManager, player);
        assertTrue(gameManager.getCurrentTurn().getPossibleChoices().getSelectableTargets().isEmpty());

        //movement - afterChooseTarget
        gameManager.getCurrentTurn().setDirection(Direction.WEST);
        fakeEffect1.afterChooseTargetAction(mov1, gameManager, player, enemy);
        assertEquals(player.getTurnState(), TurnState.CHOOSE_TILE_FOR_WEAPON_ACTION);
        gameManager.getCurrentTurn().setDirection(null);
        //movement start action
        gameManager.getCurrentTurn().getPossibleChoices().clear();
        gameManager.getCurrentTurn().setCurrentWeapon(new Weapon(fakeEffect1, null, new ArrayList<>(), 1, null, null, "name", LoadedState.LOADED));
        gameManager.getCurrentTurn().getCurrentWeapon().getBaseEffect().setExecuted(true);

        fakeEffect1.startAction(mov2, gameManager, player);
        gameManager.getCurrentTurn().setCurrentPlayerForVisibility(enemy);

        gameManager.getCurrentTurn().getPossibleChoices().clear();
        fakeEffect1.startAction(mov2, gameManager, enemy);
        gameManager.getCurrentTurn().setCurrentPlayerForVisibility(player);

        gameManager.getCurrentTurn().pushEffect(fakeEffect2, fakeEffect2.getActions());
        fakeEffect2.afterChooseTargetAction(mov4, gameManager, player, enemy);
        assertFalse(gameManager.getTiles().get(1).get(2).getPlayers().contains(enemy));
        assertTrue(player.getCurrentTile().getPlayers().contains(enemy));
        assertEquals(player.getCurrentTile(), enemy.getCurrentTile());

        gameManager.getCurrentTurn().setDirection(Direction.WEST);
        //not targetting enemy
        fakeEffect1.afterChooseTargetAction(mov3, gameManager, player, player);
        assertEquals(gameManager.getTiles().get(1).get(3), player.getCurrentTile());
        fakeEffect1.afterChooseTargetAction(mov2, gameManager, player, player);
        assertEquals(player.getTurnState(), TurnState.CHOOSE_TILE_FOR_WEAPON_ACTION);
        fakeEffect2.afterChooseTargetAction(mov3, gameManager, player, player);
        assertEquals(gameManager.getTiles().get(1).get(3), player.getCurrentTile());
        fakeEffect2.afterChooseTargetAction(mov2, gameManager, player, player);
        assertEquals(player.getTurnState(), TurnState.CHOOSE_TILE_FOR_WEAPON_ACTION);
        //restore positions for other test
        enemy.setCurrentTile(gameManager.getTiles().get(1).get(2));
        player.getCurrentTile().removePlayer(enemy);
        gameManager.getTiles().get(1).get(2).addPlayer(enemy);
        gameManager.getCurrentTurn().setDirection(null);

        //movement - afterChooseTile
        gameManager.getCurrentTurn().setChosenTarget(enemy);
        gameManager.getCurrentTurn().setCurrentWeapon(new Weapon(fakeEffect2, null, new ArrayList<>(), 1, null, null, "name", LoadedState.LOADED));
        gameManager.getCurrentTurn().getCurrentWeapon().getBaseEffect().setExecuted(true);
        fakeEffect2.afterChooseTileAction(mov1, gameManager, player, player.getCurrentTile());
        //assertEquals(enemy.getCurrentTile(), player.getCurrentTile());
        //assertTrue(player.getCurrentTile().getPlayers().contains(enemy));
        //assertFalse(gameManager.getTiles().get(1).get(2).getPlayers().contains(enemy));
        fakeEffect2.afterChooseTileAction(mov2, gameManager, player, gameManager.getTiles().get(2).get(2));
        //assertEquals(gameManager.getTiles().get(2).get(2), player.getCurrentTile());
        //assertTrue(gameManager.getTiles().get(2).get(2).getPlayers().contains(player));
        //assertFalse(gameManager.getTiles().get(1).get(3).getPlayers().contains(player));
        //restore positions for other test
        player.setCurrentTile(gameManager.getTiles().get(1).get(3));
        gameManager.getTiles().get(1).get(3).addPlayer(player);
        gameManager.getTiles().get(2).get(2).removePlayer(player);
        enemy.setCurrentTile(gameManager.getTiles().get(1).get(2));
        player.getCurrentTile().removePlayer(enemy);
        gameManager.getTiles().get(1).get(2).addPlayer(enemy);


        //mark tests
        fakeEffect1.startAction(mark1, gameManager, player);
        assertEquals(1, gameManager.getCurrentTurn().getPossibleChoices().getSelectableDirections().size());
        assertEquals(player.getTurnState(), TurnState.CHOOSE_DIRECTION);
        gameManager.getCurrentTurn().setDirection(Direction.WEST);
        fakeEffect1.startAction(mark1, gameManager, player);
        assertEquals(player.getTurnState(), TurnState.CHOOSE_TARGET);
        //mark - aoe
        fakeEffect1.startAction(mark1Aoe, gameManager, player);
        assertEquals(player.getTurnState(), TurnState.CHOOSE_TILE_FOR_WEAPON_ACTION);
        //mark - anotherRoom
        fakeEffect3.startAction(mark1, gameManager, player);
        assertEquals(2, gameManager.getCurrentTurn().getPossibleChoices().getSelectableTiles().size());
        assertEquals(player.getTurnState(), TurnState.CHOOSE_ROOM);

        //movement directions test
        gameManager.getCurrentTurn().setCurrentPlayerForVisibility(enemy);
        fakeEffect1.afterChooseDirectionAction(mov2, gameManager, Direction.SOUTH, enemy);
        //fakeEffect1.afterChooseDirectionAction(mov2, gameManager, Direction.NORTH, enemy);
        //fakeEffect1.afterChooseDirectionAction(mov2, gameManager, Direction.EAST, enemy);
        //fakeEffect1.afterChooseDirectionAction(mov2, gameManager, Direction.WEST, enemy);
        gameManager.getCurrentTurn().setCurrentPlayerForVisibility(player);
        gameManager.getCurrentTurn().pushEffect(fakeEffect1,fakeEffect1.getActions());
        fakeEffect1.afterChooseDirectionAction(mov2, gameManager, Direction.SOUTH, player);
        //fakeEffect1.afterChooseDirectionAction(mov2, gameManager, Direction.NORTH, player);
        //fakeEffect1.afterChooseDirectionAction(mov2, gameManager, Direction.EAST, player);
        //fakeEffect1.afterChooseDirectionAction(mov2, gameManager, Direction.WEST, player);
        gameManager.getCurrentTurn().setNOfActionMade(0);
        gameManager.getCurrentTurn().setDirection(null);
        gameManager.getTiles().get(1).get(3).removePlayer(player);
        gameManager.getTiles().get(1).get(2).removePlayer(enemy);
        gameManager.getCurrentTurn().getAlreadyHitPlayer().clear();
        torpedine.getOptionalEffect().get(0).setCanBeUsed(false);
        gameManager.getWeaponsDeck().addToPile(weapons);
    }

    @Test
    void afterPowerUpTest() {
        gameManager.getCurrentTurn().setNOfActionMade(0);
        while(gameManager.getCurrentTurn().topEffect() != null)
            gameManager.getCurrentTurn().popEffect();
        List<Weapon> weapons = new ArrayList<>();
        for(int i = 0; i < 21; i++) {
              weapons.add(gameManager.getWeaponsDeck().draw());
        }
        Player player = new Player("Player", PlayerColor.GREEN, PlayerState.NORMAL, gameManager.getTiles().get(1).get(3), gameManager.getCurrentGameMode().getBaseTurnAction());
        gameManager.getTiles().get(1).get(3).addPlayer(player);
        player.setTurnState(TurnState.READY_FOR_ACTION);
        Player enemy = new Player("Enemy", PlayerColor.YELLOW, PlayerState.NORMAL, gameManager.getTiles().get(1).get(2), gameManager.getCurrentGameMode().getBaseTurnAction());
        gameManager.getTiles().get(1).get(2).addPlayer(enemy);
        gameManager.getCurrentTurn().setCurrentPlayerForVisibility(player);
        gameManager.setCurrentPlayer(player);

        DamageAction dam1 = new DamageAction(1,1,false, 0, 5, null, null, null,false);
        List<Action> actions = new ArrayList<>();
        actions.add(dam1);
        Visibility visibility = new Visibility(true, false, false, false, false, false, false,false,false, 1, 2, 0, 0);
        Effect fakeEffect1 = new Effect("fake1", false, actions, true, 0, 0, null, visibility, new Ammo(0,0,0), 0, true, false,false);
        Weapon fakeWeapon = new Weapon(fakeEffect1, null, new ArrayList<>(), 0, new Ammo(0,0,0), new Ammo(0,0,0),
                "Fake_Weapon", LoadedState.LOADED);
        gameManager.getCurrentTurn().setCurrentWeapon(fakeWeapon);
        gameManager.getCurrentTurn().pushEffect(fakeEffect1, fakeEffect1.getActions());

        PowerUp powerUp = new PowerUp(AmmoColor.RED, PowerUpUse.AFTER_DAMAGE, "Pow",
                new PowerUpEffect(new Ammo(0,0,0), 1, 0, 0, 0, false, true,true));
        player.drawPowerUp(gameManager,powerUp);
        player.drawPowerUp(gameManager,powerUp);
        player.setTurnState(TurnState.CHOOSE_AFTER_DAMAGE_POWERUP);
        gameManager.getCurrentTurn().setCurrentAction(dam1);
        gameManager.getCurrentTurn().pushEffect(fakeEffect1, fakeEffect1.getActions());
        assertEquals(TurnState.CHOOSE_AFTER_DAMAGE_POWERUP, player.getTurnState());

        gameManager.getTiles().get(1).get(3).removePlayer(player);
        gameManager.getTiles().get(1).get(2).removePlayer(enemy);
        gameManager.getWeaponsDeck().addToPile(weapons);
    }

    @Test
    void dummyTest() {
        DamageAction damage = new DamageAction(1,1,false, 1,1, null, null, null,false);
        MarkAction mark = new MarkAction(1, 2, true, 1, 2, damage, null, null,false);
        MovementAction movementAction = new MovementAction(1,1,true, ForcedMovement.FORCED_TO_PLAYER, null, null, null, false);
        assertEquals(1, damage.getMinAmount());
        assertEquals(ForcedMovement.NO_MOVEMENT, damage.getToTile());
        assertFalse(damage.isOptional());
        assertFalse(damage.isActionExecuted());
        assertNull(damage.getLinkedEffect());
        assertNull(damage.getHitInTheEffect());
        assertNull(damage.getLinkedToNext());
        assertEquals(1, mark.getMinAmount());
        assertEquals(ForcedMovement.NO_MOVEMENT, mark.getToTile());
        assertFalse(mark.isOptional());
        assertFalse(mark.isActionExecuted());
        assertNull(mark.getLinkedEffect());
        assertNull(mark.getHitInTheEffect());
        assertEquals(damage, mark.getLinkedToNext());
        assertFalse(movementAction.isMandatoryChoice());
        assertFalse(movementAction.isActionExecuted());
        assertNull(movementAction.getHitInTheEffect());
    }
}
