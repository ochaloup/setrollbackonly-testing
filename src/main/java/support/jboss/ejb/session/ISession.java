package support.jboss.ejb.session;

import javax.ejb.Local;

@Local
public interface ISession {
	public String test() throws Exception;
}
