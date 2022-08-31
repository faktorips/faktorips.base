/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.product.conditions.table;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.faktorips.devtools.core.ui.internal.ContentProposal;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.util.StringUtil;

/**
 * This is an {@link IContentProposalProvider} for {@link IIpsObject IIpsObjects}.
 * <p>
 * The possible proposals are within a List of Strings. Depending on the given content and given
 * position in the method {@link #getProposals(String, int)} an element of this list can become a
 * proposal:
 * <ul>
 * <li>If the content fits the beginning of the qualified name.</li>
 * <li>If the content fits the beginning of the name.</li>
 * </ul>
 * The algorithm is case insensitive.
 * 
 * @author dicker
 */
final class IpsObjectContentProposalProvider implements IContentProposalProvider {
    private final List<String> ipsObjects;

    /**
     * The constructor of IpsObjectContentProposalProvider with a List of Strings as argument.
     * 
     * @param ipsObjects a List of Strings representing the qualified names of IIpsObjects.
     */
    IpsObjectContentProposalProvider(List<String> ipsObjects) {
        this.ipsObjects = ipsObjects;
    }

    @Override
    public IContentProposal[] getProposals(String contents, int position) {
        String content = StringUtils.left(contents, position);

        List<IContentProposal> proposals = new ArrayList<>();

        for (String value : ipsObjects) {
            if (isFittingContent(value, content)) {
                proposals.add(new ContentProposal(value, value, value));
            }
        }

        return proposals.toArray(new IContentProposal[0]);
    }

    private boolean isFittingContent(String value, String content) {
        String valueLowerCase = value.toLowerCase();
        String contentLowerCase = content.toLowerCase();

        if (valueLowerCase.startsWith(contentLowerCase)) {
            return true;
        }
        return StringUtil.unqualifiedName(valueLowerCase).startsWith(contentLowerCase);
    }
}
