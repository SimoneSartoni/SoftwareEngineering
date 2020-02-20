package model.exceptions;

public class EffectException extends GameException{
    private String errorString;
    /**
     * Effect Exception empty constructor
     * */
    public EffectException(String errorString) {
        this.errorString=errorString;
    }

    /**
     * Override toString to print the error causing the exception
     */
    @Override
    public String toString() {
        return "Error linked to a particulare Effect: "+errorString;
    }
}
