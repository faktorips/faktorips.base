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

package org.faktorips.devtools.core.ui.editors.tablestructure;

import java.util.List;

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.tablestructure.IUniqueKey;
import org.faktorips.devtools.core.ui.AbstractCompletionProcessor;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

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
     * Sets the table structure which unique keys should be completed. If <code>null</code> is
     * passed, no completion is available.
     */
    public void setTableStructure(ITableStructure structure) {
        tableStructure = structure;
        if (tableStructure != null) {
            setIpsProject(tableStructure.getIpsProject());
        }
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.ui.AbstractCompletionProcessor#doComputeCompletionProposals(java.lang.String,
     *      int, java.util.List)
     */
    @Override
    protected void doComputeCompletionProposals(String prefix, int documentOffset, List<ICompletionProposal> result)
            throws Exception {

        prefix = prefix.toLowerCase();
        if (tableStructure == null) {
            return;
        }
        IUniqueKey[] keys = tableStructure.getUniqueKeys();
        for (IUniqueKey key : keys) {
            addToResult(result, key, documentOffset);
        }
    }

    private void addToResult(List<ICompletionProposal> result, IUniqueKey key, int documentOffset) {
        String name = key.getName();
        String displayText = name;
        CompletionProposal proposal = new CompletionProposal(name, 0, documentOffset, name.length(), IpsUIPlugin
                .getImageHandling().getImage(key), displayText, null, key.getDescription());
        result.add(proposal);
    }

}
