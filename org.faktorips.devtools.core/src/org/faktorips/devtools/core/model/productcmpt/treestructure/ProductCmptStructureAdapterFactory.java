/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.productcmpt.treestructure;

import java.util.GregorianCalendar;

import org.eclipse.core.runtime.IAdapterFactory;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;

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
                if (cmptReference.getChildren().length == 1
                        && cmptReference.getChildren()[0] instanceof IProductCmptTypeAssociationReference) {
                    IProductCmptTypeAssociationReference associationRef = (IProductCmptTypeAssociationReference)cmptReference
                            .getChildren()[0];
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
        return cmptReference.getProductCmpt().findGenerationEffectiveOn(validAt);
    }

    @SuppressWarnings("rawtypes")
    // eclipse does not use generics in IAdapterFactory
    @Override
    public Class[] getAdapterList() {
        return new Class[] { IProductCmptGeneration.class, IProductCmptTypeAssociation.class };
    }

}
