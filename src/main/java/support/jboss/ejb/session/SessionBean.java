package support.jboss.ejb.session;

import java.sql.Connection;
//import java.sql.DatabaseMetaData;
//import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

//import javax.annotation.PostConstruct;
import javax.annotation.Resource;
//import javax.ejb.DependsOn;
import javax.ejb.Local;
import javax.ejb.Remote;
//import javax.ejb.Schedule;
//import javax.ejb.Singleton;
//import javax.ejb.Startup;
import javax.ejb.Stateless;
//import javax.ejb.TransactionAttribute;
//import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;

@Resource( // Standardize lookup for all deployment types (EJB JAR, WAR, EAR)
	name = "java:jboss/exported/jboss-eap-test/SessionBean!support.jboss.ejb.session.remote.ISession",
	lookup = "java:module/SessionBean!support.jboss.ejb.session.remote.ISession"
)
@Stateless
@Local(support.jboss.ejb.session.ISession.class)
@Remote(support.jboss.ejb.session.remote.ISession.class)
@TransactionManagement(TransactionManagementType.BEAN)
//@Startup
//@Singleton(name = "AutoTester")
//@DependsOn("OtherSingleton")
public class SessionBean {

	Logger log = Logger.getLogger(this.getClass().getCanonicalName());

	@Resource(mappedName = "java:jboss/UserTransaction")
	protected UserTransaction userTransaction;

	@Resource(mappedName = "java:jboss/datasources/ExampleDS")
	private DataSource datasource;

	@Resource(mappedName = "java:jboss/datasources/ExampleDS2")
	private DataSource datasource2;

	//@PostConstruct
	//@Schedule(second="*/10", minute="*", hour="*", persistent=false)
	//@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public String test() {
		log.info("---> test started.");

		boolean success = false;
		try {
			Connection connection = datasource.getConnection();
			try {
/*
				try {
					connection.close(); // this works around the issue
				} finally {
					connection = null;
				}
*/
				userTransaction.begin();

				try (Connection connection2 = datasource2.getConnection()) {
					success = true;
				} finally {
					userTransaction.rollback();
				}
			} finally {
				if (connection != null) {
					connection.close();
				}
			}

		} catch (Throwable t) {
			log.log(Level.SEVERE, t.getMessage(), t);
		} finally {
			log.info("---> test " + (success ? "success" : "failure"));
			return (success ? "success" : "failure");
		}
	}
}
