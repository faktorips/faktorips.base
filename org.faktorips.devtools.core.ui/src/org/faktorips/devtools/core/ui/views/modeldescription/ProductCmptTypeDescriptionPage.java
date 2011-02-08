/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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
import org.faktorips.devtools.core.ui.editors.pctype.Messages;

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
            IAttribute[] attributes = getIpsObject().findAllAttributes(ipsProject);
            List<DescriptionItem> attributeDescriptions = new ArrayList<DescriptionItem>();
            for (IAttribute attribute : attributes) {
                createDescriptionItem(attribute, attributeDescriptions);
            }
            IPolicyCmptType policyCmptType = getIpsObject().findPolicyCmptType(ipsProject);
            if (policyCmptType != null) {
                IAttribute[] policyCmptAttributes = policyCmptType.findAllAttributes(ipsProject);
                for (IAttribute attribute : policyCmptAttributes) {
                    IPolicyCmptTypeAttribute pcAttribute = (IPolicyCmptTypeAttribute)attribute;
                    if (pcAttribute.isProductRelevant()) {
                        createDescriptionItem(attribute, attributeDescriptions);
                    }
                }
            }
            descriptions.add(new DescriptionItem(Messages.AttributesSection_title, attributeDescriptions));
            IAssociation[] associations = getIpsObject().findAllAssociations(ipsProject);
            List<DescriptionItem> aDescriptions = new ArrayList<DescriptionItem>();
            for (IAssociation association : associations) {
                if (!association.isDerivedUnion()) {
                    createDescriptionItem(association, aDescriptions);
                }
            }
            descriptions.add(new DescriptionItem(Messages.AssociationsSection_title, aDescriptions));
        }
        return descriptions;
    }

    @Override
    public IProductCmptType getIpsObject() {
        return (IProductCmptType)super.getIpsObject();
    }

}
