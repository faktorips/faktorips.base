/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modeldescription;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.ui.editors.tablecontents.Messages;

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
    protected List<DescriptionItem> createDescriptions() throws CoreException {
        List<DescriptionItem> descriptions = new ArrayList<DescriptionItem>();
        if (getIpsObject() != null) {
            String localizedDescription = IpsPlugin.getMultiLanguageSupport().getLocalizedDescription(getIpsObject());
            DescriptionItem structureDescription = new DescriptionItem(
                    Messages.TableModelDescriptionPage_generalInformation, localizedDescription);
            descriptions.add(structureDescription);
            List<IAttribute> attributes = getIpsObject().findAllAttributes(getIpsObject().getIpsProject());
            for (IAttribute attribute : attributes) {
                createDescriptionItem(attribute, descriptions);
            }
            List<IAssociation> associations = getIpsObject().findAllAssociations(getIpsObject().getIpsProject());
            for (IAssociation association : associations) {
                createDescriptionItem(association, descriptions);
            }
        }
        return descriptions;
    }

    @Override
    public IPolicyCmptType getIpsObject() {
        return (IPolicyCmptType)super.getIpsObject();
    }

}
