package support.redhat.reflect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClassUtil {
	private static final Logger log = Logger.getLogger(ClassUtil.class.getCanonicalName());

	/**
	 * Find the method for the class (matching name and parameters), ascending the superclass tree
	 *
	 * @throws NoSuchMethodException if the method is not found
	 */
	public static Method getMethod(Class<?> clazz, String method, Class<?>... params)
			// explicitly declaring as the parent ReflectiveOperationException is JavaSE 1.7 and later only
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		log.log(Level.FINEST, "getMethod(" + clazz + "," + method + ")");

		for (Method m : clazz.getDeclaredMethods()) {
			if (m.getName().equals(method)) {
				log.log(Level.FINEST, "getMethod() found method name match for " + method);

				boolean paramsMatch = true;

				Class<?>[] hasParams = m.getParameterTypes();
				if (params != null && hasParams != null) {
					if (params.length == hasParams.length) {
						for (int i = 0; i < params.length; i++) {
							if (!params[i].getCanonicalName().equals(hasParams[i].getCanonicalName())) {
								log.log(Level.FINEST, "getMethod() found param mis-match "
										+ params[i].getCanonicalName() + " != " + hasParams[i].getCanonicalName());
								paramsMatch = false;
								break;
							}
						}
					} else {
						log.log(Level.FINEST, "getMethod() found param count mis-match");
					}
				} else if (params != hasParams) { // one is null, one is not
					log.log(Level.FINEST, "getMethod() found param mis-match");
					paramsMatch = false;
				}

				if (paramsMatch) {
					if (!m.isAccessible()) {
						m.setAccessible(true);
					}
					return m;
				}
			}
		}
		Class<?> superclass = clazz.getSuperclass();
		if (superclass == null) {
			throw new NoSuchMethodException(clazz.getName() + "." + method + "()");
		} else {
			return getMethod(superclass, method, params);
		}
	}
}
