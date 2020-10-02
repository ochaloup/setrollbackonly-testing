package support.jboss.ejb.session;

import support.jboss.ejb.session.interceptor.CDIInterceptorBinding;
import support.redhat.jdbc.JdbcUtil;

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
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;
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
@Local(support.jboss.ejb.session.ISessionLocal.class)
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

	@Inject
	private Event<DataUpdatedEvent> dataUpdatedEvent;

	private boolean isRollback = true;

	// definition of the interceptor which may set the rollback on transaction
	// when method finishes - it's enabled only when defined in 'beans.xml'
	@CDIInterceptorBinding
	public String test() {
		log.info("---> test started.");

		boolean success = false;
		try (Connection connection = datasource.getConnection()) {

			log.info("Connection1: " + connection);
			if(isRollback) {
				// set rollback only before the second connection is enlisted to transaction
				log.info("Rollback is setup and the ejbContext sets the rollback only now");
				ejbContext.setRollbackOnly();

				// working with synchronization which rollbacks on afterCompletion call
				/*
				RollbackOnlySynchronization rollbackSync = new RollbackOnlySynchronization();
				tm.getTransaction().registerSynchronization(rollbackSync);
				rollbackSync.setForRollback();
				*/

				// shooting the event which is observed by CDI observer method onDataUpdate
				// dataUpdatedEvent.fire(new DataUpdatedEvent());
			}

			try (Connection connection2 = datasource2.getConnection()) {
				log.info("Connection2: " + connection2);
				// JdbcUtil.executeUpdate(connection2, "INSERT INTO (A) VALUSE ('a')", true);
			} catch (SQLException sqle) {
				throw new RuntimeException("Cannot work with connection of datasource2: " + datasource2, sqle);
			}
		} catch (SQLException sqle) {
			throw new RuntimeException("Cannot work with connection of datasource1: " + datasource, sqle);
		}/* catch (SystemException se) {
			throw new IllegalStateException("Cannot get current transaction from the transaction manager " + tm, se);
		} catch (RollbackException re) {
			throw new RuntimeException("The current transaction is marked for rollback only and adding synchronization is not permitted.", re);
		} **/

		// verification what happens when setRollbackOnly is set at the end of the method when all actions are already done
		// ejbContext.setRollbackOnly();

		log.info("---> test finished.");
		return "OK";
	}

	/* The CDI observer to be run in the same way as when the Synchronization is created */
	public void onDataUpdate(@Observes(during = TransactionPhase.BEFORE_COMPLETION) DataUpdatedEvent dataUpdatedEvent) {
		ejbContext.setRollbackOnly();
	}

	public boolean isRollback() {
		return isRollback;
	}
}
