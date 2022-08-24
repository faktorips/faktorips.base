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

import java.util.GregorianCalendar;

import org.eclipse.core.runtime.IAdapterFactory;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;

/**
 * This adapter factory tries to get model objects from {@link IProductCmptStructureReference}
 * objects.
 * 
 * @author dirmeier
 */
public class ProductCmptStructureAdapterFactory implements IAdapterFactory {

    @SuppressWarnings("unchecked")
    @Override
    // eclipse does not use generics in IAdapterFactory
    public Object getAdapter(Object adaptableObject, @SuppressWarnings("rawtypes") Class adapterType) {
        if (adaptableObject instanceof IProductCmptReference) {
            IProductCmptReference cmptReference = (IProductCmptReference)adaptableObject;
            if (adapterType.isAssignableFrom(IProductCmptGeneration.class)) {
                return getGeneration(cmptReference);
            }
            if (adapterType.isAssignableFrom(IProductCmptTypeAssociation.class)) {
                // can only adapt to association if there is only one child
                IProductCmptTypeAssociationReference[] children = cmptReference.getStructure()
                        .getChildProductCmptTypeAssociationReferences(cmptReference);
                if (children.length == 1) {
                    IProductCmptTypeAssociationReference associationRef = children[0];
                    return associationRef.getAssociation();
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else if (adaptableObject instanceof IProductCmptTypeAssociationReference) {
            IProductCmptTypeAssociationReference associationReference = (IProductCmptTypeAssociationReference)adaptableObject;
            if (adapterType.isAssignableFrom(IProductCmptGeneration.class)) {
                IProductCmptStructureReference parent = associationReference.getParent();
                if (parent instanceof IProductCmptReference) {
                    return getGeneration((IProductCmptReference)parent);
                } else {
                    return null;
                }
            } else if (adapterType.isAssignableFrom(IProductCmptTypeAssociation.class)) {
                return associationReference.getAssociation();
            } else {
                return null;
            }
        } else if (adaptableObject instanceof IProductCmptVRuleReference) {
            IProductCmptVRuleReference vruleReference = (IProductCmptVRuleReference)adaptableObject;
            if (adapterType.isAssignableFrom(IProductCmptGeneration.class)) {
                IProductCmptStructureReference parent = vruleReference.getParent();
                if (parent instanceof IProductCmptReference) {
                    return getGeneration((IProductCmptReference)parent);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    Object getGeneration(IProductCmptReference cmptReference) {
        GregorianCalendar validAt = cmptReference.getStructure().getValidAt();
        return cmptReference.getProductCmpt().getGenerationEffectiveOn(validAt);
    }

    @Override
    public Class<?>[] getAdapterList() {
        return new Class[] { IProductCmptGeneration.class, IProductCmptTypeAssociation.class };
    }

}
