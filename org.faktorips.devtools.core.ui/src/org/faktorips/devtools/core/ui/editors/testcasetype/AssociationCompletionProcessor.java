/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcasetype;

import java.util.List;

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.ui.AbstractCompletionProcessor;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.util.ArgumentCheck;

/**
 * A completion processor that searchs for associations for a given policy cmpt type.
 */
public class AssociationCompletionProcessor extends AbstractCompletionProcessor {

    private IPolicyCmptType pcType;

    /** indicates that only assoziations and composition should be searched */
    private boolean onlyAssoziationOrComposition;

    public AssociationCompletionProcessor() {
        // Provides default constructor
    }

    /**
     * @param pcType The policy cmpt type the associations will be searched for
     * @param onlyAssoziationOrComposition <code>true</code> indicates that only assoziations and
     *            composition should be searched, <code>false</code> all association will be
     *            searched
     */
    public AssociationCompletionProcessor(IPolicyCmptType pcType, boolean onlyAssoziationOrComposition) {
        ArgumentCheck.notNull(pcType);
        this.pcType = pcType;
        this.onlyAssoziationOrComposition = onlyAssoziationOrComposition;
        setIpsProject(pcType.getIpsProject());
    }

    @Override
    protected void doComputeCompletionProposals(String prefix, int documentOffset, List<ICompletionProposal> result)
            throws Exception {

        prefix = prefix.toLowerCase();

        IPolicyCmptType currentPcType = pcType;
        while (currentPcType != null) {
            List<IPolicyCmptTypeAssociation> associations = currentPcType.getPolicyCmptTypeAssociations();
            for (IPolicyCmptTypeAssociation association : associations) {
                if (onlyAssoziationOrComposition
                        && !(association.isAssoziation() || association.isCompositionMasterToDetail())) {
                    continue;
                }

                if (association.getName().toLowerCase().startsWith(prefix)) {
                    addToResult(result, association, documentOffset);
                }
            }
            currentPcType = (IPolicyCmptType)currentPcType.findSupertype(currentPcType.getIpsProject());
        }
    }

    private void addToResult(List<ICompletionProposal> result,
            IPolicyCmptTypeAssociation association,
            int documentOffset) {

        String name = association.getName();
        String displayText = name + " - " + association.getParent().getName(); //$NON-NLS-1$
        Image image = IpsUIPlugin.getImageHandling().getImage(association);
        String localizedDescription = IpsPlugin.getMultiLanguageSupport().getLocalizedDescription(association);
        CompletionProposal proposal = new CompletionProposal(name, 0, documentOffset, name.length(), image,
                displayText, null, localizedDescription);
        result.add(proposal);
    }
}
