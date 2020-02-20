package model.exceptions;

public class WrongTileException extends GameException {
    /**
     * Wrong Tile Exception empty constructor
     * */
    public WrongTileException() {
        //empty constructor
    }

    /**
     * Override toString to print the error causing the exception
     */
    @Override
    public String toString() {
        return "Error caused by the choice of a wrong Tile";
    }
}
