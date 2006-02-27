package org.faktorips.devtools.core.ui.editors.pctype;

import java.util.List;

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.ITypeHierarchy;
import org.faktorips.devtools.core.ui.AbstractCompletionProcessor;
import org.faktorips.util.ArgumentCheck;


/**
 * A completion processor that searchs for abstract read-only container relations for a given relation.
 * The search is done along the supertype hierarchy starting with the type the given relation belongs to.
 */
public class ContainerRelationCompletionProcessor extends AbstractCompletionProcessor {
    
    private IPolicyCmptType pcType;
    private IRelation relation;
    
    public ContainerRelationCompletionProcessor(IRelation relation) {
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
        ITypeHierarchy hierarchy = pcType.getSupertypeHierarchy();
        IPolicyCmptType[] supertypes = hierarchy.getAllSupertypesInclSelf(pcType);
        for (int i=0; i<supertypes.length; i++) {
            IRelation[] relations = supertypes[i].getRelations();
            for (int j=0; j<relations.length; j++) {
                if (relations[j].isReadOnlyContainer()) {
                    addToResult(result, relations[j], documentOffset);
                }
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
