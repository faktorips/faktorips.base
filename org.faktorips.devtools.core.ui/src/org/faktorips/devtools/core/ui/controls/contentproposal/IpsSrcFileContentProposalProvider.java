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

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.viewers.IFilter;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * A {@link IContentProposalProvider} for {@link IIpsSrcFile} proposals. The provider load all
 * source files of a given type within a given {@link IIpsProject}.
 * 
 * @author dirmeier
 * 
 */
public class IpsSrcFileContentProposalProvider extends AbstractIpsSrcFileContentProposalProvider implements
        ICachedContentProposalProvider {

    private IIpsSrcFile[] ipsSrcFiles;
    private final IIpsProject ipsProject;
    private final IpsObjectType ipsObjectType;

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
    }

    @Override
    public IContentProposal[] getProposals(String contents, int position) {
        checkIpsSrcFiles();
        return super.getProposals(contents, position);
    }

    protected IIpsSrcFile[] findIpsSrcFiles() {
        return ipsProject.findIpsSrcFiles(ipsObjectType);
    }

    /**
     * Checks, if there are cached {@link IIpsSrcFile source files}
     */
    protected void checkIpsSrcFiles() {
        if (getIpsSrcFiles() == null) {
            setIpsSrcFiles(findIpsSrcFiles());
        }
    }

    @Override
    public void clearCache() {
        setIpsSrcFiles(null);
    }

    @Override
    public IIpsSrcFile[] getIpsSrcFiles() {
        return ipsSrcFiles;
    }

    public void setIpsSrcFiles(IIpsSrcFile[] ipsSrcFiles) {
        this.ipsSrcFiles = ipsSrcFiles;
    }
}
