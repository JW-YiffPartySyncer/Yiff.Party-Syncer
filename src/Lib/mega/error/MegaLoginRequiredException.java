package Lib.mega.error;

public class MegaLoginRequiredException extends MegaException {

    public MegaLoginRequiredException() {
        super("You need to login first");
    }
}
