package model.exceptions;

public class GrabException extends GameException{
    private String errorString;
    //private List<Tile> PossibleTiles;
    /**
     * Grab Exception empty constructor
     * */
    public GrabException(String errorString) {
        this.errorString=errorString;
    }

    /**
     * Override toString to print the error causing the exception
     */
    @Override
    public String toString() {
        return "Error due to an impossible grab: "+errorString;
    }
}
