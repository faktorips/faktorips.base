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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.faktorips.devtools.core.ui.controls.contentproposal.AbstractPrefixContentProposalProvider;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.util.ArgumentCheck;

/**
 * A content proposal provider that searches for associations for a given {@link IPolicyCmptType
 * policy component type} .
 */
public class AssociationContentProposalProvider extends AbstractPrefixContentProposalProvider {

    private IPolicyCmptType pcType;

    /** indicates that only associations and composition should be searched */
    private boolean onlyAssociationOrComposition;

    public AssociationContentProposalProvider() {
        // Provides default constructor
    }

    /**
     * @param pcType The {@link IPolicyCmptType policy component type} the associations will be
     *            searched for
     * @param onlyAssoziationOrComposition <code>true</code> indicates that only associations and
     *            composition should be searched, <code>false</code> all association will be
     *            searched
     */
    public AssociationContentProposalProvider(IPolicyCmptType pcType, boolean onlyAssoziationOrComposition) {
        ArgumentCheck.notNull(pcType);
        this.pcType = pcType;
        this.onlyAssociationOrComposition = onlyAssoziationOrComposition;
    }

    @Override
    public IContentProposal[] getProposals(String prefix) {
        List<IContentProposal> proposals = new ArrayList<>();
        IPolicyCmptType currentPcType = pcType;

        while (currentPcType != null) {
            List<IPolicyCmptTypeAssociation> associations = currentPcType.getPolicyCmptTypeAssociations();
            for (IPolicyCmptTypeAssociation association : associations) {
                if (onlyAssociationOrComposition
                        && !(association.isAssoziation() || association.isCompositionMasterToDetail())) {
                    continue;
                }

                if (association.getName().toLowerCase().startsWith(prefix.toLowerCase())) {
                    addToProposals(proposals, association);
                }
            }
            currentPcType = (IPolicyCmptType)currentPcType.findSupertype(currentPcType.getIpsProject());
        }

        return proposals.toArray(new IContentProposal[0]);
    }

    private void addToProposals(List<IContentProposal> proposals, IPolicyCmptTypeAssociation association) {
        String name = association.getName();
        String localizedDescription = IIpsModel.get().getMultiLanguageSupport().getLocalizedDescription(association);

        IContentProposal proposal = new ContentProposal(name, name, localizedDescription);
        proposals.add(proposal);
    }
}
