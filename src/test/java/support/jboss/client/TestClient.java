package support.jboss.client;

import java.util.Properties;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;

//import org.junit.Test;
import org.junit.Test;
import org.wildfly.naming.client.WildFlyInitialContextFactory;

import support.jboss.ejb.session.remote.ISession;

public class TestClient {
	Logger log = Logger.getLogger(this.getClass().getCanonicalName());

	// TODO Disabled to avoid auto-run
	@Test
	public void run() throws Exception {
		log.info("test started.");

		try {
			Properties prop = new Properties();
			prop.put(Context.INITIAL_CONTEXT_FACTORY, WildFlyInitialContextFactory.class.getName());
			prop.put(Context.PROVIDER_URL, "remote+http://127.0.0.1:8080");
			// authentication not required when running locally
			// alternatively, update standalone/configuration/application-users.properties
			//prop.put(Context.SECURITY_PRINCIPAL, "user");
			//prop.put(Context.SECURITY_CREDENTIALS, "password");
			InitialContext ctx = new InitialContext(prop);
			log.info("test ------> InitialContext created for Client.");

			((ISession) ctx.lookup("java:jboss-eap-test/SessionBean!support.jboss.ejb.session.remote.ISession")).test();
		} finally {
			log.info("test completed.");
		}
	}
}
