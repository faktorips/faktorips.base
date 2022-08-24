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

import java.util.GregorianCalendar;

import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;

/**
 * A reference to a <code>IProductCmpt</code>. Used by <code>ProductCmptStructure</code>.
 * 
 * Note: This is in fact a Reference to ProductCmptLink not to the ProductCmpt. We might refactor
 * the name once
 * 
 * @author Thorsten Guenther
 */
public class ProductCmptReference extends ProductCmptStructureReference implements IProductCmptReference {

    private final IProductCmpt cmpt;
    private final IProductCmptLink link;

    public ProductCmptReference(IProductCmptTreeStructure structure, ProductCmptTypeAssociationReference parent,
            IProductCmpt cmpt, IProductCmptLink link) throws CycleInProductStructureException {
        super(structure, parent);
        this.cmpt = cmpt;
        this.link = link;
    }

    @Override
    public IProductCmptTypeAssociationReference getParent() {
        return (IProductCmptTypeAssociationReference)super.getParent();
    }

    @Override
    public IProductCmpt getProductCmpt() {
        return cmpt;
    }

    @Override
    public IIpsObjectPart getWrapped() {
        return link;
    }

    @Override
    public IIpsObject getWrappedIpsObject() {
        return cmpt;
    }

    @Override
    public IProductCmptLink getLink() {
        return link;
    }

    @Override
    public GregorianCalendar getValidTo() {
        GregorianCalendar validTo = getValidToForCmptOrGeneration();
        return getMostRestrictiveValidToInChildren(validTo);
    }

    protected GregorianCalendar getMostRestrictiveValidToInChildren(final GregorianCalendar validTo) {
        GregorianCalendar mostRestrictiveValidTo = validTo;
        IProductCmptReference[] childProductCmptReferences = getStructure().getChildProductCmptReferences(this);
        for (IProductCmptReference productCmptReference : childProductCmptReferences) {
            GregorianCalendar childValidTo = productCmptReference.getValidTo();
            if (mostRestrictiveValidTo == null
                    || (childValidTo != null && childValidTo.before(mostRestrictiveValidTo))) {
                mostRestrictiveValidTo = childValidTo;
            }
        }
        return mostRestrictiveValidTo;
    }

    protected GregorianCalendar getValidToForCmptOrGeneration() {
        IProductCmptGeneration generation = cmpt.getGenerationEffectiveOn(getStructure().getValidAt());
        GregorianCalendar validTo;
        if (generation == null) {
            validTo = cmpt.getValidTo();
        } else {
            validTo = generation.getValidTo();
        }
        return validTo;
    }

    /**
     * 
     * Returns this {@link IProductCmptReference} if it references the searched product component's
     * qualified name. If not it searches all children in the same way and returns the result.
     * 
     * @param prodCmptQualifiedName the qualified name of the searched {@link IProductCmpt}
     * @return the {@link IProductCmptReference} referencing the indicated {@link IProductCmpt}, or
     *             <code>null</code> if none was found.
     */
    @Override
    public IProductCmptReference findProductCmptReference(String prodCmptQualifiedName) {
        if (getProductCmpt().getQualifiedName().equals(prodCmptQualifiedName)) {
            return this;
        }
        IProductCmptReference[] childProductCmptReferences = getStructure().getChildProductCmptReferences(this);
        for (IProductCmptReference childRef : childProductCmptReferences) {
            IProductCmptReference foundRef = childRef.findProductCmptReference(prodCmptQualifiedName);
            if (foundRef != null) {
                return foundRef;
            }
        }
        return null;
    }

    @Override
    public boolean hasAssociationChildren() {
        return getStructure().getChildProductCmptTypeAssociationReferences(this).length > 0;
    }
}
