package Lib.mega.cmd;

import java.util.Arrays;
import java.util.List;

import Lib.mega.MegaUtils;
import Lib.mega.platform.OSPlatform;

public abstract class AbstractMegaCmd<T> {

    protected List<String> executableCommand() {
        final String[] cmdInstructions = MegaUtils.convertInstructionsToExecParams(
            getCmdAdaptedToPlatform()
        );
        return Arrays.asList(cmdInstructions);
    }

    protected String[] executableCommandArray(){
        final List<String> execCmd = executableCommand();

        String[] execCmdArray = new String[execCmd.size()];
        execCmd.toArray(execCmdArray);

        return execCmdArray;
    }

    protected String getCmdAdaptedToPlatform() {
        return OSPlatform.getCurrent().cmdInstruction(getCmd());
    }

    public abstract String getCmd();
}
