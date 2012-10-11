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
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.ui.dialogs.SearchPattern;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

public abstract class AbstractIpsSrcFileContentProposalProvider implements ICachedContentProposalProvider {

    private IIpsSrcFile[] ipsSrcFiles;
    private SearchPattern searchPattern = new SearchPattern();
    private IFilter filter;

    public AbstractIpsSrcFileContentProposalProvider() {
        super();
    }

    public void setFilter(IFilter filter) {
        this.filter = filter;
    }

    @Override
    public final IContentProposal[] getProposals(String contents, int position) {

        checkIpsSrcFiles();

        String prefix = StringUtils.left(contents, position);
        searchPattern.setPattern(prefix);
        List<IContentProposal> result = new ArrayList<IContentProposal>();
        for (IIpsSrcFile ipsSrcFile : ipsSrcFiles) {
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

    /**
     * Checks, if there are cached {@link IIpsSrcFile source files}
     */
    protected void checkIpsSrcFiles() {
        if (ipsSrcFiles == null) {
            ipsSrcFiles = findIpsSrcFiles();
            if (ipsSrcFiles == null) {
                ipsSrcFiles = new IIpsSrcFile[0];
            }
        }
    }

    /**
     * finds all {@link IIpsSrcFile source files} for this proposal provider.
     */
    protected abstract IIpsSrcFile[] findIpsSrcFiles();

    @Override
    public void clearCache() {
        ipsSrcFiles = null;
    }

}