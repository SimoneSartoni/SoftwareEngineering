package model.exceptions;

public class WrongCommandException extends GameException{
    private String wrongCommandString;
    /**
     * Wrong Command Exception empty constructor
     * */
    public WrongCommandException(String wrongCommandString) {
        this.wrongCommandString=wrongCommandString;
    }

    /**
     * Override toString to print the error causing the exception
     */
    @Override
    public String toString() {
        return "Command: "+wrongCommandString+" not available";
    }
}
