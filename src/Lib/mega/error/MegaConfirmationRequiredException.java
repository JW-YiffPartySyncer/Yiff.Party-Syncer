package Lib.mega.error;

public class MegaConfirmationRequiredException extends MegaException {

    public MegaConfirmationRequiredException() {
        super("Mega requires confirmation");
    }
}
