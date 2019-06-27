/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablestructure;

import java.util.List;

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.tablestructure.IIndex;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.AbstractCompletionProcessor;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * A completion processor for a table structure's indices.
 */
public class IndexCompletionProcessor extends AbstractCompletionProcessor {

    private ITableStructure tableStructure;

    public IndexCompletionProcessor() {
        super();
        setComputeProposalForEmptyPrefix(true);
    }

    /**
     * Sets the table structure which indices should be completed. If <code>null</code> is passed,
     * no completion is available.
     */
    public void setTableStructure(ITableStructure structure) {
        tableStructure = structure;
        if (tableStructure != null) {
            setIpsProject(tableStructure.getIpsProject());
        }
    }

    @Override
    protected void doComputeCompletionProposals(final String prefix,
            int documentOffset,
            List<ICompletionProposal> result) throws Exception {

        if (tableStructure == null) {
            return;
        }
        IIndex[] keys = tableStructure.getUniqueKeys();
        for (IIndex key : keys) {
            addToResult(result, key, documentOffset);
        }
    }

    private void addToResult(List<ICompletionProposal> result, IIndex key, int documentOffset) {
        String name = key.getName();
        String displayText = name;
        Image image = IpsUIPlugin.getImageHandling().getImage(key);
        String localizedDescription = IpsPlugin.getMultiLanguageSupport().getLocalizedDescription(key);
        CompletionProposal proposal = new CompletionProposal(name, 0, documentOffset, name.length(), image,
                displayText, null, localizedDescription);
        result.add(proposal);
    }

}
