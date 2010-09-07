/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablestructure;

import java.util.List;

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
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
        Image image = IpsUIPlugin.getImageHandling().getImage(key);
        String localizedDescription = IpsPlugin.getMultiLanguageSupport().getLocalizedDescription(key);
        CompletionProposal proposal = new CompletionProposal(name, 0, documentOffset, name.length(), image,
                displayText, null, localizedDescription);
        result.add(proposal);
    }

}
