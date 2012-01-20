/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.product.conditions.table;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.ui.internal.ContentProposal;
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

        List<IContentProposal> proposals = new ArrayList<IContentProposal>();

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