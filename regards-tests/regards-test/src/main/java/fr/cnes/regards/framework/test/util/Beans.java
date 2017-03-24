package fr.cnes.regards.framework.test.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;

public final class Beans {

    /**
     * Compare two objects first with equals then by their properties readers, recursively follow inner objects using
     * same comparison mechanism.
     * <b>Beware of collections or arrays, in this case, native equals is used.</b>
     * @param pO1 first object to compare
     * @param pO2 second object to compare
     * @return true when objects equal, false otherwise
     */
    @SuppressWarnings("rawtypes")
    public static boolean equals(final Object pO1, final Object pO2) {
        Object o1 = pO1;
        Object o2 = pO2;
        if ((o1 == null) && (o2 == null)) {
            return true;
        } else if ((o1 == null) || (o2 == null)) {
            return false;
        }

        if (o1.getClass().isArray() && (o2.getClass().isArray())) {
            return Arrays.equals((Object[]) o1, (Object[]) o2);
        }
        if (!Objects.equal(o1, o2)) {
            return false;
        }

        if (o1.getClass() != o2.getClass()) {
            throw new IllegalArgumentException("Both objects must be of same class");
        }
        // Particular case of "base" types
        if ((o1 instanceof Number) || (o1 instanceof String) || (o1 instanceof Character)) {
            return o1.equals(o2);
        }
        // Find all properties
        try {
            BeanInfo info1 = Introspector.getBeanInfo(o1.getClass(), Object.class);
            if ((info1.getMethodDescriptors() == null) || (info1.getMethodDescriptors().length == 0)) {
                return true;
            }
            // For all externally accessible methods
            for (MethodDescriptor methodDesc : info1.getMethodDescriptors()) {
                Method method = methodDesc.getMethod();
                // if it is a read property method (starting by get or is and without any parameter)
                if ((method.getParameterCount() == 0)
                        && (method.getName().startsWith("get") || method.getName().startsWith("is"))) {
                    Object v1 = method.invoke(o1, new Object[0]);
                    Object v2 = method.invoke(o2, new Object[0]);
                    if (v1 != null) {
                        if (v1.getClass().getName().startsWith("fr.cnes")) {
                            if (!Beans.equals(v1, v2)) {
                                return false;
                            }
                        } else if (v1 instanceof Collection) { // For Hb9n PersistentBag type which seems to not be compatible with collections
                            if (!Beans.equals(((Collection) v1).toArray(), ((Collection) v2).toArray())) {
                                return false;
                            }
                        } else {
                            if (!v1.equals(v2)) {
                                return false;
                            }
                        }
                    }
                }
            }
            return true;
        } catch (IntrospectionException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw Throwables.propagate(e);
        }
    }
}