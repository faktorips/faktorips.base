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

import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;

/**
 * <p>
 * An enum attribute value belongs to an enum value and represents the value of a specific enum
 * attribute.
 * </p>
 * <p>
 * An enum attribute value can be imagined as the value of a field in a table.
 * </p>
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public interface IEnumAttributeValue extends IIpsObjectPart {

    /** The xml tag for this ips object part. */
    public final static String XML_TAG = "EnumAttributeValue"; //$NON-NLS-1$
    
    /** Name of the xml attribute for the value property. */
    public final static String XML_ATTRIBUTE_VALUE = "value"; //$NON-NLS-1$

    /**
     * Returns the enum attribute this enum attribute value refers to.
     * 
     * @return A reference to the enum attribute this enum attribute value refers to.
     */
    public IEnumAttribute getEnumAttribute();

    /**
     * Returns the value as string.
     * 
     * @return A string representing the actual value.
     */
    public String getValue();

    /**
     * Sets the actual value.
     * 
     * @param value The new value.
     * 
     * @throws NullPointerException If value is <code>null</code>.
     */
    public void setValue(String value);

}
