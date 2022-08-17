/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.faktorips.devtools.core.ui.controls.contentproposal.AbstractPrefixContentProposalProvider;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.util.ArgumentCheck;

public class InverseAssociationContentProposalProvider extends AbstractPrefixContentProposalProvider {

    private IPolicyCmptTypeAssociation association;

    public InverseAssociationContentProposalProvider() {
        // Default constructor
    }

    public InverseAssociationContentProposalProvider(IPolicyCmptTypeAssociation association) {
        ArgumentCheck.notNull(association);
        this.association = association;
    }

    @Override
    public IContentProposal[] getProposals(String prefix) {
        String lowerPrefix = prefix.toLowerCase();
        List<IContentProposal> proposals = new ArrayList<>();
        IIpsProject ipsProject = association.getIpsObject().getIpsProject();
        IPolicyCmptType target = association.findTargetPolicyCmptType(ipsProject);

        if (target != null) {
            List<IAssociation> associationCandidates = target.findAssociationsForTargetAndAssociationType(association
                    .getPolicyCmptType().getQualifiedName(),
                    association.getAssociationType()
                            .getCorrespondingAssociationType(),
                    ipsProject, false);
            for (IAssociation associationCandidate : associationCandidates) {
                // only association with name starts with prefix
                if (associationCandidate.getName().toLowerCase().startsWith(lowerPrefix)) {
                    addToProposals(proposals, associationCandidate);
                }
            }
        }

        return proposals.toArray(new IContentProposal[0]);
    }

    private void addToProposals(List<IContentProposal> proposals, IAssociation association) {
        String name = association.getName();
        String localizedDescription = IIpsModel.get().getMultiLanguageSupport().getLocalizedDescription(association);

        IContentProposal proposal = new ContentProposal(name, name, localizedDescription);
        proposals.add(proposal);
    }
}
