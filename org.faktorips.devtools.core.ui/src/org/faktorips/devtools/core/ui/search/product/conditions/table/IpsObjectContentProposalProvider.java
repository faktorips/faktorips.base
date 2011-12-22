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
import org.faktorips.devtools.core.ui.internal.ContentProposal;

final class IpsObjectContentProposalProvider implements IContentProposalProvider {
    private static final String QUALIFICATION_SEPARATOR = "."; //$NON-NLS-1$
    private final List<String> ipsObjects;

    IpsObjectContentProposalProvider(List<String> ipsObjects) {
        this.ipsObjects = ipsObjects;
    }

    @Override
    public IContentProposal[] getProposals(String contents, int position) {
        String content = StringUtils.left(contents, position);

        List<IContentProposal> proposals = new ArrayList<IContentProposal>();

        for (String value : ipsObjects) {
            boolean startsWith = isFittingContent(value, content);

            if (startsWith) {
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
        if (valueLowerCase.contains(QUALIFICATION_SEPARATOR)) {
            return StringUtils.substringAfterLast(valueLowerCase, QUALIFICATION_SEPARATOR).startsWith(contentLowerCase);
        }
        return false;
    }
}