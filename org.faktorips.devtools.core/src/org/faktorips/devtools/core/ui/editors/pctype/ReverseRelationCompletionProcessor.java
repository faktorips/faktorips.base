package org.faktorips.devtools.core.ui.editors.pctype;

import java.util.List;

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.ui.AbstractCompletionProcessor;
import org.faktorips.util.ArgumentCheck;


/**
 *
 */
public class ReverseRelationCompletionProcessor extends AbstractCompletionProcessor {
    
    private IPolicyCmptType pcType;
    private IRelation relation;
    
    public ReverseRelationCompletionProcessor() {
        
    }
    
    public ReverseRelationCompletionProcessor(IRelation relation) {
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
        IRelation[] relations = target.getRelations();
        for (int j=0; j<relations.length; j++) {
            if (relations[j].getName().toLowerCase().startsWith(prefix)) {
                addToResult(result, relations[j], documentOffset);
            }
        }
    }
    
    private void addToResult(List result, IRelation relation, int documentOffset) {
        String name = relation.getName();
        String displayText = name + " - " + relation.getParent().getName();
        CompletionProposal proposal = new CompletionProposal(
                name, 0, documentOffset, name.length(),  
                relation.getImage(), displayText, null, relation.getDescription());
        result.add(proposal);
    }

}
