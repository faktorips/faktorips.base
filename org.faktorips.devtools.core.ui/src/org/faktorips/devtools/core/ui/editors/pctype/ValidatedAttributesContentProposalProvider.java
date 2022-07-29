/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.faktorips.devtools.core.ui.controls.contentproposal.AbstractPrefixContentProposalProvider;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.pctype.AttributeType;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.util.ArgumentCheck;

public class ValidatedAttributesContentProposalProvider extends AbstractPrefixContentProposalProvider {

    private IPolicyCmptType pcType;

    private IValidationRule rule;

    public ValidatedAttributesContentProposalProvider(IValidationRule rule) {
        ArgumentCheck.notNull(rule);
        this.rule = rule;
        pcType = (IPolicyCmptType)rule.getIpsObject();
    }

    @Override
    public IContentProposal[] getProposals(String prefix) {
        List<IContentProposal> proposals = new ArrayList<>();
        List<IAttribute> attributes = pcType.getSupertypeHierarchy().getAllAttributes(pcType);
        List<String> validatedAttributes = Arrays.asList(rule.getValidatedAttributes());
        for (IAttribute attribute : attributes) {
            IPolicyCmptTypeAttribute iPolicyCmptAttribute = (IPolicyCmptTypeAttribute)attribute;
            if (iPolicyCmptAttribute.getAttributeType() != AttributeType.CONSTANT) {
                if (!validatedAttributes.contains(iPolicyCmptAttribute.getName())
                        && iPolicyCmptAttribute.getName().toLowerCase().startsWith(prefix.toLowerCase())) {
                    addToProposals(proposals, iPolicyCmptAttribute);
                }
            }
        }

        return proposals.toArray(new IContentProposal[0]);
    }

    private void addToProposals(List<IContentProposal> proposals, IPolicyCmptTypeAttribute attribute) {
        String name = attribute.getName();
        String localizedDescription = IIpsModel.get().getMultiLanguageSupport().getLocalizedDescription(attribute);

        IContentProposal proposal = new ContentProposal(name, name, localizedDescription);
        proposals.add(proposal);
    }
}
