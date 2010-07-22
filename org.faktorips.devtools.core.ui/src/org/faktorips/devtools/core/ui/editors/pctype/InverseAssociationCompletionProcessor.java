/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype;

import java.util.List;

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
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
        IPolicyCmptTypeAssociation[] associationCandidates = target.getPolicyCmptTypeAssociations();
        for (int i = 0; i < associationCandidates.length; i++) {
            // only association candidates with target policy component type equal to the policy
            // component type this association belongs to
            if (!associationCandidates[i].getTarget().equals(association.getPolicyCmptType().getQualifiedName())) {
                continue;
            }

            // only association with name starts with prefix
            if (associationCandidates[i].getName().toLowerCase().startsWith(prefix)) {
                addToResult(result, associationCandidates[i], documentOffset);
            }
        }
    }

    private void addToResult(List<ICompletionProposal> result, IPolicyCmptTypeAssociation relation, int documentOffset) {
        String name = relation.getName();
        String displayText = name + " - " + relation.getParent().getName(); //$NON-NLS-1$
        CompletionProposal proposal = new CompletionProposal(name, 0, documentOffset, name.length(), IpsUIPlugin
                .getImageHandling().getImage(relation), displayText, null, relation.getDescription());
        result.add(proposal);
    }

}
