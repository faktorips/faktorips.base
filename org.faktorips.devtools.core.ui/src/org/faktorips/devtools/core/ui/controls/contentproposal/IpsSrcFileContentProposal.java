/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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