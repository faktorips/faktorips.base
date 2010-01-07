/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

/**
 *
 */
public class InverseAssociationCompletionProcessor extends AbstractCompletionProcessor {

    private IPolicyCmptType pcType;
    private IPolicyCmptTypeAssociation relation;

    public InverseAssociationCompletionProcessor() {

    }

    public InverseAssociationCompletionProcessor(IPolicyCmptTypeAssociation relation) {
        ArgumentCheck.notNull(relation);
        this.relation = relation;
        pcType = (IPolicyCmptType)relation.getIpsObject();
        setIpsProject(pcType.getIpsProject());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doComputeCompletionProposals(String prefix, int documentOffset, List<ICompletionProposal> result)
            throws Exception {
        prefix = prefix.toLowerCase();
        IPolicyCmptType target = relation.findTargetPolicyCmptType(ipsProject);
        if (target == null) {
            return;
        }
        IPolicyCmptTypeAssociation[] relations = target.getPolicyCmptTypeAssociations();
        for (int j = 0; j < relations.length; j++) {
            if (relations[j].getName().toLowerCase().startsWith(prefix)) {
                addToResult(result, relations[j], documentOffset);
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
