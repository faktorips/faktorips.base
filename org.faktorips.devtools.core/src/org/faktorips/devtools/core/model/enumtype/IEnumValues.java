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

package org.faktorips.devtools.core.model.enumtype;

/**
 * <p>
 * This object type is used when the values for a Faktor-IPS enumeration shall not be defined
 * directly in the enum type itself but separate from it by the product side.
 * </p>
 * <p>
 * An enum values always refers to a specific enum type which defines the structure of the
 * enumeration.
 * </p>
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public interface IEnumValues extends IEnumValueContainer {

    /** The xml tag for this ips object. */
    public final static String XML_TAG = "EnumValues"; //$NON-NLS-1$

    /** Name of the xml attribute for the enumType property. */
    public final static String XML_ATTRIBUTE_ENUM_TYPE = "enumType"; //$NON-NLS-1$

    /**
     * Sets the enum type this enum values is based upon.
     * 
     * @param enumType The qualified name of the enum type this enum values shall be based upon.
     * 
     * @throws NullPointerException If enumType is <code>null</code>.
     */
    public void setEnumType(String enumType);

    /**
     * Returns the enum type this enum values is based upon.
     * 
     * @return A string representing the qualified name of the enum type this enum values is based
     *         upon.
     */
    public String getEnumType();

}
