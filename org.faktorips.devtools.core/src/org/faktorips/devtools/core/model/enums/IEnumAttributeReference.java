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

package org.faktorips.devtools.core.model.enums;

import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;

/**
 * An <tt>IEnumAttributeReference</tt> is part of an <tt>IEnumContent</tt> used to save information
 * about the <tt>IEnumAttribute</tt>s of the <tt>IEnumType</tt> the <tt>IEnumContent</tt> is based
 * upon.
 * <p>
 * This is necessary to be able to determine if the <tt>IEnumAttribute</tt>s have changed since the
 * creation of the <tt>IEnumContent</tt>. In this case, the <tt>IEnumContent</tt> needs to be fixed
 * to be once again consistent with the model.
 * <p>
 * With the use of <tt>IEnumAttributeReference</tt>s we can determine whether the names of the
 * <tt>IEnumAttribute</tt>s or their ordering has changed.
 * 
 * @see IEnumContent
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.4
 */
public interface IEnumAttributeReference extends IIpsObjectPart {

    /** The XML tag for this IPS object part. */
    public final static String XML_TAG = "EnumAttributeReference"; //$NON-NLS-1$

    /**
     * Sets the name of this <tt>IEnumAttributeReference</tt>.
     * 
     * @param name The new name for this <tt>IEnumAttributeReference</tt>.
     * 
     * @throws NullPointerException If <tt>name</tt> is <tt>null</tt>.
     */
    public void setName(String name);

}
