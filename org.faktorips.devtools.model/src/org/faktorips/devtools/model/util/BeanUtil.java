/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Utility class for beans and reflection.
 * 
 * @author Jan Ortmann
 */
public final class BeanUtil {

    private static final Map<ClassAndProperty, PropertyDescriptor> PROPERTY_DESCRIPTORS = new HashMap<>();

    private BeanUtil() {
        // Utility class not to be instantiated.
    }

    /**
     * Returns the <code>PropertyDescriptor</code> for the given class and property name.
     * 
     * @throws IllegalArgumentException If the class hasn't got a property of the given name.
     * 
     * @throws RuntimeException If an error occurs while introspecting.
     */
    public static PropertyDescriptor getPropertyDescriptor(Class<?> clazz, String propertyName) {
        return PROPERTY_DESCRIPTORS.computeIfAbsent(new ClassAndProperty(clazz, propertyName),
                cap -> getPropertyDescriptorInternal(cap.clazz, cap.propertyName));
    }

    private static PropertyDescriptor getPropertyDescriptorInternal(Class<?> clazz, String propertyName) {
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

    private static class ClassAndProperty {
        private final Class<?> clazz;
        private final String propertyName;

        public ClassAndProperty(Class<?> clazz, String propertyName) {
            super();
            this.clazz = clazz;
            this.propertyName = propertyName;
        }

        @Override
        public int hashCode() {
            return Objects.hash(clazz, propertyName);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ClassAndProperty)) {
                return false;
            }
            ClassAndProperty other = (ClassAndProperty)obj;
            return Objects.equals(clazz, other.clazz) && Objects.equals(propertyName, other.propertyName);
        }

    }

}
