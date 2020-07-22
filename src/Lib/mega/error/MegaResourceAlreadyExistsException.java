package Lib.mega.error;

public class MegaResourceAlreadyExistsException extends MegaWrongArgumentsException {

  public MegaResourceAlreadyExistsException() {
    super("Existent resource");
  }
}

