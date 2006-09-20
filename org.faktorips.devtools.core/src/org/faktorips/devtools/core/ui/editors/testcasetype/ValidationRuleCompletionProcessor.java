/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcasetype;

import java.util.List;

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.ui.AbstractCompletionProcessor;
import org.faktorips.util.ArgumentCheck;


/**
 * A completion processor that searchs for relations for a given policy cmpt type.
 */
public class ValidationRuleCompletionProcessor extends AbstractCompletionProcessor {
    
    private ITestCaseType testCaseType;
    
    public ValidationRuleCompletionProcessor() {
    }
    
    public ValidationRuleCompletionProcessor(ITestCaseType testCaseType) {
        ArgumentCheck.notNull(testCaseType);
        this.testCaseType = testCaseType;
        setIpsProject(testCaseType.getIpsProject());
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.AbstractCompletionProcessor#doComputeCompletionProposals(java.lang.String, java.util.List)
     */
    protected void doComputeCompletionProposals(String prefix, int documentOffset, List result) throws Exception {
        prefix = prefix.toLowerCase();
        IValidationRule[] rules = testCaseType.getTestRuleCandidates();
        for (int i = 0; i < rules.length; i++) {
            if (rules[i].getName().toLowerCase().startsWith(prefix)) {
                addToResult(result, rules[i], documentOffset);
            }
        }
    }
    
    private void addToResult(List result, IValidationRule rule, int documentOffset) {
        String name = rule.getName();
        String displayText = name;
        CompletionProposal proposal = new CompletionProposal(name, 0, documentOffset, name.length(), rule.getImage(),
                displayText, null, rule.getDescription());
        result.add(proposal);
    }
}
