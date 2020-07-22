package Lib.mega.cmd;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import Lib.mega.MegaUtils;
import Lib.mega.error.MegaLoginException;

public class MegaCmdLogin extends AbstractMegaCmdRunnerWithParams {

    private final List<String> cmdParams;

    public MegaCmdLogin(String... cmdParams) {
        super();
        this.cmdParams = Arrays.asList(cmdParams);
    }

    @Override
    public String getCmd() {
        return "login";
    }

    @Override
    protected void executeSysCmd(String... cmdStr) {
        try {
            final int result = MegaUtils.execCmd(cmdStr);
            MegaUtils.handleResult(result);
        } catch (IOException e) {
            throw new MegaLoginException(
                    "Error while executing login command in Mega"
            );
        } catch (InterruptedException e) {
            throw new MegaLoginException("The login was interrupted");
        }
    }

    @Override
    protected List<String> cmdParams() {
        return cmdParams;
    }
}
