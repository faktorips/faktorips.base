/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modeldescription;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.ui.editors.tablecontents.Messages;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IAttribute;

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
    protected List<DescriptionItem> createDescriptions() {
        List<DescriptionItem> descriptions = new ArrayList<>();
        IIpsProject ipsProject = getIpsObject().getIpsProject();
        if (getIpsObject() != null) {
            String localizedDescription = IIpsModel.get().getMultiLanguageSupport()
                    .getLocalizedDescription(getIpsObject());
            DescriptionItem structureDescription = new DescriptionItem(
                    Messages.TableModelDescriptionPage_generalInformation, localizedDescription);
            descriptions.add(structureDescription);
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
