/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablestructure;

import java.util.stream.Stream;

import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.faktorips.devtools.core.ui.controls.contentproposal.AbstractPrefixContentProposalProvider;
import org.faktorips.devtools.model.tablestructure.IIndex;

/**
 * A {@link IContentProposalProvider} appropriate for the textual dialog, for example
 * {@link ForeignKeyEditDialog} field.
 */
public class UniqueKeysProposalProvider extends AbstractPrefixContentProposalProvider {

    private final ForeignKeyPMO pmo;

    public UniqueKeysProposalProvider(ForeignKeyPMO pmo) {
        this.pmo = pmo;
    }

    @Override
    public IContentProposal[] getProposals(String prefix) {
        if (pmo.getAvailableUniqueKeys() != null) {
            String lowerCasePrefix = prefix.toLowerCase();
            return Stream.of(pmo.getAvailableUniqueKeys())
                    .map(IIndex::getName)
                    .filter(n -> n.toLowerCase().startsWith(lowerCasePrefix))
                    .map(ContentProposal::new)
                    .toArray(IContentProposal[]::new);
        } else {
            return EMPTY_PROPOSALS;
        }
    }

}
