/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.type;

import java.util.List;

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.ui.AbstractCompletionProcessor;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.util.ArgumentCheck;

/**
 * A completion processor that searchs for derived unions for a given associations. The search is
 * done along the supertype hierarchy starting with the type the given association belongs to.
 */
public class DerivedUnionCompletionProcessor extends AbstractCompletionProcessor {

    private IAssociation association;

    public DerivedUnionCompletionProcessor(IAssociation association) {
        ArgumentCheck.notNull(association);

        this.association = association;

        setIpsProject(association.getIpsProject());
    }

    @Override
    protected void doComputeCompletionProposals(String prefix, int documentOffset, List<ICompletionProposal> result)
            throws Exception {
        IAssociation[] derivedUnionCandidates = association.findDerivedUnionCandidates(association.getIpsProject());
        for (IAssociation derivedUnionCandidate : derivedUnionCandidates) {
            if (derivedUnionCandidate.getName().toLowerCase().startsWith(prefix.toLowerCase())) {
                addToResult(result, derivedUnionCandidate, documentOffset);
            }
        }
    }

    private void addToResult(List<ICompletionProposal> result, IAssociation relation, int documentOffset) {
        String name = relation.getName();
        String displayText = name + " - " + relation.getParent().getName(); //$NON-NLS-1$
        String localizedDescription = IpsPlugin.getMultiLanguageSupport().getLocalizedDescription(relation);
        Image image = IpsUIPlugin.getImageHandling().getImage(relation);
        ICompletionProposal proposal = new CompletionProposal(name, 0, documentOffset, name.length(), image,
                displayText, null, localizedDescription);
        result.add(proposal);
    }
}
