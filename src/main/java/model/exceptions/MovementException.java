package model.exceptions;

public class MovementException extends GameException{
    private String errorString;
    //private List<Tile> PossibleTiles;
    /**
     * Movement Exception empty constructor
     * */
    public MovementException(String errorString) {
        this.errorString=errorString;
    }

    /**
     * Override toString to print the error causing the exception
     */
    @Override
    public String toString() {
        return "Error due to an impossible movement: "+errorString;
    }
}
