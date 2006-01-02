package org.faktorips.devtools.core.ui.editors.tablestructure;

import java.util.List;

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.tablestructure.IUniqueKey;
import org.faktorips.devtools.core.ui.AbstractCompletionProcessor;


/**
 * A completion processor for a table structure's unique keys.
 */
public class UniqueKeyCompletionProcessor extends AbstractCompletionProcessor {

    private ITableStructure tableStructure;
    
    public UniqueKeyCompletionProcessor() {
        super();
        setComputeProposalForEmptyPrefix(true);
    }
    
    /**
     * Sets the table structure which unique keys should be completed.
     * If <code>null</code> is passed, no completion is available. 
     */
    public void setTableStructure(ITableStructure structure) {
        tableStructure = structure;
        if (tableStructure!=null) {
            setIpsProject(tableStructure.getIpsProject());
        }
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.AbstractCompletionProcessor#doComputeCompletionProposals(java.lang.String, int, java.util.List)
     */
    protected void doComputeCompletionProposals(String prefix,
            int documentOffset, List result) throws Exception {

        prefix = prefix.toLowerCase();
        if (tableStructure==null) {
            return;
        }
        IUniqueKey[] keys = tableStructure.getUniqueKeys();
        for (int i=0; i<keys.length; i++) {
            addToResult(result, keys[i], documentOffset);
        }
    }
    
    private void addToResult(List result, IUniqueKey key, int documentOffset) {
        String name = key.getName();
        String displayText = name;
        CompletionProposal proposal = new CompletionProposal(
                name, 0, documentOffset, name.length(),  
                key.getImage(), displayText, null, key.getDescription());
        result.add(proposal);
    }


}
