package model.exceptions;

public class AmmoException extends GameException {
    /**
     * Ammo Exception empty constructor
     * */
    private String errorString;
    public AmmoException(String errorString) {
        this.errorString=errorString;
        //empty constructor
    }

    /**
     * Override toString to print the error causing the exception
     */
    @Override
    public String toString() {
        return "Error caused by value of "+errorString;
    }
}
