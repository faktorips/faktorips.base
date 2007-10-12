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

package org.faktorips.devtools.core.ui.editors.pctype;

import java.util.List;

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.ui.AbstractCompletionProcessor;
import org.faktorips.util.ArgumentCheck;


/**
 *
 */
public class ReverseRelationCompletionProcessor extends AbstractCompletionProcessor {
    
    private IPolicyCmptType pcType;
    private IPolicyCmptTypeAssociation relation;
    
    public ReverseRelationCompletionProcessor() {
        
    }
    
    public ReverseRelationCompletionProcessor(IPolicyCmptTypeAssociation relation) {
        ArgumentCheck.notNull(relation);
        this.relation = relation;
        this.pcType = (IPolicyCmptType)relation.getIpsObject();
        setIpsProject(pcType.getIpsProject());
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.AbstractCompletionProcessor#doComputeCompletionProposals(java.lang.String, java.util.List)
     */
    protected void doComputeCompletionProposals(String prefix, int documentOffset, List result) throws Exception {
        prefix = prefix.toLowerCase();
        IPolicyCmptType target = relation.findTarget();
        if (target==null) {
            return;
        }
        IPolicyCmptTypeAssociation[] relations = target.getPolicyCmptTypeAssociations();
        for (int j=0; j<relations.length; j++) {
            if (relations[j].getName().toLowerCase().startsWith(prefix)) {
                addToResult(result, relations[j], documentOffset);
            }
        }
    }
    
    private void addToResult(List result, IPolicyCmptTypeAssociation relation, int documentOffset) {
        String name = relation.getName();
        String displayText = name + " - " + relation.getParent().getName(); //$NON-NLS-1$
        CompletionProposal proposal = new CompletionProposal(
                name, 0, documentOffset, name.length(),  
                relation.getImage(), displayText, null, relation.getDescription());
        result.add(proposal);
    }

}
