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

package org.faktorips.devtools.core.enums;

/**
 * An <code>EnumValue</code> represents a value in an enum type, e.g. male and female are values in
 * the type gender.
 * <p>
 * Two EnumValues are considered equal if they belong to the same type and have the same id.
 * 
 * @author Jan Ortmann
 */
public interface EnumValue extends Comparable<Object> {

    /**
     * Returns the EnumType this value belongs to.
     */
    public EnumType getType();

    /**
     * Returns the enum value's identfication in the enum type.
     */
    public String getId();

    /**
     * Returns the value's human readable name in the default locale.
     */
    public String getName();

    /**
     * Returns the type's id followed by a dot followed by the value's id, e.g.
     * <code>Gender.male</code>
     */
    public abstract String toString();
}
