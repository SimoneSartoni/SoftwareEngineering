package model.exceptions;

public class ShootingException extends GameException{
    private String errorString;
    //private List<Tile> PossibleTiles;
    /**
     * Grab Exception empty constructor
     * */
    public ShootingException(String errorString) {
        this.errorString=errorString;
    }

    /**
     * Override toString to print the error causing the exception
     */
    @Override
    public String toString() {
        return "Error due to an impossible shooting: "+errorString;
    }
}
