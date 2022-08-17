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

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.type.IMethod;

/**
 * A page for presenting the properties of a {@link IPolicyCmptType}. This page is connected to a
 * Editor similar to the outline view.
 * 
 * @author Quirin Stoll
 */
public class PolicyCmtTypeDescriptionPage extends DefaultModelDescriptionPage {

    public PolicyCmtTypeDescriptionPage(IPolicyCmptType policyCmptType) {
        super();
        setIpsObject(policyCmptType);
        setDescriptionData();
    }

    @Override
    protected List<DescriptionItem> createDescriptions() {
        IIpsProject ipsProject = getIpsObject().getIpsProject();
        List<DescriptionItem> descriptions = new ArrayList<>();
        if (getIpsObject() != null) {
            descriptions.add(createStructureDescriptionItem());
            List<IAttribute> attributes = getIpsObject().findAllAttributes(ipsProject);
            for (IAttribute attribute : attributes) {
                createDescriptionItem(attribute, descriptions);
            }
            List<IAssociation> associations = getIpsObject().findAllAssociations(ipsProject);
            for (IAssociation association : associations) {
                createDescriptionItem(association, descriptions);
            }
            List<IMethod> methods = getIpsObject().findAllMethods(ipsProject);
            for (IMethod method : methods) {
                createDescriptionItem(method, descriptions);
            }
            List<IValidationRule> rules = getIpsObject().findAllValidationRules(ipsProject);
            for (IValidationRule rule : rules) {
                createDescriptionItem(rule, descriptions);
            }
        }
        return descriptions;
    }

    @Override
    public IPolicyCmptType getIpsObject() {
        return (IPolicyCmptType)super.getIpsObject();
    }

}
