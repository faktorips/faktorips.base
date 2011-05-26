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

package org.faktorips.devtools.core.internal.model.productcmpt;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainerToTypeDelta;

public class ProductCmptToTypeDelta extends PropertyValueContainerToTypeDelta {

    public ProductCmptToTypeDelta(ProductCmpt productCmpt, IIpsProject ipsProject) throws CoreException {
        super(productCmpt, ipsProject);
    }

    @Override
    public ProductCmpt getPropertyValueContainer() {
        return (ProductCmpt)super.getPropertyValueContainer();
    }

    @Override
    protected void createAdditionalEntriesAndChildren() throws CoreException {
        for (IIpsObjectGeneration generation : getPropertyValueContainer().getGenerationsOrderedByValidDate()) {
            ProductCmptGeneration productCmptGen = (ProductCmptGeneration)generation;
            IPropertyValueContainerToTypeDelta computeDeltaToModel = productCmptGen
                    .computeDeltaToModel(getIpsProject());
            addChild(computeDeltaToModel);
        }
    }

}
