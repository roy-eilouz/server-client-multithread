package bgu.spl.net.impl.rci;

import bgu.spl.net.api.MessagingProtocol;
import sun.security.provider.certpath.UntrustedChecker;

import java.io.Serializable;
@SuppressWarnings("unchecked")
public class RemoteCommandInvocationProtocol<T> implements MessagingProtocol<Serializable> {

    private T arg;

    public RemoteCommandInvocationProtocol(T arg) {
        this.arg = arg;
    }

    @Override
    public Serializable process(Serializable msg) {
        return ((Command) msg).execute(arg);
    }

    @Override
    public boolean shouldTerminate() {
        return false;
    }

}
