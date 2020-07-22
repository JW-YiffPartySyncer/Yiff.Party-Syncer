package Lib.mega.error;

public class MegaWrongArgumentsException extends MegaException {

    public MegaWrongArgumentsException(String message) {
        super(message);
    }

    public MegaWrongArgumentsException() {
        this("Wrong arguments");
    }
}
