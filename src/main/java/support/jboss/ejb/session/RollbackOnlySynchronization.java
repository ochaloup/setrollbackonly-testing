package support.jboss.ejb.session;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import java.util.logging.Logger;

public class RollbackOnlySynchronization implements Synchronization {
    private static final Logger log = Logger.getLogger(RollbackOnlySynchronization.class.getName());

    private boolean isForRollback = false;

    @Override
    public void beforeCompletion() {
        if(!isForRollback) {
            return;
        }

        TransactionManager tm = null;
        try {
            tm = (TransactionManager) new InitialContext().lookup("java:/TransactionManager");
            tm.setRollbackOnly();
        } catch (NamingException ne) {
            throw new IllegalStateException("Cannot find transaction manager", ne);
        } catch (SystemException se) {
            throw new IllegalStateException("Cannot setRollbackOnly for tm " + tm, se);
        }
    }

    @Override
    public void afterCompletion(int status) {
        log.info("The transaction finished with status: " + getStatusAsString(status));
    }

    public void setForRollback() {
        isForRollback = true;
    }

    /**
     * Converts transaction int status code {@link javax.transaction.Status}
     * to string form.
     */
    private static String getStatusAsString(int statusCode) {
        String status;
        switch(statusCode) {
            case Status.STATUS_ACTIVE:
                status = "STATUS_ACTIVE";
                break;
            case Status.STATUS_MARKED_ROLLBACK:
                status = "STATUS_MARKED_ROLLBACK";
                break;
            case Status.STATUS_PREPARED:
                status = "STATUS_PREPARED";
                break;
            case Status.STATUS_COMMITTED:
                status = "STATUS_COMMITTED";
                break;
            case Status.STATUS_ROLLEDBACK:
                status = "STATUS_ROLLEDBACK";
                break;
            case Status.STATUS_UNKNOWN:
                status = "STATUS_UNKNOWN";
                break;
            case Status.STATUS_NO_TRANSACTION:
                status = "STATUS_NO_TRANSACTION";
                break;
            case Status.STATUS_PREPARING:
                status = "STATUS_PREPARING";
                break;
            case Status.STATUS_COMMITTING:
                status = "STATUS_COMMITTING";
                break;
            case Status.STATUS_ROLLING_BACK:
                status = "STATUS_ROLLING_BACK";
                break;
            default:
                throw new IllegalStateException("Can't determine status code " + statusCode
                        + " as transaction status code defined under " + Status.class.getName());
        }
        return String.format("%s (%d)", status, statusCode);
    }

}
