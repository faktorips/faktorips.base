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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.ui.dialogs.SearchPattern;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

/**
 * Abstract implementation of an {@link IContentProposalProvider} for {@link IIpsSrcFile}. A source
 * file is part of the proposal, if its unqualified name fits the given arguments in the method
 * {@link #getProposals(String, int)}.
 * <p>
 * The result could be filtered, if an {@link IFilter} is set.
 * <p>
 * Subclasses must implement {@link #getIpsSrcFiles()}.
 * 
 * @author dicker
 */
public abstract class AbstractIpsSrcFileContentProposalProvider extends AbstractPrefixContentProposalProvider {

    private SearchPattern searchPattern = new SearchPattern();
    private IFilter filter;

    @Override
    public IContentProposal[] getProposals(String prefix) {
        searchPattern.setPattern(prefix);
        List<IContentProposal> result = new ArrayList<>();
        for (IIpsSrcFile ipsSrcFile : getIpsSrcFiles()) {
            if (ipsSrcFile.exists() && (filter == null || filter.select(ipsSrcFile))) {
                String unqualifiedName = ipsSrcFile.getIpsObjectName();
                if (searchPattern.matches(unqualifiedName)) {
                    IpsSrcFileContentProposal contentProposal = new IpsSrcFileContentProposal(ipsSrcFile);
                    result.add(contentProposal);
                }
            }
        }
        return result.toArray(new IContentProposal[result.size()]);
    }

    protected abstract IIpsSrcFile[] getIpsSrcFiles();

    public void setFilter(IFilter filter) {
        this.filter = filter;
    }

    public IFilter getFilter() {
        return filter;
    }
}
