/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modeldescription;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;

/**
 * A page for presenting the properties of a {@link IProductCmptType} or {@link IProductCmpt}. This
 * page is connected to a Editor similar to the outline view.
 * 
 * @author Quirin Stoll
 */
public class ProductCmptTypeDescriptionPage extends DefaultModelDescriptionPage {

    public ProductCmptTypeDescriptionPage(IProductCmptType productCmptType) {
        super();
        setIpsObject(productCmptType);
        setDescriptionData();
    }

    @Override
    protected List<DescriptionItem> createDescriptions() throws CoreException {
        List<DescriptionItem> descriptions = new ArrayList<DescriptionItem>();
        IIpsProject ipsProject = getIpsObject().getIpsProject();
        if (getIpsObject() != null) {
            List<IAttribute> attributes = getIpsObject().findAllAttributes(ipsProject);
            for (IAttribute attribute : attributes) {
                createDescriptionItem(attribute, descriptions);
            }
            IPolicyCmptType policyCmptType = getIpsObject().findPolicyCmptType(ipsProject);
            if (policyCmptType != null) {
                List<IAttribute> policyCmptAttributes = policyCmptType.findAllAttributes(ipsProject);
                for (IAttribute attribute : policyCmptAttributes) {
                    IPolicyCmptTypeAttribute pcAttribute = (IPolicyCmptTypeAttribute)attribute;
                    if (pcAttribute.isProductRelevant()) {
                        createDescriptionItem(attribute, descriptions);
                    }
                }
            }
            List<IAssociation> associations = getIpsObject().findAllAssociations(ipsProject);
            for (IAssociation association : associations) {
                if (!association.isDerivedUnion()) {
                    createDescriptionItem(association, descriptions);
                }
            }
        }
        return descriptions;
    }

    @Override
    public IProductCmptType getIpsObject() {
        return (IProductCmptType)super.getIpsObject();
    }

}
