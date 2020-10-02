package support.jboss.ejb.session;

import javax.ejb.Local;

@Local
public interface ISessionLocal extends ISession {
	void onDataUpdate(DataUpdatedEvent dataUpdatedEvent);
}
