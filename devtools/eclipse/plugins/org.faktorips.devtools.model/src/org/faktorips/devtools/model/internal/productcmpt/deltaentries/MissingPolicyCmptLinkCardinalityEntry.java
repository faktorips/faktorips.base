/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt.deltaentries;

import java.text.MessageFormat;

import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.internal.productcmpt.PolicyCmptLinkCardinality;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpt.DeltaType;
import org.faktorips.devtools.model.productcmpt.IDeltaEntry;
import org.faktorips.devtools.model.productcmpt.IPolicyCmptLinkCardinality;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;

/**
 * Delta entry for a {@link IPolicyCmptLinkCardinality} that should be there because of a
 * {@link IPolicyCmptTypeAssociation#isCardinalityConfigurable()} but is missing in the current
 * product component (generation).
 *
 * @since 26.7
 */
public class MissingPolicyCmptLinkCardinalityEntry implements IDeltaEntry {

    private final IProductCmptLinkContainer linkContainer;
    private final IPolicyCmptTypeAssociation policyAssociation;

    public MissingPolicyCmptLinkCardinalityEntry(IPolicyCmptTypeAssociation policyAssociation,
            IProductCmptLinkContainer linkContainer) {
        this.policyAssociation = policyAssociation;
        this.linkContainer = linkContainer;
    }

    @Override
    public Class<PolicyCmptLinkCardinality> getPartType() {
        return PolicyCmptLinkCardinality.class;
    }

    @Override
    public DeltaType getDeltaType() {
        return DeltaType.MISSING_POLICY_CMPT_LINK_CARDINALITY;
    }

    @Override
    public String getDescription() {
        String policyAssociationCaption = IIpsModel.get().getMultiLanguageSupport()
                .getLocalizedCaption(policyAssociation);
        return MessageFormat.format(Messages.MissingPolicyCmptLinkCardinalityEntry_fixMessage,
                policyAssociationCaption);
    }

    /**
     * Creates a new policy component link cardinality for the policy association.
     */
    @Override
    public void fix() {
        var newPolicyCmptLinkCardinality = linkContainer.newPolicyCmptLinkCardinality(policyAssociation.getName());
        IProductCmpt productCmpt = linkContainer.getProductCmpt();
        if (productCmpt.isUsingTemplate()) {
            newPolicyCmptLinkCardinality.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        }
    }

}
