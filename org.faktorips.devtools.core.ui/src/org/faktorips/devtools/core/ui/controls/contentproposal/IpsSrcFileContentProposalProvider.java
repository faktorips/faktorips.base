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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.viewers.IFilter;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * A {@link IContentProposalProvider} for {@link IIpsSrcFile} proposals. The provider load all
 * source files of a given type within a given {@link IIpsProject}.
 * 
 * @author dirmeier
 * 
 */
public class IpsSrcFileContentProposalProvider extends AbstractIpsSrcFileContentProposalProvider {

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
    protected IIpsSrcFile[] findIpsSrcFiles() {
        try {
            return ipsProject.findIpsSrcFiles(ipsObjectType);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

}
