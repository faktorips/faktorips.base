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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

/**
 * A content proposal for {@link IIpsSrcFile IPS source files}. The label and image of these
 * proposals could be provided by the {@link IpsSrcFileContentProposalLabelProvider}.
 * 
 * @author dirmeier
 */
class IpsSrcFileContentProposal implements IContentProposal {

    private final IIpsSrcFile ipsSrcFile;

    public IpsSrcFileContentProposal(IIpsSrcFile ipsSrcFile) {
        this.ipsSrcFile = ipsSrcFile;
    }

    @Override
    public String getContent() {
        return getIpsSrcFile().getQualifiedNameType().getName();
    }

    @Override
    public int getCursorPosition() {
        return getContent().length();
    }

    @Override
    public String getLabel() {
        return getIpsSrcFile().getIpsObjectName();
    }

    @Override
    public String getDescription() {
        try {
            String localizedDescription = IpsPlugin.getMultiLanguageSupport().getLocalizedDescription(
                    getIpsSrcFile().getIpsObject());
            return localizedDescription.isEmpty() ? null : localizedDescription;
        } catch (CoreException e) {
            // ignore errors - just show no description
        }
        return null;
    }

    /**
     * @return Returns the ipsSrcFile.
     */
    public IIpsSrcFile getIpsSrcFile() {
        return ipsSrcFile;
    }

}