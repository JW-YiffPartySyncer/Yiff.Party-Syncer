package Lib.mega.auth;

import java.util.Optional;

import Lib.mega.Mega;
import Lib.mega.MegaSession;
import Lib.mega.error.MegaException;
import Lib.mega.error.MegaLoginException;

/**
 * Logs into MEGA using a session ID
 */
public class MegaAuthSessionID extends MegaAuth {

    private final String sessionID;

    public MegaAuthSessionID(String sessionID) {
        this.sessionID = sessionID;
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

    public String getSessionID() {
        return sessionID;
    }

    @Override
    public MegaSession login() throws MegaLoginException {
        try{
            return new MegaSession(this);
        }catch(Throwable err){
            throw new MegaLoginException("There is no started session", err);
        }
    }
}
