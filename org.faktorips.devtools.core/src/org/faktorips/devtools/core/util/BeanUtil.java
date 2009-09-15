/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

/**
 * Utility-Class for beans and reflection.
 * 
 * @author Jan Ortmann
 */
public final class BeanUtil {

    /**
     * Returns the PropertyDescriptor for the given class and property name.
     * 
     * @throws IllegalArgumentException if the class hasn't got a property of the given name.
     * @throws RuntimeException if an error occurs while introspecting.
     */
    public static final PropertyDescriptor getPropertyDescriptor(Class<?> clazz, String propertyName) {
        BeanInfo info;
        try {
            info = Introspector.getBeanInfo(clazz);
        } catch (IntrospectionException e) {
            throw new RuntimeException("Exception while introspection class " //$NON-NLS-1$
                    + clazz, e);
        }
        PropertyDescriptor[] props = info.getPropertyDescriptors();
        for (int i = 0; i < props.length; i++) {
            if (props[i].getName().equals(propertyName)) {
                return props[i];
            }
        }
        throw new IllegalArgumentException("Class " + clazz + " hasn't got a property " + propertyName); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private BeanUtil() {
    }
}
