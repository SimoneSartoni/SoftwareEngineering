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
import model.weapon.actions.DamageAction;
import model.weapon.actions.MovementAction;
import network.ViewProxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import view.cli.MapDrawing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ParticularWeaponTest {

    private GameManager gameManager;

    final String fileToParse = "fileToParse/";
    final String powFile = fileToParse + "powerUpToParse.txt";
    final String ammoFile = fileToParse + "ammoTilesToParse.txt";
    final String mapFile = fileToParse + "map.txt";
    final String pointsFile = fileToParse + "points.txt";
    final String weaponFile = fileToParse + "weaponsToParse.txt";
    final String commandsFile = fileToParse + "state_commands.txt";
    private MapInfo map;
    private KillShotTrack killShotTrack;
    private static TurnStateController turnStateController;

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
        PowerUp powerUp= new PowerUp(AmmoColor.RED, PowerUpUse.SEPARATED,"Teletrasporto",new PowerUpEffect(new Ammo(0,0,0),1,0,100,0,false,false,false));
        PowerUp powerUp1= new PowerUp(AmmoColor.RED,PowerUpUse.SEPARATED,"Teletrasporto",new PowerUpEffect(new Ammo(0,0,0),1,0,100,0,false,false,false));
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
    void cannoneVortexWeaponTest(){
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
        //Player enemy3=gameManager.getPlayerOrderTurn().get(3);
        gameManager.getPlayerOrderTurn().get(1).setCurrentTile(gameManager.getTiles().get(0).get(0));
        gameManager.getTiles().get(0).get(0).addPlayer(gameManager.getPlayerOrderTurn().get(1));
        gameManager.getTiles().get(0).get(2).addPlayer(gameManager.getPlayerOrderTurn().get(2));
        gameManager.getPlayerOrderTurn().get(2).setCurrentTile(gameManager.getTiles().get(0).get(2));
       // gameManager.getPlayerOrderTurn().get(3).setCurrentTile(gameManager.getTiles().get(0).get(0));
        //gameManager.getTiles().get(0).get(0).addPlayer(gameManager.getPlayerOrderTurn().get(3));
        gameManager.getPlayerOrderTurn().get(1).pickWeapon(gameManager.getTiles().get(1).get(0).getWeapons().get(1),gameManager);
        Player player = gameManager.getCurrentPlayerTurn();
        player.setAmmo(new Ammo(3,3,3));
        gameManager.spawnDrawPhase(player, 2);
        assertEquals(2, player.getPowerUps().size());
        assertEquals(player.getTurnState(), TurnState.DISCARD_POWERUP_FOR_SPAWN);
        gameManager.spawnSetPosition(player, player.getPowerUps().get(0));
        assertEquals(1, player.getPowerUps().size());
        assertEquals(3, gameManager.getCurrentTurn().getPossibleChoices().getSelectableActions().size());
        assertEquals(RoomColor.RED, player.getCurrentTile().getRoom());
        player.chooseTileGrabAction(gameManager);
        player.grabActionAfterTile(gameManager, player.getCurrentTile());
        player.pickWeapon(player.getCurrentTile().getWeapons().get(0), gameManager);
        assertEquals(1, player.getWeapons().size());
        assertTrue(player.isValidShoot(gameManager));
        Weapon w=player.getWeapons().get(0);
        player.startShootingWithAWeapon(gameManager,w);
        assertEquals(1,w.getPossibleTypeOfEffects(gameManager,player).size());
        assertEquals(TurnState.CHOOSE_TARGET,player.getTurnState());
        assertTrue(gameManager.getCurrentTurn().getPossibleChoices().getSelectableTargets().contains(enemy1));
        assertTrue(gameManager.getCurrentTurn().getPossibleChoices().getSelectableTargets().contains(enemy2));
        assertFalse(gameManager.getCurrentTurn().getPossibleChoices().getSelectableTargets().contains(player));
        w.getBaseEffect().afterChooseTargetAction((MovementAction)w.getBaseEffect().getActions().get(0),gameManager,player,enemy1);
        assertEquals(TurnState.CHOOSE_TILE_FOR_WEAPON_ACTION,player.getTurnState());
        assertTrue(gameManager.getCurrentTurn().getPossibleChoices().getSelectableTiles().contains(gameManager.getTiles().get(0).get(0)));
        assertTrue(gameManager.getCurrentTurn().getPossibleChoices().getSelectableTiles().contains(gameManager.getTiles().get(1).get(0)));
        assertTrue(gameManager.getCurrentTurn().getPossibleChoices().getSelectableTiles().contains(gameManager.getTiles().get(0).get(1)));
        assertEquals(3,gameManager.getCurrentTurn().getPossibleChoices().getSelectableTiles().size());
        w.getBaseEffect().afterChooseTileAction((MovementAction)w.getBaseEffect().getActions().get(0),gameManager,player,gameManager.getTiles().get(0).get(1));
        assertEquals(1,gameManager.getCurrentTurn().getPossibleChoices().getSelectableOptionalEffects().size());
        w.optionalEffectExecute(w.getOptionalEffect().get(0),gameManager,player);
        player.payCost(gameManager,w.getOptionalEffect().get(0).getCost());
        w.getOptionalEffect().get(0).startingExecution(w.getOptionalEffect().get(0).getActions().get(0),gameManager,player);
        assertEquals(TurnState.CHOOSE_TARGET,player.getTurnState());
        assertTrue(gameManager.getCurrentTurn().getPossibleChoices().getSelectableTargets().contains(enemy2));
        assertFalse(gameManager.getCurrentTurn().getPossibleChoices().getSelectableTargets().contains(enemy1));
        assertFalse(gameManager.getCurrentTurn().getPossibleChoices().getSelectableTargets().contains(player));
        w.getOptionalEffect().get(0).afterChooseTargetAction((MovementAction)w.getOptionalEffect().get(0).getActions().get(0),gameManager,player,enemy2);
        assertEquals(gameManager.getTiles().get(0).get(1),enemy2.getCurrentTile());
        assertNotEquals(TurnState.CHOOSE_TARGET,player.getTurnState());
       //assertTrue(gameManager.getCurrentTurn().getPossibleChoices().getSelectableTargets().contains(enemy3));
      //  assertFalse(gameManager.getCurrentTurn().getPossibleChoices().getSelectableTargets().contains(enemy1));
       // assertFalse(gameManager.getCurrentTurn().getPossibleChoices().getSelectableTargets().contains(player));
       // assertFalse(gameManager.getCurrentTurn().getPossibleChoices().getSelectableTargets().contains(enemy2));
    //    w.getOptionalEffect().get(0).afterChooseTargetAction((MovementAction) w.getOptionalEffect().get(0).getActions().get(2),gameManager,player,enemy3);
        assertEquals(1,enemy2.getDamageTaken().size());
      //  assertEquals(1,enemy3.getDamageTaken().size());
        MapDrawing.drawMap(MapInfo.createMapView(map),killShotTrack);
    }


    @Test
    void lanciarazziWeaponTest(){
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
        gameManager.getTiles().get(0).get(2).addPlayer(gameManager.getPlayerOrderTurn().get(2));
        gameManager.getPlayerOrderTurn().get(2).setCurrentTile(gameManager.getTiles().get(0).get(2));
        gameManager.getPlayerOrderTurn().get(1).pickWeapon(gameManager.getTiles().get(1).get(0).getWeapons().get(1),gameManager);
        Player player = gameManager.getCurrentPlayerTurn();
        player.setAmmo(new Ammo(3,3,3));
        gameManager.spawnDrawPhase(player, 2);
        assertEquals(2, player.getPowerUps().size());
        assertEquals(player.getTurnState(), TurnState.DISCARD_POWERUP_FOR_SPAWN);
        gameManager.spawnSetPosition(player, player.getPowerUps().get(0));
        assertEquals(1, player.getPowerUps().size());
        assertEquals(3, gameManager.getCurrentTurn().getPossibleChoices().getSelectableActions().size());
        assertEquals(RoomColor.RED, player.getCurrentTile().getRoom());
        player.chooseTileGrabAction(gameManager);
        player.grabActionAfterTile(gameManager, player.getCurrentTile());
        player.pickWeapon(player.getCurrentTile().getWeapons().get(2), gameManager);
        assertEquals(1, player.getWeapons().size());
        Weapon w=player.getWeapons().get(0);
        player.loadWeaponGrabbed(w,gameManager,gameManager.getCurrentTurn().getCurrentWeaponToDrop());
        assertTrue(player.isValidShoot(gameManager));
        MapDrawing.drawMap(MapInfo.createMapView(map),killShotTrack);
        player.startShootingWithAWeapon(gameManager,w);
        assertEquals(TurnState.CHOOSE_TYPE_OF_EFFECT,player.getTurnState());
        assertTrue(gameManager.getCurrentTurn().getPossibleChoices().getSelectableEffects().contains(TypeOfEffect.BASE));
        assertTrue(gameManager.getCurrentTurn().getPossibleChoices().getSelectableEffects().contains(TypeOfEffect.OPTIONAL));
        w.baseEffectExecute(gameManager,player);
        assertEquals(TurnState.CHOOSE_TARGET,player.getTurnState());
        assertTrue(gameManager.getCurrentTurn().getPossibleChoices().getSelectableTargets().contains(enemy2));
        assertTrue(gameManager.getCurrentTurn().getPossibleChoices().getSelectableTargets().contains(enemy1));
        w.getBaseEffect().afterChooseTargetAction((DamageAction)w.getBaseEffect().getActions().get(0),gameManager,player,enemy1);
        assertEquals(TurnState.CHOOSE_LINKED_EFFECT,player.getTurnState());
        assertTrue(gameManager.getCurrentTurn().getPossibleChoices().getSelectableOptionalEffects().contains(w.getOptionalEffect().get(1)));
        w.optionalEffectExecute(w.getOptionalEffect().get(1),gameManager,player);
        assertEquals(TurnState.CHOOSE_TILE_FOR_WEAPON_ACTION,player.getTurnState());
        assertTrue(gameManager.getCurrentTurn().getPossibleChoices().getSelectableTiles().contains(gameManager.getTiles().get(0).get(0)));
        assertEquals(1,gameManager.getCurrentTurn().getPossibleChoices().getSelectableTiles().size());
        w.getOptionalEffect().get(1).afterChooseTileAction((DamageAction)w.getOptionalEffect().get(1).getActions().get(0), gameManager,player,gameManager.getTiles().get(0).get(0));
        assertEquals(TurnState.CHOOSE_TILE_FOR_WEAPON_ACTION,player.getTurnState());
        assertTrue(gameManager.getCurrentTurn().getPossibleChoices().getSelectableTiles().contains(gameManager.getTiles().get(0).get(1)));
        assertTrue(gameManager.getCurrentTurn().getPossibleChoices().getSelectableTiles().contains(gameManager.getTiles().get(0).get(0)));
        assertTrue(gameManager.getCurrentTurn().getPossibleChoices().getSelectableTiles().contains(gameManager.getTiles().get(1).get(0)));
    }





}
