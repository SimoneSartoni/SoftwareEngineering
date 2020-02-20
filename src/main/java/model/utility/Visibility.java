package model.utility;

import model.board.GameManager;
import model.board.Tile;
import model.enums.Direction;
import model.enums.RoomColor;
import model.enums.TileLinks;
import model.player.Player;
import model.weapon.Effect;
import model.weapon.actions.Action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Visibility implements Serializable {
    private boolean canSee;
    private boolean cannotSee;
    private boolean anotherRoom;
    private boolean straightForward;
    private boolean everywhere;
    private boolean chainSee;
    private boolean differentTiles;
    private int minMov;
    private int maxMov;
    private int maxDistanceToVisibleTiles;
    private int minDistanceToVisibleTiles;
    private boolean isChainedToTake;
    private boolean rangeDist;

    public Visibility() {

    }

    /**
     * constructor of the class
     * @param canSee true if player can hit visible players (see game rules for more details)
     * @param cannotSee true if player can hit not visible players
     * @param anotherRoom true if the targets are the rooms
     * @param straightForward true if the effect is on a specific direction
     * @param everywhere true if al the players can be selected (other constraints need to be applied)
     * @param chainSee true if the visibility of the effect starts not from the current player but from the player hit in previous effect
     * @param differentTiles true if effect can't hit players in the same tile
     * @param rangeDist true if effect hits all the players at a set range (visibility range)
     * @param isChainedToTake true if the current player for visibility can be re-selected
     * @param minMov the minimum movement for valid visibility of the effect
     * @param maxMov the maximum movement for valid visibility of the effect
     * @param maxDistanceToVisibleTiles the maximum movement that can be applied to a player in order to make him visible
     * @param minDistanceToVisibleTiles the minimum movement that can be applied to a player in order to make him visible
     */
    public Visibility(boolean canSee, boolean cannotSee, boolean anotherRoom, boolean straightForward, boolean everywhere,
                      boolean chainSee, boolean differentTiles,boolean rangeDist,boolean isChainedToTake, int minMov, int maxMov, int maxDistanceToVisibleTiles, int minDistanceToVisibleTiles) {
        this.isChainedToTake=isChainedToTake;
        this.canSee = canSee;
        this.cannotSee = cannotSee;
        this.anotherRoom = anotherRoom;
        this.straightForward = straightForward;
        this.everywhere = everywhere;
        this.chainSee = chainSee;
        this.minMov = minMov;
        this.maxMov = maxMov;
        this.differentTiles = differentTiles;
        this.maxDistanceToVisibleTiles = maxDistanceToVisibleTiles;
        this.minDistanceToVisibleTiles = minDistanceToVisibleTiles;
        this.rangeDist=rangeDist;
    }

    public boolean isCanSee() {
        return canSee;
    }

    public boolean isRangeDist() {
        return rangeDist;
    }

    public boolean isCannotSee() {
        return cannotSee;
    }

    public boolean isAnotherRoom() {
        return anotherRoom;
    }

    public boolean isStraightForward() {
        return straightForward;
    }

    public boolean isEverywhere() {
        return everywhere;
    }

    public boolean isChainSee() {
        return chainSee;
    }

    public boolean isDifferentTiles() {
        return differentTiles;
    }

    public int getMinMov() {
        return minMov;
    }

    public int getMaxMov() {
        return maxMov;
    }

    public boolean isChainedToTake() {
        return isChainedToTake;
    }

    public int getMaxDistanceToVisibleTiles() {
        return maxDistanceToVisibleTiles;
    }

    public int getMinDistanceToVisibleTiles() {
        return minDistanceToVisibleTiles;
    }


    /**
     * main method of this classes. It checks all the visibility constraints to check if there are possible players that can be
     * targetted by the effect
     * @param gameManager
     * @param player the current player for visibility
     * @param effect the current effect
     * @return the list of players that can be targeted. This before more specific constraints about actions.
     */
    public List<Player> getTargettablePlayers(GameManager gameManager, Player player, Effect effect){
        List<Player> targettablePlayers = new ArrayList<>();
        List<Player> unTargettablePlayers;

        if(isChainSee()) {
            targettablePlayers.addAll(gameManager.getVisiblePlayers(player));
            targettablePlayers.remove(gameManager.getCurrentPlayerTurn());
            if(isChainedToTake)
                targettablePlayers.add(player);
        }
        if(isCanSee()) {
            targettablePlayers = gameManager.getVisiblePlayers(player);
            targettablePlayers.remove(gameManager.getCurrentPlayerTurn());
        }
        if(isAnotherRoom()){
            targettablePlayers.addAll(gameManager.getVisiblePlayers(player));
            List<Player> temp=new ArrayList<>(targettablePlayers);
            targettablePlayers.remove(gameManager.getCurrentPlayerTurn());
            for(Player p:temp)
                if(p.getCurrentTile().getRoom().equals(player.getCurrentTile().getRoom()))
                    targettablePlayers.remove(p);
        }
        if(isRangeDist()){
             targettablePlayers.addAll(gameManager.getOnBoardPlayers());
             targettablePlayers.remove(gameManager.getCurrentPlayerTurn());
            }
        if(isCannotSee()) {
            targettablePlayers = gameManager.getOnBoardPlayers();
            targettablePlayers.remove(gameManager.getCurrentPlayerTurn());
            unTargettablePlayers = gameManager.getVisiblePlayers(player);
            targettablePlayers.removeAll(unTargettablePlayers);
        }
        if(isEverywhere()) {
            targettablePlayers.addAll(gameManager.getOnBoardPlayers());
            targettablePlayers=removeNotValidPlayers(gameManager,targettablePlayers,player,minMov,maxMov,effect);
            targettablePlayers.remove(gameManager.getCurrentPlayerTurn());
        }
        if(isStraightForward()){
            targettablePlayers=getDirectionPlayers(Direction.SOUTH,player,gameManager, effect);
            targettablePlayers.addAll(getDirectionPlayers(Direction.NORTH,player,gameManager, effect));
            targettablePlayers.addAll(getDirectionPlayers(Direction.EAST,player,gameManager, effect));
            targettablePlayers.addAll(getDirectionPlayers(Direction.WEST,player,gameManager, effect));
        }
        if(!effect.isNewTarget()){
            for(Effect e :gameManager.getCurrentTurn().getAlreadyHitPlayer().keySet())
                    targettablePlayers.addAll(gameManager.getCurrentTurn().getAlreadyHitPlayer().get(e));
            return targettablePlayers;
        }
        if (effect.getChainedTo()!=null && effect.getAlreadyTarget()>0&&gameManager.getCurrentTurn().getAlreadyHitPlayer().containsKey(effect.getChainedTo())){
            for(Player p: gameManager.getCurrentTurn().getAlreadyHitPlayer().get(effect.getChainedTo()))
                if(!targettablePlayers.contains(p))
                    targettablePlayers.add(p);
        }
        return targettablePlayers;
    }

    /**
     * this method returns the directions that have possible players to hit
     * @param player the current player for visibility
     * @param gameManager
     * @param effect the current effect
     * @return the selectable directions
     */
    public List<Direction> getTargettableDirections(Player player, GameManager gameManager, Effect effect){
        List<Direction> ret= new ArrayList<>();
        if(!getDirectionPlayers(Direction.NORTH,player,gameManager, effect).isEmpty())
            ret.add(Direction.NORTH);
        if(!getDirectionPlayers(Direction.SOUTH,player,gameManager, effect).isEmpty())
            ret.add(Direction.SOUTH);
        if(!getDirectionPlayers(Direction.EAST,player,gameManager, effect).isEmpty())
            ret.add(Direction.EAST);
        if(!getDirectionPlayers(Direction.WEST,player,gameManager, effect).isEmpty())
            ret.add(Direction.WEST);
        return ret;
    }

    /**
     * private method to make getDirectionPlayers more readable
     * */
    public List<Player> eastComputation(Tile currentTile, GameManager gameManager, Player player, Effect effect) {
        List<Player> ret = new ArrayList<>();
        while (currentTile.getY()<gameManager.getTiles().get(0).size()){
            for (Player p: currentTile.getPlayers()){
                if(!p.equals(player))
                    ret.add(p); }
            if((currentTile.getCanRight()==TileLinks.ENDOFMAP)||(currentTile.getCanRight()==TileLinks.HOLE)||((currentTile.getCanRight()==TileLinks.WALL)&&(!effect.isThroughWalls()))){
                break; }
            else {
                currentTile = gameManager.getTiles().get(currentTile.getX()).get(currentTile.getY() + 1); }
        }
        return ret;
    }
    /**
     * private method to make getDirectionPlayers more readable
     * */
    public List<Player> westComputation(Tile currentTile, GameManager gameManager, Player player, Effect effect) {
        List<Player> ret = new ArrayList<>();
        while (currentTile.getY()>=0){
            for (Player p: currentTile.getPlayers()){
                if(!p.equals(player)){
                    ret.add(p); }
            }
            if((currentTile.getCanLeft()==TileLinks.ENDOFMAP)||(currentTile.getCanLeft()==TileLinks.HOLE)||((currentTile.getCanLeft()==TileLinks.WALL)&&(!effect.isThroughWalls()))){
                break; }
            else{
                currentTile=gameManager.getTiles().get(currentTile.getX()).get(currentTile.getY()-1); }
        }
        return ret;
    }
    /**
     * private method to make getDirectionPlayers more readable
     * */
    public List<Player> northComputation(Tile currentTile, GameManager gameManager, Player player, Effect effect) {
        List<Player> ret = new ArrayList<>();
        while (currentTile.getX()>=0){
            for (Player p: currentTile.getPlayers()) {
                if (!p.equals(player)) {
                    ret.add(p); }
            }
            if((currentTile.getCanUp()==TileLinks.ENDOFMAP)||(currentTile.getCanUp()==TileLinks.HOLE)||((currentTile.getCanUp()==TileLinks.WALL)&&(!effect.isThroughWalls()))){
                break;}
            else{
                currentTile=gameManager.getTiles().get(currentTile.getX()-1).get(currentTile.getY());}
        }
        return ret;
    }
    /**
     * private method to make getDirectionPlayers more readable
     * */
    public List<Player> southComputation(Tile currentTile, GameManager gameManager, Player player, Effect effect) {
        List<Player> ret = new ArrayList<>();
        while (currentTile.getX()<gameManager.getTiles().size()){
            for (Player p: currentTile.getPlayers()) {
                if (!p.equals(player)) {
                    ret.add(p);
                }
            }
            if(((currentTile.getCanDown()==TileLinks.ENDOFMAP)||(currentTile.getCanDown()==TileLinks.HOLE))||((currentTile.getCanDown()==TileLinks.WALL)&&(!effect.isThroughWalls()))){
                break;
            }
            else {
                currentTile = gameManager.getTiles().get(currentTile.getX() + 1).get(currentTile.getY()); }
        }
        return ret;
    }

    public List<Player> getDirectionPlayers(Direction direction,Player player,GameManager gameManager, Effect effect){
        List<Player> ret=new ArrayList<>();
        Tile currentTile=player.getCurrentTile();
        switch (direction){
            case EAST:
                ret.addAll(eastComputation(currentTile, gameManager, player, effect));
                break;
            case WEST:
                ret.addAll(westComputation(currentTile, gameManager, player, effect));
                break;
            case NORTH:
                ret.addAll(northComputation(currentTile, gameManager, player, effect));
                break;
            case SOUTH:
                ret.addAll(southComputation(currentTile, gameManager, player, effect));
                break;
            default: break;
        }
        return ret;
    }

    public List<Direction> getPossibleDirections(GameManager gameManager, Player player, int minMov, int maxMov){
        List<Direction> ret = new ArrayList<>();
        if(!getDirectionTiles(gameManager, player, minMov, maxMov, Direction.SOUTH).isEmpty())
            ret.add(Direction.SOUTH);
        if(!getDirectionTiles(gameManager, player, minMov, maxMov, Direction.NORTH).isEmpty())
            ret.add(Direction.NORTH);
        if(!getDirectionTiles(gameManager, player, minMov, maxMov, Direction.EAST).isEmpty())
            ret.add(Direction.EAST);
        if(!getDirectionTiles(gameManager, player, minMov, maxMov, Direction.WEST).isEmpty())
            ret.add(Direction.WEST);
        return ret;
    }


    public List<RoomColor>getTargettableRoomColors(Player player, List<Player> targettable){
        List<RoomColor> roomColors = new ArrayList<>();
        for(Player player1 : targettable)
            if( ! player1.getCurrentTile().getRoom().equals(player.getCurrentTile().getRoom()) )
                if( ! roomColors.contains(player1.getCurrentTile().getRoom()))
                    roomColors.add(player1.getCurrentTile().getRoom());
        return roomColors;
    }

    /**
     * private method to make getDirectionTiles more readable
     * */
    public static List<Tile> southTileComputation(GameManager gameManager, Tile current, int minMov, int maxMov) {
        List<Tile> ret = new ArrayList<>();
        Tile startingTile = current;
        if (current.getX() + minMov >= gameManager.getBoardHeight())
            return new ArrayList<>();
        else {
            for (int i = 0; i <= minMov; i++) {
                if (startingTile.getCanDown() != TileLinks.DOOR && startingTile.getCanDown() != TileLinks.NEAR)
                    return new ArrayList<>();
                startingTile = gameManager.getTiles().get(current.getX() + i).get(current.getY());
            }
        }
        for (int i = minMov; startingTile.getX() < gameManager.getBoardHeight() && i <= maxMov; i++) {
            ret.add(startingTile);
            if (startingTile.getCanDown() == TileLinks.DOOR || startingTile.getCanDown() == TileLinks.NEAR) {
                if (startingTile.getX() < gameManager.getBoardHeight())
                    startingTile = gameManager.getTiles().get(startingTile.getX() + 1).get(startingTile.getY());
            } else
                break;
        }
        return ret;
    }

    /**
     * private method to make getDirectionTiles more readable
     * */
    public static List<Tile> northTileComputation(GameManager gameManager, Tile current, int minMov, int maxMov) {
        List <Tile> ret = new ArrayList<>();
        Tile startingTile = current;
        if(current.getX() - minMov < 0)
            return new ArrayList<>();
        else {
            for(int i = 0; i <= minMov; i++) {
                if(startingTile.getCanUp() != TileLinks.DOOR && startingTile.getCanUp() != TileLinks.NEAR)
                    return new ArrayList<>();
                startingTile = gameManager.getTiles().get(current.getX() - i).get(current.getY());
            }
        }
        for(int i = minMov; startingTile.getX()>= 0 && i <= maxMov; i++) {
            ret.add(startingTile);
            if(startingTile.getCanUp() == TileLinks.DOOR || startingTile.getCanUp() == TileLinks.NEAR) {
                if(startingTile.getX()>0)
                    startingTile = gameManager.getTiles().get(startingTile.getX() - 1).get(startingTile.getY());
            }
            else break;
        }
        return ret;
    }

    /**
     * private method to make getDirectionTiles more readable
     * */
    public static List<Tile> eastTileComputation(GameManager gameManager, Tile current, int minMov, int maxMov) {
        List <Tile> ret = new ArrayList<>();
        Tile startingTile = current;
        if(current.getY() + minMov >= gameManager.getBoardLength())
            return new ArrayList<>();
        else {
            for(int i = 0; i <= minMov; i++) {
                if(startingTile.getCanRight() != TileLinks.DOOR && startingTile.getCanRight() != TileLinks.NEAR)
                    return new ArrayList<>();
                startingTile = gameManager.getTiles().get(current.getX()).get(current.getY() + i);
            }
        }
        for(int i =minMov; startingTile.getY() < gameManager.getBoardLength() && i <= maxMov; i++) {
            ret.add(startingTile);
            if(startingTile.getCanRight() == TileLinks.DOOR || startingTile.getCanRight() == TileLinks.NEAR) {
                if(startingTile.getY()<gameManager.getBoardLength()-1)
                    startingTile = gameManager.getTiles().get(startingTile.getX()).get(startingTile.getY() + 1);
            }
            else break;
        }
        return ret;
    }

    /**
     * private method to make getDirectionTiles more readable
     * */
    public static List<Tile> westTileComputation(GameManager gameManager, Tile current, int minMov, int maxMov) {
        List <Tile> ret = new ArrayList<>();
        Tile startingTile = current;
        if(current.getY() - minMov < 0)
            return new ArrayList<>();
        else {
            for(int i = 0; i <= minMov; i++) {
                if(startingTile.getCanLeft() != TileLinks.DOOR && startingTile.getCanLeft() != TileLinks.NEAR)
                    return new ArrayList<>();
                startingTile = gameManager.getTiles().get(current.getX()).get(current.getY() - i);
            }
        }
        for(int i = minMov; startingTile.getY()>= 0 && i <= maxMov; i++) {
            ret.add(startingTile);
            if(startingTile.getCanLeft() == TileLinks.DOOR || startingTile.getCanLeft() == TileLinks.NEAR) {
                if(startingTile.getY()>0)
                    startingTile = gameManager.getTiles().get(startingTile.getX()).get(startingTile.getY() - 1);
            }
            else break;
        }

        return ret;
    }

    /**
     * method that return the valid tiles in a direction
     * @param gameManager
     * @param player the current player for visibility
     * @param minMov the min movement before the first valid tile
     * @param maxMov the max movement for a valid tile
     * @param direction the selected direction
     * @return the valid tiles in a direction
     */
    public static List<Tile> getDirectionTiles(GameManager gameManager,Player player, int minMov,int maxMov,Direction direction){
        List<Tile> ret=new ArrayList<>();
        Tile current = player.getCurrentTile();

        switch (direction) {
            case SOUTH:
                ret = southTileComputation(gameManager, current, minMov, maxMov);
                break;
            case NORTH:
                ret = northTileComputation(gameManager, current, minMov, maxMov);
                break;
            case WEST:
                ret = westTileComputation(gameManager, current, minMov, maxMov);
                break;
            case EAST:
                ret = eastTileComputation(gameManager, current, minMov, maxMov);
                break;
            default:
                break;
        }

        return ret;
    }

    /**
     * methods that return the targettable tiles starting from the targettable players
     * @param player the current player for visibility
     * @param targettable the list of targettable players
     * @return the selectable tiles
     */
    public List<Tile> getTargettableTiles( Player player,List<Player> targettable){
        List<Tile> targettableTiles = new ArrayList<>();
        for(Player player1 : targettable) {
            if (!targettableTiles.contains(player1.getCurrentTile())) {
                targettableTiles.add(player1.getCurrentTile());
            }
        }
        return targettableTiles;
    }

    /**
     * this method removes the non valid players for the current effect or action, related to the parameters of the visibility and of the restricted visibility
     * in the effect/action
     * @param gameManager
     * @param targettablePlayers the currently selectable players
     * @param currentPlayer the current player for visibility
     * @param minDistance min distance at which shooting is valid
     * @param maxDistance max distance at which shooting is valid
     * @param effect the related effect
     * @return the effectively selectable players
     */
    public List<Player> removeNotValidPlayers(GameManager gameManager, List<Player> targettablePlayers, Player currentPlayer, int minDistance, int maxDistance, Effect effect) {
        int distance = 0;
        boolean toRemove = true;
        Tile temp;
        List<Player> support = new ArrayList<>(targettablePlayers);
        if ((effect.isNewTarget())&&(effect.getChainedTo() != null) && (effect.getAlreadyTarget()==0)) {
            for(Effect e: gameManager.getCurrentTurn().getAlreadyHitPlayer().keySet())
                    targettablePlayers.removeAll(gameManager.getCurrentTurn().getAlreadyHitPlayer().get(e));
        }
        if(effect.isNewTarget()&&effect.getChainedTo()!=null &&effect.getAlreadyTarget()>0) {
            for (Effect e : gameManager.getCurrentTurn().getAlreadyHitPlayer().keySet())
                if (!e.equals(effect) && !e.equals(effect.getChainedTo()))
                    for (Player p : gameManager.getCurrentTurn().getAlreadyHitPlayer().get(e))
                        targettablePlayers.remove(p);
        }
        if (isEverywhere()) {
            for (Player player1 : support) {
                distance = gameManager.getDistanceBetweenTiles(currentPlayer.getCurrentTile(), player1.getCurrentTile());
                if ((distance < minMov) || (distance > maxMov))
                    targettablePlayers.remove(player1);
            }
            support=new ArrayList<>(targettablePlayers);
            for (Player p : support) {
                temp = p.getCurrentTile();
                temp.removePlayer(p);
                for (Tile t : gameManager.getPossibleTiles(minDistanceToVisibleTiles, maxDistanceToVisibleTiles, p.getCurrentTile())) {
                    p.setCurrentTile(t);
                    t.addPlayer(p);
                    if (gameManager.getVisiblePlayers(currentPlayer).contains(p)) {
                        toRemove = false;
                        t.removePlayer(p);
                        p.setCurrentTile(temp);
                        break;
                    }
                    t.removePlayer(p);
                    p.setCurrentTile(temp);
                }
                p.setCurrentTile(temp);
                temp.addPlayer(p);
                if (toRemove)
                    targettablePlayers.remove(p);
                toRemove=true;
            }
            return targettablePlayers;
        }
        for (Player player1 : support) {
            distance = gameManager.getDistanceBetweenTiles(currentPlayer.getCurrentTile(), player1.getCurrentTile());
            if ((distance < minDistance) || (distance > maxDistance))
                targettablePlayers.remove(player1);
        }
        if(effect.isNewTarget()) {
            if (!isDifferentTiles()) {
                if (gameManager.getCurrentTurn().getAlreadyHitPlayer().get(effect) != null)
                    targettablePlayers.removeAll(gameManager.getCurrentTurn().getAlreadyHitPlayer().get(effect));
            } else {
                if (gameManager.getCurrentTurn().getAlreadyHitPlayer().get(effect) != null) {
                    for (Tile t : gameManager.getCurrentTurn().getAlreadyHitTile().get(effect)) {
                        targettablePlayers.removeAll(t.getPlayers());
                    }
                }
            }
        }
            return targettablePlayers;
    }

    /**
     * method used to remove the not valid players related to the current Action
     * @param gameManager
     * @param targettablePlayers the players that can be chosen before checking the action
     * @param action the current action
     * @param player the current player for visibility
     * @param effect the current effect
     * @return the selectable players
     */
    public List<Player> removeNotValidActionPlayer(GameManager gameManager, List<Player> targettablePlayers, Action action, Player player,Effect effect){
        if(action.getHitInTheEffect()!=null) {
            targettablePlayers.clear();
            targettablePlayers.addAll(gameManager.getCurrentTurn().getAlreadyHitPlayer().get(action.getHitInTheEffect()));
            for(Effect e: gameManager.getCurrentTurn().getAlreadyHitPlayer().keySet())
                if(!e.equals(effect)&&!e.equals(action.getHitInTheEffect()))
                    for(Player p:gameManager.getCurrentTurn().getAlreadyHitPlayer().get(e))
                        targettablePlayers.remove(p);
        }
        else{
            if(effect.getChainedTo()!=null)
                for(Player p: gameManager.getCurrentTurn().getAlreadyHitPlayer().get(effect.getChainedTo()))
                    targettablePlayers.remove(p);
        }
        return  targettablePlayers;
    }

    /**
     * this method removes the not selectable directions related to the current effect
     * @param gameManager
     * @param targettableDirections the selectable directions before computation
     * @param currentPlayer the current player for visibility
     * @param effect the current effect
     * @return the directions that can be chosen
     */
    public List<Direction> removeNotReachableDirections(GameManager gameManager,List<Direction> targettableDirections,Player currentPlayer, Effect effect ){
        List<Player> temp = new ArrayList<>();
        List<Player> supportTemp;
        List<Direction> support = new ArrayList<>(targettableDirections);
        for (Direction d: support) {
            temp.addAll(getDirectionPlayers(d,currentPlayer,gameManager, effect));
            supportTemp = new ArrayList<>(temp);
            for( Player p: supportTemp) {
                if ((gameManager.getDistanceBetweenTiles(currentPlayer.getCurrentTile(), p.getCurrentTile()) < minMov) || (gameManager.getDistanceBetweenTiles(currentPlayer.getCurrentTile(), p.getCurrentTile()) > maxMov))
                    temp.remove(p);
            }
            if (temp.isEmpty())
                targettableDirections.remove(d);
            temp.clear();
        }
        return targettableDirections;
    }
}
