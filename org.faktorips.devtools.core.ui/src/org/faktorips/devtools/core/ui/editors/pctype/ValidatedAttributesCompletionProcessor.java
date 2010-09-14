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

package org.faktorips.devtools.core.ui.editors.pctype;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
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
        IAttribute[] attributes = pcType.getSupertypeHierarchy().getAllAttributes(pcType);
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
        CompletionProposal proposal = new CompletionProposal(name, 0, documentOffset, name.length(), IpsUIPlugin
                .getImageHandling().getImage(attribute), name, null, attribute.getDescription());
        result.add(proposal);
    }
}
