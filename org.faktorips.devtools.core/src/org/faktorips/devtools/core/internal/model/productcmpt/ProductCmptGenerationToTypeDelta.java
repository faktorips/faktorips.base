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
import org.faktorips.devtools.core.internal.model.productcmpt.deltaentries.LinkWithoutAssociationEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;

public class ProductCmptGenerationToTypeDelta extends PropertyValueContainerToTypeDelta {

    public ProductCmptGenerationToTypeDelta(IProductCmptGeneration generation, IIpsProject ipsProject)
            throws CoreException {
        super(generation, ipsProject);
        computeLinksWithMissingAssociations();
    }

    @Override
    public IProductCmptGeneration getPropertyValueContainer() {
        return (IProductCmptGeneration)super.getPropertyValueContainer();
    }

    private void computeLinksWithMissingAssociations() throws CoreException {
        IProductCmptLink[] links = getPropertyValueContainer().getLinks();
        for (IProductCmptLink link : links) {
            if (getProductCmptType().findAssociation(link.getAssociation(), getIpsProject()) == null) {
                LinkWithoutAssociationEntry linkWithoutAssociationEntry = new LinkWithoutAssociationEntry(link);
                addEntry(linkWithoutAssociationEntry);
            }
        }
    }

}
