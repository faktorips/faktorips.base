/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model;

import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.tablecontents.ITableContents;

/**
 * An <code>IPartReference</code> is part of an
 * <code>IEnumContent</code>/<code>ITableContents</code> used to save information about the
 * <code>IEnumAttribute</code>s/<code>IColumn</code>s of the
 * <code>IEnumType</code>/<code>ITableStructure</code> the
 * <code>IEnumContent</code>/<code>ITableContents</code> is based upon.
 * <p>
 * This is necessary to be able to determine if the
 * <code>IEnumAttribute</code>s/<code>IColumn</code>s have changed since the creation of the
 * <code>IEnumContent</code>/<code>ITableContents</code>. In this case, the
 * <code>IEnumContent</code> needs to be fixed to be once again consistent with the model.
 * <p>
 * With the use of <code>IPartReference</code>s we can determine whether the names of the
 * <code>IEnumAttribute</code>s/<code>IColumn</code>s or their ordering has changed.
 * 
 * @see IEnumContent
 * @see ITableContents
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.4
 */
public interface IPartReference extends IIpsObjectPart {

    /**
     * Sets the name of this <code>IPartReference</code>.
     * 
     * @param name The new name for this <code>IPartReference</code>.
     * 
     * @throws NullPointerException If <code>name</code> is <code>null</code>.
     */
    void setName(String name);

}
