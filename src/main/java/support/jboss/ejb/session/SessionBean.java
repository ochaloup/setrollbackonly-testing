package support.jboss.ejb.session;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.sql.DataSource;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

@Resource( // Standardize lookup for all deployment types (EJB JAR, WAR, EAR)
	name = "java:jboss/exported/jboss-eap-test/SessionBean!support.jboss.ejb.session.remote.ISession",
	lookup = "java:module/SessionBean!support.jboss.ejb.session.remote.ISession"
)
@Stateless
@Local(support.jboss.ejb.session.ISession.class)
@Remote(support.jboss.ejb.session.remote.ISession.class)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class SessionBean {
	Logger log = Logger.getLogger(this.getClass().getCanonicalName());

	@Resource(lookup = "java:/TransactionManager")
	private TransactionManager tm;

	@Resource(mappedName = "java:jboss/datasources/ExampleXADS")
	private DataSource datasource;

	@Resource(mappedName = "java:jboss/datasources/ExampleXADS2")
	private DataSource datasource2;

	@Resource
	private EJBContext ejbContext;

	public String test() {
		log.info("---> test started.");

		boolean success = false;
		try (Connection connection = datasource.getConnection()) {

			// ejbContext.setRollbackOnly();
			RollbackOnlySynchronization rollbackSync = new RollbackOnlySynchronization();
			tm.getTransaction().registerSynchronization(rollbackSync);
			rollbackSync.setForRollback();

			log.info("Connection1: " + connection);

			try (Connection connection2 = datasource2.getConnection()) {
				log.info("Connection2: " + connection2);
			} catch (SQLException sqle) {
				throw new RuntimeException("Cannot work with connection of datasource2: " + datasource2, sqle);
			}
		} catch (SQLException sqle) {
			throw new RuntimeException("Cannot work with connection of datasource1: " + datasource, sqle);
		} catch (SystemException se) {
			throw new IllegalStateException("Cannot get current transaction from the transaction manager " + tm, se);
		} catch (RollbackException re) {
			throw new RuntimeException("The current transaction is marked for rollback only and adding synchronization is not permitted.", re);
		}

		log.info("---> test finished.");
		return "OK";
	}
}
