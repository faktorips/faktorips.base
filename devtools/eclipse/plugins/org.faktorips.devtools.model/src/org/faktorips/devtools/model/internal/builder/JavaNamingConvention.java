/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.builder;

import java.lang.reflect.Modifier;

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.internal.ValidationUtils;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IJavaNamingConvention;

/**
 * @author Jan Ortmann
 */
public class JavaNamingConvention implements IJavaNamingConvention {

    @Override
    public String getPublishedInterfaceName(String name) {
        return "I" + name; //$NON-NLS-1$
    }

    @Override
    public String getImplementationClassName(String name) {
        return name;
    }

    @Override
    public String getImplementationClassNameForPublishedInterfaceName(String publishedInterfaceName) {
        return getImplementationClassName(publishedInterfaceName.substring(1));
    }

    @Override
    public String getConstantClassVarName(String propertyName) {
        return StringUtils.upperCase(propertyName);
    }

    @Override
    public String getMemberVarName(String propertyName) {
        return StringUtils.uncapitalize(propertyName);
    }

    @Override
    public String getMultiValueMemberVarName(String propertyNamePlural) {
        return StringUtils.uncapitalize(propertyNamePlural);
    }

    @Override
    public String getMultiValueGetterMethodName(String propertyNamePlural) {
        return "get" + StringUtils.capitalize(propertyNamePlural); //$NON-NLS-1$
    }

    @Override
    public String getGetterMethodName(String propertyName, Class<?> datatype) {
        // The is... instead of get... method is only generated for primitive boolean
        if (datatype.equals(Datatype.PRIMITIVE_BOOLEAN.getClass())) {
            return "is" + StringUtils.capitalize(propertyName); //$NON-NLS-1$
        }
        return "get" + StringUtils.capitalize(propertyName); //$NON-NLS-1$
    }

    @Override
    public String getGetterMethodName(String propertyName, Datatype datatype) {
        // The is... instead of get... method is only generated for primitive boolean
        if (datatype.equals(Datatype.PRIMITIVE_BOOLEAN)) {
            return "is" + StringUtils.capitalize(propertyName); //$NON-NLS-1$
        }
        return "get" + StringUtils.capitalize(propertyName); //$NON-NLS-1$
    }

    @Override
    public String getGetterMethodName(String propertyName) {
        return "get" + StringUtils.capitalize(propertyName); //$NON-NLS-1$
    }

    @Override
    public String getSetterMethodName(String propertyName) {
        return "set" + StringUtils.capitalize(propertyName); //$NON-NLS-1$
    }

    @Override
    public int getModifierForPublicInterfaceMethod() {
        return Modifier.PUBLIC;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Java type names begin with a capital letter.
     */
    @Override
    public String getTypeName(String name) {
        return StringUtils.capitalize(name);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Java enumeration literals consist of all upper case characters whereas each character must be
     * valid to use in a Java identifier. Any invalid character will be transformed to an underscore
     * while umlaute will be replaced with replacement characters (e.g. AE for Ä).
     * <p>
     * Note that this is not part of an official naming convention. We defined this on ourselves.
     */
    @Override
    public String getEnumLiteral(String name) {
        // Transform to all upper case
        String enumLiteral = StringUtils.upperCase(name);

        // Replace umlaute
        enumLiteral = enumLiteral.replace("\u00C4", "AE"); //$NON-NLS-1$ //$NON-NLS-2$
        enumLiteral = enumLiteral.replace("\u00D6", "OE"); //$NON-NLS-1$ //$NON-NLS-2$
        enumLiteral = enumLiteral.replace("\u00DC", "UE"); //$NON-NLS-1$ //$NON-NLS-2$
        enumLiteral = enumLiteral.replace("\u00DF", "SS"); //$NON-NLS-1$ //$NON-NLS-2$

        return getValidJavaIdentifier(enumLiteral);
    }

    /**
     * <p>
     * Any invalid character will be transformed to an underscore.
     * <p>
     * Note that this is not part of an official naming convention. We defined this because code
     * generator generated project name than package name part.
     * 
     * @see ValidationUtils#validateJavaIdentifier(String, IIpsProject)
     * 
     */
    @Override
    public String getValidJavaIdentifier(String identifier) {
        // Replace characters that are not valid for Java identifier
        char[] characters = identifier.toCharArray();
        for (int i = 0; i < characters.length; i++) {
            if (!Character.isJavaIdentifierPart(characters[i])) {
                characters[i] = '_';
            }
        }
        return Character.isJavaIdentifierStart(characters[0]) ? String.valueOf(characters)
                : '_' + String
                        .valueOf(characters);
    }

    @Override
    public String getToDoMarker() {
        return "TODO"; //$NON-NLS-1$
    }

}
