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

import java.util.List;

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.ui.AbstractCompletionProcessor;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.util.ArgumentCheck;

public class InverseAssociationCompletionProcessor extends AbstractCompletionProcessor {

    private IPolicyCmptType pcType;
    private IPolicyCmptTypeAssociation association;

    public InverseAssociationCompletionProcessor() {
        // Default constructor
    }

    public InverseAssociationCompletionProcessor(IPolicyCmptTypeAssociation association) {
        ArgumentCheck.notNull(association);
        this.association = association;
        pcType = (IPolicyCmptType)association.getIpsObject();
        setIpsProject(pcType.getIpsProject());
    }

    @Override
    protected void doComputeCompletionProposals(String prefix, int documentOffset, List<ICompletionProposal> result)
            throws Exception {

        prefix = prefix.toLowerCase();
        IPolicyCmptType target = association.findTargetPolicyCmptType(ipsProject);
        if (target == null) {
            return;
        }
        List<IAssociation> associationCandidates = target.findAssociationsForTargetAndAssociationType(association
                .getPolicyCmptType().getQualifiedName(), association.getAssociationType()
                .getCorrespondingAssociationType(), ipsProject, false);
        for (IAssociation association : associationCandidates) {
            // only association with name starts with prefix
            if (association.getName().toLowerCase().startsWith(prefix)) {
                addToResult(result, association, documentOffset);
            }
        }
    }

    private void addToResult(List<ICompletionProposal> result, IAssociation association, int documentOffset) {
        String name = association.getName();
        String displayText = name + " - " + association.getParent().getName(); //$NON-NLS-1$
        Image image = IpsUIPlugin.getImageHandling().getImage(association);
        String localizedDescription = IpsPlugin.getMultiLanguageSupport().getLocalizedDescription(association);
        CompletionProposal proposal = new CompletionProposal(name, 0, documentOffset, name.length(), image,
                displayText, null, localizedDescription);
        result.add(proposal);
    }

}
