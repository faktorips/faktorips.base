/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.ipsproject;

import org.faktorips.datatype.Datatype;

/**
 * Determines how Java elements are named.
 * 
 * @author Jan Ortmann
 */
public interface IJavaNamingConvention {

    /**
     * Returns the unqualified name of the published interface for the given concept name.
     */
    String getPublishedInterfaceName(String name);

    /**
     * Returns the unqualified name of the implementation class for the given concept name.
     */
    String getImplementationClassName(String name);

    /**
     * Returns the unqualified name of the implementation class that is associated to the given
     * published interface name.
     */
    String getImplementationClassNameForPublishedInterfaceName(String publishedInterfaceName);

    /**
     * Converts the provided name of a constant class variable according to these naming convention
     * and returns it.
     */
    String getConstantClassVarName(String propertyName);

    /**
     * Returns the name of the member variable for a property.
     */
    String getMemberVarName(String propertyName);

    /**
     * Returns the name of the member variable for a multi value property.
     */
    String getMultiValueMemberVarName(String propertyNamePlural);

    /**
     * Returns the method name for the method with that the property is read.
     */
    String getGetterMethodName(String propertyName, Class<?> datatype);

    /**
     * Returns the method name for the method with that the property is read.
     */
    String getGetterMethodName(String propertyName, Datatype datatype);

    /**
     * Returns the method name for the method with that the property is read. In contrast to
     * {@link #getGetterMethodName(String, Class)} and
     * {@link #getGetterMethodName(String, Datatype)} this method does not respect special cases for
     * exaple for boolean datatype.
     */
    String getGetterMethodName(String propertyName);

    /**
     * Returns the method name for the method with that a multi value property is read.
     */
    String getMultiValueGetterMethodName(String propertyNamePlural);

    /**
     * Returns the setter method name for the given property name.
     */
    String getSetterMethodName(String propertyName);

    /**
     * Returns the modifier used for public interface methods.
     */
    int getModifierForPublicInterfaceMethod();

    /**
     * Returns the Java type name for the given name.
     */
    String getTypeName(String name);

    /**
     * Returns the Java enumeration literal for the given name.
     */
    String getEnumLiteral(String name);

    /**
     * Returns how to-do markers are named according to this naming convention.
     */
    String getToDoMarker();

    /**
     * Replace characters with underscore that are not valid for Java identifier.
     */
    String getValidJavaIdentifier(String identifier);

}
