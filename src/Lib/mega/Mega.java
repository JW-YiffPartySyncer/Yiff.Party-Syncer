package Lib.mega;

import Lib.mega.auth.MegaAuth;
import Lib.mega.auth.MegaAuthCredentials;
import Lib.mega.auth.MegaAuthSessionID;
import Lib.mega.cmd.MegaCmdQuit;
import Lib.mega.cmd.MegaCmdSession;
import Lib.mega.cmd.MegaCmdSignup;
import Lib.mega.cmd.MegaCmdVersion;
import Lib.mega.error.MegaException;

public interface Mega {

  String USERNAME_ENV_VAR = "MEGA_EMAIL";
  String PASSWORD_ENV_VAR = "MEGA_PWD";
  String CMD_TTL_ENV_VAR = "MEGA_CMD_TTL";

  public static String[] envVars() {
    String pathVar = "PATH=" + System.getenv("PATH");
    return new String[]{pathVar};
  }

  public static MegaSession init() {
    try {
      MegaServer.getCurrent().start();
      return currentSession();
    } catch (MegaException e) {
      return login(MegaAuthCredentials.createFromEnvVariables());
    }
  }

  public static MegaSession login(MegaAuth credentials) {
    return credentials.login();
  }

  public static MegaSession currentSession() {
    final String sessionID = new MegaCmdSession().call();
    return login(new MegaAuthSessionID(sessionID));
  }

  public static MegaCmdSignup signup(String username, String password) {
    return new MegaCmdSignup(username, password);
  }

  public static void quit() {
    new MegaCmdQuit().run();
  }

  public static MegaCmdVersion version(){
    return new MegaCmdVersion();
  }
}
