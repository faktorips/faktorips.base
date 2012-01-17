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

package org.faktorips.devtools.core.ui.controls.contentproposal;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.ui.dialogs.SearchPattern;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.IIpsSrcFilesChangeListener;
import org.faktorips.devtools.core.model.IpsSrcFilesChangedEvent;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

public class IpsSrcFileContentProposalProvider implements IContentProposalProvider {

    private IIpsSrcFile[] ipsSrcFiles;

    private SearchPattern searchPattern = new SearchPattern();

    private IFilter filter;

    private final IIpsProject ipsProject;

    private final IpsObjectType ipsObjectType;

    private IIpsSrcFilesChangeListener listener;

    /**
     * A content proposal provider for IIpsSrcFiled. The proposal provider searches for all
     * {@link IIpsSrcFile} in the given project of the given type.
     * <p>
     * If you want to set an filter to further decrease the list of proposals, use the method
     * {@link #setFilter(IFilter)}
     * <p>
     * Note: You have to call the dispose method when you do not need the proposal provider anymore!
     * 
     * @param ipsProject The project to search the files
     * @param ipsObjectType the type of the objects
     */
    public IpsSrcFileContentProposalProvider(IIpsProject ipsProject, IpsObjectType ipsObjectType) {
        this.ipsProject = ipsProject;
        this.ipsObjectType = ipsObjectType;
        listener = new IIpsSrcFilesChangeListener() {

            @Override
            public void ipsSrcFilesChanged(IpsSrcFilesChangedEvent event) {
                ipsSrcFiles = null;
            }
        };
        IpsPlugin.getDefault().getIpsModel().addIpsSrcFilesChangedListener(listener);
    }

    public void setFilter(IFilter filter) {
        this.filter = filter;
    }

    @Override
    public IContentProposal[] getProposals(String contents, int position) {
        if (ipsSrcFiles == null) {
            try {
                ipsSrcFiles = ipsProject.findIpsSrcFiles(ipsObjectType);
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }
        String prefix = StringUtils.left(contents, position);
        searchPattern.setPattern(prefix);
        List<IContentProposal> result = new ArrayList<IContentProposal>();
        for (IIpsSrcFile ipsSrcFile : ipsSrcFiles) {
            if (filter == null || filter.select(ipsSrcFile)) {
                String unqualifiedName = ipsSrcFile.getIpsObjectName();
                if (searchPattern.matches(unqualifiedName)) {
                    String description = null;
                    try {
                        description = IpsPlugin.getMultiLanguageSupport().getLocalizedDescription(
                                ipsSrcFile.getIpsObject());
                        if (description.isEmpty()) {
                            // better no description field but an empty description field
                            description = null;
                        }
                    } catch (CoreException e) {
                        // Ignore exception - we do not need the description necessarily
                    }
                    IpsSrcFileContentProposal contentProposal = new IpsSrcFileContentProposal(ipsSrcFile);
                    result.add(contentProposal);
                }
            }
        }
        return result.toArray(new IContentProposal[result.size()]);
    }

    public void dispose() {
        IpsPlugin.getDefault().getIpsModel().removeIpsSrcFilesChangedListener(listener);
    }

}
