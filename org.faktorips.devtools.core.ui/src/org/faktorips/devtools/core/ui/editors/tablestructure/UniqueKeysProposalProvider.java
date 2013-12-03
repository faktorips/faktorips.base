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
 * A {@link IContentProposalProvider} appropriate for the textual dialog, for example
 * {@link ForeignKeyEditDialog} field.
 * 
 */

public class UniqueKeysProposalProvider implements IContentProposalProvider {
    private IContentProposal[] EMPTY_PROPOSALS = new IContentProposal[0];
    private IIndex[] uniqueKeys;

    @Override
    public IContentProposal[] getProposals(String contents, int position) {
        String content = StringUtils.left(contents, position);
        if (uniqueKeys != null) {
            List<IContentProposal> proposals = getProposals(content);
            return proposals.toArray(new IContentProposal[proposals.size()]);
        } else {
            return EMPTY_PROPOSALS;
        }
    }

    private List<IContentProposal> getProposals(String content) {
        List<IContentProposal> proposals = new ArrayList<IContentProposal>();
        for (IIndex uniqueKey : uniqueKeys) {
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

    protected void setUniqueKeys(IIndex[] newUniqueKeys) {
        this.uniqueKeys = newUniqueKeys;
    }

}
