/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.ipsproject;

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
    public String getPublishedInterfaceName(String name);

    /**
     * Returns the unqualified name of the implementation class for the given concept name.
     */
    public String getImplementationClassName(String name);

    /**
     * Returns the unqualified name of the implementation class that is associated to the given
     * published interface name.
     */
    public String getImplementationClassNameForPublishedInterfaceName(String publishedInterfaceName);

    /**
     * Converts the provided name of a constant class variable according to these naming convention
     * and returns it.
     */
    public String getConstantClassVarName(String propertyName);

    /**
     * Returns the name of the member variable for a property.
     */
    public String getMemberVarName(String propertyName);

    /**
     * Returns the name of the member variable for a multi value property.
     */
    public String getMultiValueMemberVarName(String propertyNamePlural);

    /**
     * Returns the method name for the method with that the property is read.
     */
    public String getGetterMethodName(String propertyName, Class<?> datatype);

    /**
     * Returns the method name for the method with that the property is read.
     */
    public String getGetterMethodName(String propertyName, Datatype datatype);

    /**
     * Returns the method name for the method with that the property is read. In contrast to
     * {@link #getGetterMethodName(String, Class)} and
     * {@link #getGetterMethodName(String, Datatype)} this method does not respect special cases for
     * exaple for boolean datatype.
     */
    public String getGetterMethodName(String propertyName);

    /**
     * Returns the method name for the method with that a multi value property is read.
     */
    public String getMultiValueGetterMethodName(String propertyNamePlural);

    /**
     * Returns the setter method name for the given property name.
     */
    public String getSetterMethodName(String propertyName);

    /**
     * Returns the modifier used for public interface methods.
     */
    public int getModifierForPublicInterfaceMethod();

    /**
     * Returns the Java type name for the given name.
     */
    public String getTypeName(String name);

    /**
     * Returns the Java enumeration literal for the given name.
     */
    public String getEnumLiteral(String name);

    /**
     * Returns how to-do markers are named according to this naming convention.
     */
    public String getToDoMarker();

    /**
     * Replace characters with underscore that are not valid for Java identifier.
     */
    public String getValidJavaIdentifier(String identifier);

}
