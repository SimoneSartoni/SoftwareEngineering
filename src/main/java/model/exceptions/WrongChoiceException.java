package model.exceptions;

public class WrongChoiceException extends GameException {
    private String errorString;

    /**
     * Wrong Choice Exception empty constructor
     * */
    public WrongChoiceException(String errorString) {
        this.errorString=errorString;
    }

    /**
     * Override toString to print the error causing the exception
     */
    @Override
    public String toString() {
        return "Error caused by a wrong choice of "+errorString;
    }
}
