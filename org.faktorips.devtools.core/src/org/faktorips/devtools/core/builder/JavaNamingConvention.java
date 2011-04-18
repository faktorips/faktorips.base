/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.builder;

import java.lang.reflect.Modifier;

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsproject.IJavaNamingConvention;
import org.faktorips.util.ArgumentCheck;

/**
 * @author Jan Ortmann
 */
public class JavaNamingConvention implements IJavaNamingConvention {

    @Override
    public String getPublishedInterfaceName(String name) {
        return "I" + name; //$NON-NLS-1$
    }

    @Override
    public boolean isPublishedInterfaceName(String name) {
        char[] characters = name.toCharArray();
        char firstChar = characters[0];
        /*
         * First character must be an I while the second character must exist and must not be lower
         * case.
         */
        if (firstChar != 'I') {
            return false;
        }
        if (characters.length < 2) {
            return false;
        }
        return !(Character.isLowerCase(characters[1]));
    }

    @Override
    public String getImplementationClassName(String name) {
        return name;
    }

    @Override
    public String getImplementationClassNameForPublishedInterfaceName(String publishedInterfaceName) {
        ArgumentCheck.isTrue(isPublishedInterfaceName(publishedInterfaceName));
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
        enumLiteral = enumLiteral.replaceAll("[Ää]", "AE"); //$NON-NLS-1$ //$NON-NLS-2$
        enumLiteral = enumLiteral.replaceAll("[Öö]", "OE"); //$NON-NLS-1$ //$NON-NLS-2$
        enumLiteral = enumLiteral.replaceAll("[Üü]", "UE"); //$NON-NLS-1$ //$NON-NLS-2$
        enumLiteral = enumLiteral.replaceAll("[ß]", "SS"); //$NON-NLS-1$ //$NON-NLS-2$

        // Replace characters that are not valid for Java identifiers
        char[] characters = enumLiteral.toCharArray();
        for (int i = 0; i < characters.length; i++) {
            if ((i == 0 && !Character.isJavaIdentifierStart(characters[i]))
                    || (i > 0 && !Character.isJavaIdentifierPart(characters[i]))) {
                characters[i] = '_';
            }
        }

        return String.valueOf(characters);
    }

    @Override
    public String getToDoMarker() {
        return "TODO"; //$NON-NLS-1$
    }

}
