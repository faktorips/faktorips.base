/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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
    public static final String XML_TAG = "EnumAttributeReference"; //$NON-NLS-1$

    /**
     * Sets the name of this <tt>IEnumAttributeReference</tt>.
     * 
     * @param name The new name for this <tt>IEnumAttributeReference</tt>.
     * 
     * @throws NullPointerException If <tt>name</tt> is <tt>null</tt>.
     */
    public void setName(String name);

}
