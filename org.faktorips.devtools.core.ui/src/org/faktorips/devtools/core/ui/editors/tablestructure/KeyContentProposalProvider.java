/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
 * 
 */
public class KeyContentProposalProvider implements IContentProposalProvider {
    List<IContentProposal> proposals;
    private IIndex[] uniqueKeys;

    public KeyContentProposalProvider(IIndex[] uniqueKeys) {
        this.uniqueKeys = uniqueKeys;
    }

    @Override
    public IContentProposal[] getProposals(String contents, int position) {
        proposals = new ArrayList<IContentProposal>();
        String content = StringUtils.left(contents, position);

        for (IIndex uniqueKey : uniqueKeys) {
            if (isFittingContent(uniqueKey, content)) {
                String name = uniqueKey.getName();
                proposals.add(new ContentProposal(name, name, name));
            }
        }
        return proposals.toArray(new IContentProposal[proposals.size()]);
    }

    private boolean isFittingContent(IIndex uniqueKey, String content) {
        String uniqueKeyName = uniqueKey.getName().toLowerCase();
        return uniqueKeyName.startsWith(content.toLowerCase());
    }

    public void setUniquekeys(IIndex[] allowedContent) {
        uniqueKeys = allowedContent;
    }
}
