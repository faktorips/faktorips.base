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

package org.faktorips.devtools.core.ui.editors.type;

import java.util.List;

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.ui.AbstractCompletionProcessor;
import org.faktorips.util.ArgumentCheck;

/**
 * A completion processor that searchs for derived unions for a given associations.
 * The search is done along the supertype hierarchy starting with the type the given association belongs to.
 */
public class DerivedUnionCompletionProcessor extends AbstractCompletionProcessor {
    
    private IAssociation association;
    
    public DerivedUnionCompletionProcessor(IAssociation association) {
        ArgumentCheck.notNull(association);
        
        this.association = association;
        
        setIpsProject(association.getIpsProject());
    }

    /** 
     * {@inheritDoc}
     */
    protected void doComputeCompletionProposals(String prefix, int documentOffset, List result) throws Exception {
        IAssociation[] derivedUnionCandidates = association.findDerivedUnionCandidates(association.getIpsProject());
        for (int i = 0; i < derivedUnionCandidates.length; i++) {
            if (derivedUnionCandidates[i].getName().toLowerCase().startsWith(prefix)){
                addToResult(result, derivedUnionCandidates[i], documentOffset);
            }
        }
    }
    
    private void addToResult(List result, IAssociation relation, int documentOffset) {
        String name = relation.getName();
        String displayText = name + " - " + relation.getParent().getName(); //$NON-NLS-1$
        CompletionProposal proposal = new CompletionProposal(
                name, 0, documentOffset, name.length(),  
                relation.getImage(), displayText, null, relation.getDescription());
        result.add(proposal);
    }
}
