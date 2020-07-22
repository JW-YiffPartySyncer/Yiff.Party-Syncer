package Lib.mega.cmd;

import java.io.IOException;
import java.util.Optional;

import Lib.mega.MegaUtils;
import Lib.mega.error.MegaIOException;
import Lib.mega.error.MegaLoginRequiredException;

public class MegaCmdWhoAmI extends AbstractMegaCmdCaller<String> {

    static final Optional<String> parseUsername(String response) {
        return Optional.ofNullable(response)
                .map(s -> s.split("e-mail:"))
                .filter(x -> x.length == 2)
                .map(s -> s[1].trim());
    }

    @Override
    public String getCmd() {
        return "whoami";
    }

    @Override
    public String call() {
        try {
            final String response =
                    MegaUtils.execCmdWithSingleOutput(executableCommandArray());
            return parseUsername(response).orElseThrow(
                    () -> new MegaLoginRequiredException()
            );
        } catch (IOException e) {
            throw new MegaIOException();
        }
    }
}
