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

package org.faktorips.devtools.core.builder;

import java.lang.reflect.Modifier;

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.Datatype;

/**
 * The Java naming convention determines for how published interfaces and their implementation are
 * named and how getter and setter are named.
 * 
 * @author Jan Ortmann
 */
public class JavaNamingConvention {

    /**
     * Standard Java naming convention. Interfaces have the name of the conecpt and the
     * implementation has the suffix 'Impl'.
     */
    public final static JavaNamingConvention JAVA_STANDARD = new JavaNamingConvention(0);

    /**
     * Standard Eclipse naming convention. Interfaces have the prefix 'I' and the implementation has
     * the concept name.
     */
    public final static JavaNamingConvention ECLIPSE_STANDARD = new JavaNamingConvention(1);

    private int type;

    private JavaNamingConvention(int type) {
        this.type = type;
    }

    /**
     * Returns the unqualified name of the published interface for the given concept name.
     */
    public String getPublishedInterfaceName(String name) {
        if (type == 1) {
            return "I" + name; //$NON-NLS-1$
        } else {
            return name;
        }
    }

    /**
     * Returns the unqualified name of the implementation class for the given concept name.
     */
    public String getImplementationClassName(String name) {
        if (type == 1) {
            return name;
        } else {
            return name + "Impl"; //$NON-NLS-1$
        }
    }

    /**
     * Converts the provided name of a constant class variable according to these naming convention
     * and returns it.
     */
    public String getConstantClassVarName(String propertyName) {
        return StringUtils.upperCase(propertyName);
    }

    /**
     * Returns the name of the member variable for a property.
     */
    public String getMemberVarName(String propertyName) {
        return StringUtils.uncapitalize(propertyName);
    }

    /**
     * Returns the name of the member variable for a multi value property.
     */
    public String getMultiValueMemberVarName(String propertyNamePlural) {
        return StringUtils.uncapitalize(propertyNamePlural);
    }

    /**
     * Returns the method name for the method with that the property is read.
     */
    public String getGetterMethodName(String propertyName, Class<?> datatype) {
        if (datatype.equals(Boolean.TYPE)) {
            return "is" + StringUtils.capitalize(propertyName); //$NON-NLS-1$
        }
        return "get" + StringUtils.capitalize(propertyName); //$NON-NLS-1$
    }

    /**
     * Returns the method name for the method with that the property is read.
     */
    public String getGetterMethodName(String propertyName, Datatype datatype) {
        if (datatype.equals(Datatype.PRIMITIVE_BOOLEAN)) {
            return "is" + StringUtils.capitalize(propertyName); //$NON-NLS-1$
        }
        return "get" + StringUtils.capitalize(propertyName); //$NON-NLS-1$
    }

    /**
     * Returns the method name for the method with that a multi value property is read.
     */
    public String getMultiValueGetterMethodName(String propertyNamePlural) {
        return "get" + StringUtils.capitalize(propertyNamePlural); //$NON-NLS-1$
    }

    /**
     * Returns the method name for the method with that the property is set.
     */
    public String getSetterMethodName(String propertyName, Class<?> datatype) {
        return "set" + StringUtils.capitalize(propertyName); //$NON-NLS-1$
    }

    /**
     * Returns the method name for the method with that the property is set.
     */
    public String getSetterMethodName(String propertyName, Datatype datatype) {
        return "set" + StringUtils.capitalize(propertyName); //$NON-NLS-1$
    }

    /**
     * Returns the modifier used for public interface methods.
     */
    public int getModifierForPublicInterfaceMethod() {
        return Modifier.PUBLIC;
    }

    /**
     * Returns the java type name for the given name. Java type names begin with a capital letter.
     */
    public String getTypeName(String name) {
        return StringUtils.capitalize(name);
    }

    public String getToDoMarker() {
        return "TODO"; //$NON-NLS-1$
    }
}
