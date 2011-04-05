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

/**
 * The Java naming convention determines for how published interfaces and their implementation are
 * named and how getter and setter are named.
 * 
 * @author Jan Ortmann
 */
public class JavaNamingConvention {

    /*
     * TODO AW: Very bad style (implicit inheritance), instead we should have subclasses, also the
     * JAVA_STANDARD isn't used at all ...
     */
    /**
     * Standard Java naming convention. Interfaces have the name of the concecpt and the
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
     * Returns whether the given unqualified concept name actually is a published interface name.
     */
    public boolean isPublishedInterfaceName(String name) {
        char[] characters = name.toCharArray();
        char firstChar = characters[0];
        if (type == 1) {
            /*
             * First character must be an I while the second character must exist and must not be
             * lower case.
             */
            if (firstChar != 'I') {
                return false;
            }
            if (characters.length < 2) {
                return false;
            }
            return !(Character.isLowerCase(characters[1]));
        } else {
            // Second character must be lower case or non-existent.
            if (characters.length < 2) {
                return true;
            }
            return Character.isLowerCase(characters[1]);
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
     * Returns the unqualified name of the implementation class that is associated to the given
     * published interface name.
     */
    public String getImplementationClassNameForPublishedInterfaceName(String publishedInterfaceName) {
        if (type == 1) {
            return getImplementationClassName(publishedInterfaceName.substring(1));
        } else {
            return getImplementationClassName(publishedInterfaceName);
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
        // The is... instead of get... method is only generated for primitive boolean
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
     * 
     * @param datatype The class of the property's data type.
     * 
     * @deprecated Use {@link #getSetterMethodName(String)} instead as the data type is of no
     *             relevance.
     */
    @Deprecated
    // Deprecated since 3.0
    public String getSetterMethodName(String propertyName, Class<?> datatype) {
        return getSetterMethodName(propertyName);
    }

    /**
     * Returns the method name for the method with that the property is set.
     * 
     * @param datatype The data type of the property.
     * 
     * @deprecated Use {@link #getSetterMethodName(String)} instead as the data type is of no
     *             relevance.
     */
    @Deprecated
    // Deprecated since 3.0
    public String getSetterMethodName(String propertyName, Datatype datatype) {
        return getSetterMethodName(propertyName);
    }

    /**
     * Returns the setter method name for the given property name.
     */
    public String getSetterMethodName(String propertyName) {
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

    /**
     * Returns the Java enumeration literal for the given name.
     * <p>
     * Java enumeration literals consist of all upper case characters whereas each character must be
     * valid to use in a Java identifier. Any invalid character will be transformed to an underscore
     * while umlaute will be replaced with replacement characters (e.g. AE for Ä).
     * <p>
     * Note that this is not part of an official naming convention. We defined this on ourselves.
     */
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

    public String getToDoMarker() {
        return "TODO"; //$NON-NLS-1$
    }

}
