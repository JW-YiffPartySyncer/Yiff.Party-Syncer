package Lib.mega.auth;

import Lib.mega.MegaSession;
import Lib.mega.error.MegaLoginException;

/**
 * Abstraction of authentication mechanisms, used to provide an strategy
 * of creating a MEGA session.
 */
public abstract class MegaAuth {

    abstract public MegaSession login() throws MegaLoginException;
}
