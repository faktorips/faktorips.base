/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.controls.contentproposal;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.ui.dialogs.SearchPattern;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

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
public abstract class AbstractIpsSrcFileContentProposalProvider implements IContentProposalProvider {

    private SearchPattern searchPattern = new SearchPattern();
    private IFilter filter;

    @Override
    public IContentProposal[] getProposals(String contents, int position) {
        String prefix = StringUtils.left(contents, position);
        searchPattern.setPattern(prefix);
        List<IContentProposal> result = new ArrayList<IContentProposal>();
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