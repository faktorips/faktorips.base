/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt.treestructure;

import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;

/**
 * A reference to a {@link IProductCmptTypeAssociation}. Used by <code>ProductCmptStructure</code> .
 * 
 * @author Thorsten Guenther
 */
public class ProductCmptTypeAssociationReference extends ProductCmptStructureReference implements
        IProductCmptTypeAssociationReference {

    private final IProductCmptTypeAssociation association;

    public ProductCmptTypeAssociationReference(IProductCmptTreeStructure structure,
            ProductCmptStructureReference parent, IProductCmptTypeAssociation association)
            throws CycleInProductStructureException {
        super(structure, parent);
        this.association = association;
    }

    @Override
    public IProductCmptTypeAssociation getAssociation() {
        return association;
    }

    @Override
    public IIpsObjectPart getWrapped() {
        return association;
    }

    @Override
    public IIpsObject getWrappedIpsObject() {
        return null;
    }

    @Override
    public IIpsProject getIpsProject() {
        return getParent().getIpsProject();
    }

}
