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
 * This is the published interface for enum attributes.
 * </p>
 * <p>
 * For more information about how enum attributes relate to the entire Faktor-IPS enumeration
 * concept please read the documentation of IEnumType.
 * </p>
 * 
 * @see org.faktorips.devtools.core.model.enumtype.IEnumType
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public interface IEnumAttribute extends IIpsObjectPart {

    /** The xml tag for this ips object part. */
    public final static String XML_TAG = "EnumAttribute"; //$NON-NLS-1$

    /** Name of the xml attribute for the datatype property. */
    public final static String XML_ATTRIBUTE_DATATYPE = "datatype"; //$NON-NLS-1$

    /** Name of the xml attribute for the isIdentifier property. */
    public final static String XML_ATTRIBUTE_IS_IDENTIFIER = "isIdentifier"; //$NON-NLS-1$

    /**
     * Sets the name of this enum attribute.
     * 
     * @param name The new name for this enum attribute.
     * 
     * @throws NullPointerException If name is <code>null</code>.
     */
    public void setName(String name);

    /**
     * Returns the unqualified name of the datatype of this enum attribute.
     * 
     * @return A string representing the unqualified name of the datatype of this enum attribute.
     */
    public String getDatatype();

    /**
     * Sets the datatype of this enum attribute.
     * 
     * @param datatype The unqualified name of the datatype.
     * 
     * @throws NullPointerException If datatype is <code>null</code>.
     */
    public void setDatatype(String datatype);

    /**
     * Returns <code>true</code> if this enum attribute is an identifier, <code>false</code> if not.
     * 
     * @return Flag indicating whether this enum attribute is an identifier or not.
     */
    public boolean isIdentifier();

    /**
     * Sets whether this enum attribute is an identifier.
     * 
     * @param isIdentifier Flag indicating whether this enum attribute will be an identifier.
     */
    public void setIsIdentifier(boolean isIdentifier);

}
