package model.weapon.actions;

import model.board.GameManager;
import model.board.Tile;
import model.enums.Direction;
import model.enums.RoomColor;
import model.player.Player;
import model.weapon.Effect;

/**
 * interface for the Visitor pattern
 */
public interface Visitable {
    void initialExecute(Effect effectVisitor, GameManager gameManager, Player currentPlayer /*, Player targetPlayer*/);
    void afterChooseDirectionExecute(Effect effectVisitor, GameManager gameManager, Player currentPlayer, Direction direction);
    void afterChooseTargetExecute(Effect effectVisitor, GameManager gameManager, Player currentPlayer,Player targetPlayer);
    void afterChooseTileExecute(Effect effectVisitor, GameManager gameManager, Player currentPlayer, Tile tile);
    void afterChooseRoomExecute(Effect effectVisitor, GameManager gameManager, Player currentPlayer, RoomColor roomColor);
    void afterCounterAttackedExecute(Effect effectVisitor,GameManager gameManager,Player currentPlayer);
    void linkedToNextExecute(Effect effectVisitor, GameManager gameManager, Player currentPlayer);
}
