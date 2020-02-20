import controller.TurnStateController;
import model.board.*;
import model.enums.*;
import model.gamemodes.BoardStructure;
import model.gamemodes.FinalFrenzyGameMode;
import model.gamemodes.Gamemodes;
import model.player.Player;
import model.powerup.PowerUp;
import model.powerup.PowerUpEffect;
import model.utility.Ammo;
import model.utility.MapInfo;
import model.utility.TurnStateHandler;
import model.weapon.InitWeapons;
import model.weapon.Weapon;
import network.ViewProxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


public class FinalFrenzyTest {

        private GameManager gameManager;
        private static TurnStateController turnStateController;

        final String fileToParse = "fileToParse/";
        final String powFile = fileToParse + "powerUpToParse.txt";
        final String ammoFile = fileToParse + "ammoTilesToParse.txt";
        final String mapFile = fileToParse + "map.txt";
        final String pointsFile = fileToParse + "points.txt";
        final String weaponFile = fileToParse + "weaponsToParse.txt";
        final String turnStateFile = fileToParse + "state_commands.txt";
        private MapInfo map;
        private KillShotTrack killShotTrack;


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
            killShotTrack = new KillShotTrack(Arrays.asList(scores), 1, new ArrayList<>(), 1);
            Gamemodes gamemodes = new Gamemodes(new BoardStructure(pointsFile));
            gameManager = new GameManager(new Deck<Weapon>(InitWeapons.initAllWeapons(weaponFile)), new Deck<PowerUp>(InitMap.initPowerUps(powFile)),
                    new Deck<AmmoTile>(InitMap.initAmmoTiles(ammoFile)), players, allMaps.get(0).getMap(), gamemodes.getNormalMode(), killShotTrack,
                    true, gamemodes, InitMap.initMap(mapFile).get(0).getMapWidth(), InitMap.initMap(mapFile).get(0).getMapHeight());
            for(Player p : gameManager.getPlayerOrderTurn()) {
                p.setCurrentTurnAction(gameManager.getCurrentGameMode().getBaseTurnAction());
            }
            gameManager.setNotifyObservers(views);
            gamemodes.getNormalMode().getEndObserver().setGameManager(gameManager);
            gamemodes.getFinalFrenzyMode().getEndObserver().setGameManager(gameManager);
            for(Player p : gameManager.getPlayerOrderTurn()) {
                p.setCurrentTurnAction(gameManager.getCurrentGameMode().getBaseTurnAction());
                p.setBoard(new PointsBoard(Arrays.asList(scores),1));
            }
            turnStateController = new TurnStateController(turnStateFile);
            for(Player p:players) {
                p.setViewPlayer(new ViewProxy(new ViewMock(), "ciao",turnStateController));
                views.add(p.getViewPlayer());
            }
        }


    private void fillSetUp(){
        Weapon w;
        List<Tile> spawnTiles=new ArrayList<>();
        while(true) {
            w = gameManager.getWeaponsDeck().draw();
            if (w.getIdName().equals("LOCK_RIFLE"))
                break;
            gameManager.getWeaponsDeck().addToDiscardPile(w);
            gameManager.getWeaponsDeck().reShuffleAll();
        }
        spawnTiles= gameManager.getSpawnTiles();
        for(Tile t:spawnTiles)
            if(t.getRoom()== RoomColor.RED)
                t.addWeapon(w);
        spawnTiles.clear();
        while (true) {
            w = gameManager.getWeaponsDeck().draw();
            if (w.getIdName().equals("ELECTROSCYTHE"))
                break;
            gameManager.getWeaponsDeck().addToDiscardPile(w);
            gameManager.getWeaponsDeck().reShuffleAll();
        }
        spawnTiles = gameManager.getSpawnTiles();
        for (Tile t : spawnTiles)
            if (t.getRoom() == RoomColor.RED)
                t.addWeapon(w);
        while (true) {
            w = gameManager.getWeaponsDeck().draw();
            if (w.getIdName().equals("SHOCKWAVE"))
                break;
            gameManager.getWeaponsDeck().addToDiscardPile(w);
            gameManager.getWeaponsDeck().reShuffleAll();
        }
        spawnTiles.clear();
        spawnTiles = gameManager.getSpawnTiles();
        for (Tile t : spawnTiles)
            if (t.getRoom() == RoomColor.RED)
                t.addWeapon(w);
        PowerUp powerUp= new PowerUp(AmmoColor.RED,PowerUpUse.SEPARATED,"Teletrasporto",new PowerUpEffect(new Ammo(0,0,0),1,0,100,0,false,false,false));
        PowerUp powerUp1= new PowerUp(AmmoColor.RED,PowerUpUse.SEPARATED,"Teletrasporto",new PowerUpEffect(new Ammo(0,0,0),1,0,100,0,false,false,false));
        PowerUp powerUp2= new PowerUp(AmmoColor.RED,PowerUpUse.SEPARATED,"Teletrasporto",new PowerUpEffect(new Ammo(0,0,0),1,0,100,0,false,false,true));
        PowerUp powerUp3= new PowerUp(AmmoColor.RED,PowerUpUse.SEPARATED,"Teletrasporto",new PowerUpEffect(new Ammo(0,0,0),1,0,100,0,false,false,false));
        PowerUp powerUp4= new PowerUp(AmmoColor.RED,PowerUpUse.SEPARATED,"Teletrasporto",new PowerUpEffect(new Ammo(0,0,0),1,0,100,0,false,false,false));
        gameManager.getPowerUpDeck().removeAll();
        gameManager.getPowerUpDeck().addToDiscardPile(powerUp);
        gameManager.getPowerUpDeck().addToDiscardPile(powerUp1);
        gameManager.getPowerUpDeck().addToDiscardPile(powerUp2);
        gameManager.getPowerUpDeck().addToDiscardPile(powerUp3);
        gameManager.getPowerUpDeck().addToDiscardPile(powerUp4);
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
        void finalFrenzyTurnTest(){
            fillSetUp();
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
            assertEquals(11,enemy2.getDamageTaken().size());
            p.addDamageTaken(enemy2,1,gameManager);
            p.setCurrentTile(gameManager.getTiles().get(0).get(0));
            gameManager.getTiles().get(0).get(0).addPlayer(p);
            enemy2.setCurrentTile(gameManager.getTiles().get(0).get(2));
            gameManager.getTiles().get(0).get(2).addPlayer(enemy2);
            enemy1.setCurrentTile(gameManager.getTiles().get(0).get(1));
            gameManager.getTiles().get(0).get(1).addPlayer(enemy1);
            p.grabActionAfterTile(gameManager,gameManager.getTiles().get(0).get(0));
            p.grabActionAfterTile(gameManager,gameManager.getTiles().get(0).get(1));
            gameManager.spawnSetPosition(enemy2,enemy2.getPowerUps().get(0));
            assertEquals(p.getPlayerColor(),killShotTrack.getDeathOrder().get(0));
            assertTrue(gameManager.getCurrentGameMode() instanceof FinalFrenzyGameMode);
            assertEquals(3,p.getCurrentTurnAction().getMovementBeforeGrabbing());
            assertEquals(2,p.getCurrentTurnAction().getMovementBeforeShooting());
            assertEquals(true,p.getCurrentTurnAction().isReloadBeforeShooting());
            assertEquals(1,p.getCurrentTurnAction().getMaxNOfActions());
            assertEquals(0,p.getCurrentTurnAction().getOnlyMovement());
            assertEquals(2,enemy2.getCurrentTurnAction().getMovementBeforeGrabbing());
            assertEquals(1,enemy2.getCurrentTurnAction().getMovementBeforeShooting());
            assertEquals(true,enemy2.getCurrentTurnAction().isReloadBeforeShooting());
            assertEquals(2,enemy2.getCurrentTurnAction().getMaxNOfActions());
            assertEquals(4,enemy2.getCurrentTurnAction().getOnlyMovement());
            assertEquals(0,enemy2.getBoard().getPointsFirstBlood());
            assertEquals(2,enemy2.getBoard().getPoints().get(0));
            assertEquals(1,p.getBoard().getPointsFirstBlood());
            assertEquals(8,p.getBoard().getPoints().get(0));
            gameManager.getCurrentTurn().reset();
            gameManager.changeTurn();
            p.addDamageTaken(enemy2,11,gameManager);
            p.setPlayerState(PlayerState.DEAD);
            gameManager.getCurrentTurn().addDeadPlayer(p);
            gameManager.changeTurn();
            gameManager.spawnSetPosition(p,p.getPowerUps().get(0));
            assertEquals(gameManager.getCurrentPlayerTurn().getPlayerID(),p.getPlayerID());
            assertEquals(0,p.getBoard().getPointsFirstBlood());
            assertEquals(2,p.getBoard().getPoints().get(0));
            FinalFrenzyGameMode finalFrenzyGameMode=(FinalFrenzyGameMode) gameManager.getCurrentGameMode();
            assertEquals(finalFrenzyGameMode.getnFrenzyTurn(),2);
            p.setCurrentTile(gameManager.getTiles().get(1).get(0));
            gameManager.getTiles().get(1).get(0).addPlayer(p);
            enemy2.setCurrentTile(gameManager.getTiles().get(0).get(2));
            gameManager.getTiles().get(0).get(2).addPlayer(enemy2);
            enemy1.setCurrentTile(gameManager.getTiles().get(0).get(1));
            gameManager.getTiles().get(0).get(1).addPlayer(enemy1);
            assertEquals(3,p.getCurrentTile().getWeapons().size());
            p.grabActionAfterTile(gameManager,p.getCurrentTile());
            p.pickWeapon(p.getCurrentTile().getWeapons().get(0),gameManager);
            assertEquals("LOCK_RIFLE",p.getWeapons().get(0).getIdName());
            p.setAmmo(new Ammo(3,3,3));
            p.getWeapons().get(0).setLoaded(LoadedState.UNLOADED);
            assertTrue( p.isValidShoot(gameManager));
            p.chooseTileShootingAction(gameManager);
            assertTrue(gameManager.getCurrentTurn().getPossibleChoices().getSelectableTiles().contains(p.getCurrentTile()));
            p.shootingActionAfterTile(p.getCurrentTile(),gameManager);
            p.addPowerUp(gameManager,new PowerUp(AmmoColor.BLUE,PowerUpUse.SEPARATED,"Teletrasporto",new PowerUpEffect(new Ammo(0,0,0),1,0,100,0,false,false,false)));
            assertEquals(TurnState.CHOOSE_WEAPON_TO_RELOAD_MANDATORY,p.getTurnState());
            assertFalse(turnStateController.getValidCommands(gameManager,p).contains("nothing"));
            gameManager.getCurrentTurn().setNOfActionMade(0);
            p.startReloading(gameManager,p.getWeapons().get(0));
            assertEquals(TurnState.DISCARD_POWERUP_FOR_COST_WEAPON_IN_ACTION,p.getTurnState());
            p.discardPowerUpForAmmos(p.getPowerUps().get(0),gameManager);
            TurnStateHandler.handleAfterDiscardPowerUp(p,gameManager);
            assertEquals(TurnState.CHOOSE_WEAPON_HAND,p.getTurnState());
            gameManager.changeTurn();
        }
}
