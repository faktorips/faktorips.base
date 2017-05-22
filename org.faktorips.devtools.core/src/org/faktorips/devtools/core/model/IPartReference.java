/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model;

import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;

/**
 * An <tt>IPartReference</tt> is part of an <tt>IEnumContent</tt>/<tt>ITableContents</tt> used to
 * save information about the <tt>IEnumAttribute</tt>s/<tt>IColumn</tt>s of the
 * <tt>IEnumType</tt>/<tt>ITableStructure</tt> the <tt>IEnumContent</tt>/<tt>ITableContents</tt> is
 * based upon.
 * <p>
 * This is necessary to be able to determine if the <tt>IEnumAttribute</tt>s/<tt>IColumn</tt>s have
 * changed since the creation of the <tt>IEnumContent</tt>/<tt>ITableContents</tt>. In this case,
 * the <tt>IEnumContent</tt> needs to be fixed to be once again consistent with the model.
 * <p>
 * With the use of <tt>IPartReference</tt>s we can determine whether the names of the
 * <tt>IEnumAttribute</tt>s/<tt>IColumn</tt>s or their ordering has changed.
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
     * Sets the name of this <tt>IPartReference</tt>.
     * 
     * @param name The new name for this <tt>IPartReference</tt>.
     * 
     * @throws NullPointerException If <tt>name</tt> is <tt>null</tt>.
     */
    public void setName(String name);

}
