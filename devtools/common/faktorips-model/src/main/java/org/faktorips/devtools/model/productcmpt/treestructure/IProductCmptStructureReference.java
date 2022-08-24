/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.productcmpt.treestructure;

import org.faktorips.devtools.model.adapter.IIpsSrcFileWrapper;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * A reference to unspecified objects (see sub interfaces / -classes for further details) for used
 * in a <code>IProductCmptStructure</code>.
 * 
 * @author Thorsten Guenther
 */
public interface IProductCmptStructureReference extends IIpsSrcFileWrapper {

    /**
     * @return The <code>IProductCmptStructure</code> this reference belongs to.
     */
    IProductCmptTreeStructure getStructure();

    /**
     * @return The <code>IIpsObject</code> referenced by this object or <code>null</code> if the
     *             referenced IPS object doesn't exists.
     */
    IIpsObject getWrappedIpsObject();

    /**
     * Returns the {@link IIpsProject} this structure reference belongs to.
     * 
     * @return The {@link IIpsProject} this reference belongs to
     */
    IIpsProject getIpsProject();

    /**
     * Returns the parent structure object of <code>null</code> if this element is the root.
     */
    IProductCmptStructureReference getParent();

    /**
     * Returns whether this reference is the root of its structure. Thus returns <code>true</code>
     * if it has no parent, <code>false</code> if it does have one.
     */
    boolean isRoot();

    /**
     * @return The {@link IIpsObjectPart} referenced by this object.
     */
    IIpsObjectPart getWrapped();

    /**
     * Getting the direct children of this {@link IProductCmptStructureReference}.
     * 
     * @return The children of this reference
     */
    IProductCmptStructureReference[] getChildren();
}
