package Lib.mega.auth;

import java.util.Optional;

import Lib.mega.Mega;
import Lib.mega.MegaSession;
import Lib.mega.cmd.MegaCmdLogin;
import Lib.mega.error.MegaException;
import Lib.mega.error.MegaLoginException;

/**
 * Logs into MEGA with an email/username and password combination
 */
public class MegaAuthCredentials extends MegaAuth {

    private final String username;

    private final String password;

    public MegaAuthCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public static final MegaAuthCredentials createFromEnvVariables() {
        String username = Optional.ofNullable(System.getenv(Mega.USERNAME_ENV_VAR))
                .orElseThrow(() -> MegaException.nonExistingEnvVariable(
                        Mega.USERNAME_ENV_VAR
                ));

        String password = Optional.ofNullable(System.getenv(Mega.PASSWORD_ENV_VAR))
                .orElseThrow(() -> MegaException.nonExistingEnvVariable(
                        Mega.PASSWORD_ENV_VAR
                ));

        return new MegaAuthCredentials(username, password);
    }

    @Override
    public MegaSession login() {
        try {
            final MegaCmdLogin megaCmdLogin = new MegaCmdLogin(username, password);
            megaCmdLogin.run();
            return new MegaSession(this);
        } catch (MegaException ex) {
            throw ex;
        } catch (Throwable err) {
            throw new MegaLoginException("Invalid username or password", err);
        }
    }
}
