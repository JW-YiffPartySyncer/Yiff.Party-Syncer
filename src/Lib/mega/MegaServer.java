package Lib.mega;

import Lib.mega.error.MegaUnexpectedFailureException;
import Lib.mega.platform.OSPlatform;

public class MegaServer {

    private static MegaServer current;

    public static MegaServer getCurrent() {
        if (current == null) {
            current = new MegaServer();
        }

        return current;
    }

    private MegaServer() {
    }

    public void start() {
        try {
            final String[] cmdParams = MegaUtils.convertInstructionsToExecParams(
                OSPlatform.getCurrent().cmdInstruction("help")
            );
            MegaUtils.execCmd(cmdParams);
        } catch (Exception e) {
            throw new MegaUnexpectedFailureException();
        }
    }

    public void stop() {
        Mega.quit();
    }
}
