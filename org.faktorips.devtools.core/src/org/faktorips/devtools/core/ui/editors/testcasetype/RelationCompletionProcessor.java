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
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.ITypeHierarchy;
import org.faktorips.devtools.core.ui.AbstractCompletionProcessor;
import org.faktorips.util.ArgumentCheck;


/**
 * A completion processor that searchs for relations for a given policy cmpt type.
 */
public class RelationCompletionProcessor extends AbstractCompletionProcessor {
    
    private IPolicyCmptType pcType;
    
    // indicates that only assoziations and composition should be searched
    private boolean onlyAssoziationOrComposition;
    
    public RelationCompletionProcessor() {
    }
    
    /**
     * @param pcType The policy cmpt type the relations will be searched for
     * @param onlyAssoziationOrComposition <code>true</code> indicates that only assoziations and
     *            composition should be searched, <code>false</code> all relation will be searched
     */
    public RelationCompletionProcessor(IPolicyCmptType pcType, boolean onlyAssoziationOrComposition) {
        ArgumentCheck.notNull(pcType);
        this.pcType = pcType;
        this.onlyAssoziationOrComposition = onlyAssoziationOrComposition;
        setIpsProject(pcType.getIpsProject());
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.AbstractCompletionProcessor#doComputeCompletionProposals(java.lang.String, java.util.List)
     */
    protected void doComputeCompletionProposals(String prefix, int documentOffset, List result) throws Exception {
        prefix = prefix.toLowerCase();
        ITypeHierarchy superTypeHierarchy = pcType.getSupertypeHierarchy();
        IRelation[] relations = superTypeHierarchy.getAllRelations(pcType);
        for (int i = 0; i < relations.length; i++) {
            if (onlyAssoziationOrComposition && !(relations[i].isAssoziation() || relations[i].isForwardComposition()))
                continue;

            if (relations[i].getName().toLowerCase().startsWith(prefix)) {
                addToResult(result, relations[i], documentOffset);
            }
        }
    }
    
    private void addToResult(List result, IRelation relation, int documentOffset) {
        String name = relation.getName();
        String displayText = name + " - " + relation.getParent().getName(); //$NON-NLS-1$
        CompletionProposal proposal = new CompletionProposal(
                name, 0, documentOffset, name.length(),  
                relation.getImage(), displayText, null, relation.getDescription());
        result.add(proposal);
    }
}
