package support.jboss.ejb.session.interceptor;

import support.jboss.ejb.session.SessionBean;

import javax.annotation.Resource;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.transaction.TransactionManager;
import javax.transaction.Transactional;
import javax.transaction.UserTransaction;
import java.util.logging.Logger;

@Interceptor
@CDIInterceptorBinding
public class CDIInterceptor {
    private static final Logger log = Logger.getLogger(CDIInterceptor.class.getName());

    @Resource(lookup = "java:/TransactionManager")
    private TransactionManager tm;

    @AroundInvoke
    public Object manageTransaction(InvocationContext context) throws Exception {
        Object ret = context.proceed();

        log.info("Processing CDI interceptor to possibly set rollbackOnly");
        if(context.getTarget() instanceof SessionBean) {
            if (((SessionBean) context.getTarget()).isRollback()) {
                tm.setRollbackOnly();
            }
        }
        return ret;
    }

}
