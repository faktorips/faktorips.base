/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.type;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.faktorips.devtools.core.ui.controls.contentproposal.AbstractPrefixContentProposalProvider;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.util.ArgumentCheck;

/**
 * A content proposal provider that searches for derived unions for a given associations. The search
 * is done along the supertype hierarchy starting with the type the given association belongs to.
 */
public class DerivedUnionContentProposalProvider extends AbstractPrefixContentProposalProvider {

    private IAssociation association;

    public DerivedUnionContentProposalProvider(IAssociation association) {
        ArgumentCheck.notNull(association);

        this.association = association;
    }

    @Override
    public IContentProposal[] getProposals(String prefix) {
        List<IContentProposal> proposals = new ArrayList<>();
        IAssociation[] derivedUnionCandidates = association.findDerivedUnionCandidates(association.getIpsProject());

        for (IAssociation derivedUnionCandidate : derivedUnionCandidates) {
            if (derivedUnionCandidate.getName().toLowerCase().startsWith(prefix.toLowerCase())) {
                addToProposals(proposals, derivedUnionCandidate);
            }
        }

        return proposals.toArray(new IContentProposal[0]);
    }

    private void addToProposals(List<IContentProposal> proposals, IAssociation relation) {
        String name = relation.getName();
        String localizedDescription = IIpsModel.get().getMultiLanguageSupport().getLocalizedDescription(relation);

        IContentProposal proposal = new ContentProposal(name, name, localizedDescription);
        proposals.add(proposal);
    }
}
