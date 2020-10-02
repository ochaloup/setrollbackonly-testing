package support.jboss.ejb.session.remote;

import javax.ejb.Remote;

// Extending local interface for convenience of method declaration
@Remote
public interface ISession extends support.jboss.ejb.session.ISession {
}
