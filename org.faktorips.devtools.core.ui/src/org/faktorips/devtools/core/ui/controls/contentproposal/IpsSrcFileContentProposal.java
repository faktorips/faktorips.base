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
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

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
        String localizedDescription = IIpsModel.get().getMultiLanguageSupport().getLocalizedDescription(
                getIpsSrcFile().getIpsObject());
        return localizedDescription.isEmpty() ? null : localizedDescription;
    }

    /**
     * @return Returns the ipsSrcFile.
     */
    public IIpsSrcFile getIpsSrcFile() {
        return ipsSrcFile;
    }

}
