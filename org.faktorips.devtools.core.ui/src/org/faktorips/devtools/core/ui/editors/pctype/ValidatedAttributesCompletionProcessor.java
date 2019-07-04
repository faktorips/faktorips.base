/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.ui.AbstractCompletionProcessor;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.util.ArgumentCheck;

public class ValidatedAttributesCompletionProcessor extends AbstractCompletionProcessor {

    private IPolicyCmptType pcType;

    private IValidationRule rule;

    public ValidatedAttributesCompletionProcessor(IValidationRule rule) {
        ArgumentCheck.notNull(rule);
        this.rule = rule;
        pcType = (IPolicyCmptType)rule.getIpsObject();
        setIpsProject(pcType.getIpsProject());
    }

    @Override
    public void doComputeCompletionProposals(String prefix, int documentOffset, List<ICompletionProposal> result)
            throws Exception {

        prefix = prefix.toLowerCase();
        List<IAttribute> attributes = pcType.getSupertypeHierarchy().getAllAttributes(pcType);
        List<String> validatedAttributes = Arrays.asList(rule.getValidatedAttributes());
        for (IAttribute attribute : attributes) {
            IPolicyCmptTypeAttribute iPolicyCmptAttributte = (IPolicyCmptTypeAttribute)attribute;
            if (iPolicyCmptAttributte.getAttributeType() != AttributeType.CONSTANT) {
                if (!validatedAttributes.contains(iPolicyCmptAttributte.getName())
                        && iPolicyCmptAttributte.getName().toLowerCase().startsWith(prefix)) {
                    addToResult(result, iPolicyCmptAttributte, documentOffset);
                }
            }
        }
    }

    private void addToResult(List<ICompletionProposal> result, IPolicyCmptTypeAttribute attribute, int documentOffset) {
        String name = attribute.getName();
        Image image = IpsUIPlugin.getImageHandling().getImage(attribute);
        String localizedDescription = IpsPlugin.getMultiLanguageSupport().getLocalizedDescription(attribute);
        CompletionProposal proposal = new CompletionProposal(name, 0, documentOffset, name.length(), image, name, null,
                localizedDescription);
        result.add(proposal);
    }
}
