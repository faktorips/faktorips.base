/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpt.treestructure;

import org.faktorips.devtools.core.internal.model.adapter.IIpsSrcFileWrapper;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

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
    public IProductCmptTreeStructure getStructure();

    /**
     * @return The <code>IIpsObject</code> referenced by this object or <code>null</code> if the
     *         referenced IPS object doesn't exists.
     */
    public IIpsObject getWrappedIpsObject();

    /**
     * Returns the {@link IIpsProject} this structure reference belongs to.
     * 
     * @return The {@link IIpsProject} this reference belongs to
     */
    public IIpsProject getIpsProject();

    /**
     * Returns the parent structure object of <code>null</code> if this element is the root.
     */
    public IProductCmptStructureReference getParent();

    /**
     * @return The {@link IIpsObjectPart} referenced by this object.
     */
    public abstract IIpsObjectPart getWrapped();

    /**
     * Getting the direct children of this {@link IProductCmptStructureReference}.
     * 
     * @return The children of this reference
     */
    public abstract IProductCmptStructureReference[] getChildren();
}
