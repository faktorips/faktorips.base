/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablestructure;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.faktorips.devtools.core.model.tablestructure.IIndex;
import org.faktorips.devtools.core.ui.internal.ContentProposal;

/**
 * 
 * A {@link IContentProposalProvider} appropriate for the textual dialog, for example
 * {@link ForeignKeyEditDialog} field.
 * 
 */

public class UniqueKeysProposalProvider implements IContentProposalProvider {

    private static final IContentProposal[] EMPTY_PROPOSALS = new IContentProposal[0];

    private final ForeignKeyPMO pmo;

    public UniqueKeysProposalProvider(ForeignKeyPMO pmo) {
        this.pmo = pmo;
    }

    @Override
    public IContentProposal[] getProposals(String contents, int position) {
        String content = StringUtils.left(contents, position);
        if (pmo.getAvailableUniqueKeys() != null) {
            List<IContentProposal> proposals = getProposals(content);
            return proposals.toArray(new IContentProposal[proposals.size()]);
        } else {
            return EMPTY_PROPOSALS;
        }
    }

    private List<IContentProposal> getProposals(String content) {
        List<IContentProposal> proposals = new ArrayList<IContentProposal>();
        IIndex[] availableUniqueKeys = pmo.getAvailableUniqueKeys();
        for (IIndex uniqueKey : availableUniqueKeys) {
            if (isMatchingContent(uniqueKey, content)) {
                String name = uniqueKey.getName();
                proposals.add(new ContentProposal(name, name, name));
            }
        }
        return proposals;
    }

    private boolean isMatchingContent(IIndex uniqueKey, String content) {
        String uniqueKeyName = uniqueKey.getName().toLowerCase();
        return uniqueKeyName.startsWith(content.toLowerCase());
    }

}
