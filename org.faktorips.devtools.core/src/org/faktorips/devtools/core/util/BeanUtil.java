/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

/**
 * Utility class for beans and reflection.
 * 
 * @author Jan Ortmann
 */
public final class BeanUtil {

    /**
     * Returns the <tt>PropertyDescriptor</tt> for the given class and property name.
     * 
     * @throws IllegalArgumentException If the class hasn't got a property of the given name.
     * 
     * @throws RuntimeException If an error occurs while introspecting.
     */
    public static final PropertyDescriptor getPropertyDescriptor(Class<?> clazz, String propertyName) {
        try {
            BeanInfo info = Introspector.getBeanInfo(clazz);
            PropertyDescriptor[] props = info.getPropertyDescriptors();
            for (PropertyDescriptor prop : props) {
                if (prop.getName().equals(propertyName)) {
                    return prop;
                }
            }
        } catch (IntrospectionException e) {
            throw new RuntimeException("Exception while introspection class " //$NON-NLS-1$
                    + clazz, e);
        }
        throw new IllegalArgumentException("Class " + clazz + " hasn't got a property " + propertyName); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private BeanUtil() {
        // Utility class not to be instantiated.
    }

}
