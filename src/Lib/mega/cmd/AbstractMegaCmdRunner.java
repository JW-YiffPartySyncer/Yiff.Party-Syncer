package Lib.mega.cmd;


import java.io.IOException;

import Lib.mega.MegaUtils;
import Lib.mega.error.MegaIOException;


public abstract class AbstractMegaCmdRunner extends AbstractMegaCmd implements Runnable {

    @Override
    public void run() {
        executeSysCmd(executableCommandArray());
    }

    protected void executeSysCmd(String... cmdStr) {
        try {
          final int result = MegaUtils.execCmd(cmdStr);
          MegaUtils.handleResult(result);
        } catch (IOException e) {
            throw new MegaIOException();
        } catch (InterruptedException e) {
            throw new MegaIOException(
                    "The execution of %s couldn't be finished", getCmd()
            );
        }
    }
}
