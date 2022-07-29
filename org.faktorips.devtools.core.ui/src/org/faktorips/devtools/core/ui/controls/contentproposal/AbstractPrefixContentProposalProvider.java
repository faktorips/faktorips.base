/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls.contentproposal;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

/**
 * Abstract implementation of an {@link IContentProposalProvider} that cares only about the prefix.
 */
public abstract class AbstractPrefixContentProposalProvider implements IContentProposalProvider {

    protected static final IContentProposal[] EMPTY_PROPOSALS = new IContentProposal[0];

    @Override
    public IContentProposal[] getProposals(String contents, int position) {
        String prefix = getPrefixFor(contents, position);
        return getProposals(prefix);
    }

    protected String getPrefixFor(String contents, int position) {
        return StringUtils.left(contents, position);
    }

    /**
     * Return an array of content proposals representing the valid proposals for a field.
     *
     * @param prefix the current contents of the text field up to the cursor position
     *
     * @return the array of valid {@link IContentProposal proposals} for the field.
     */
    protected abstract IContentProposal[] getProposals(String prefix);
}