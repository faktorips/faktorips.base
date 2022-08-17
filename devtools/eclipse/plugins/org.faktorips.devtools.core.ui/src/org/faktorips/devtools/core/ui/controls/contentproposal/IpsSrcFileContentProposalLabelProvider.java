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

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.ui.LocalizedLabelProvider;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

/**
 * The label provider for {@link IpsSrcFileContentProposal}. Loads the localized label and the image
 * of the IPS object included in the {@link IIpsSrcFile}.
 * 
 * @author dirmeier
 */
public class IpsSrcFileContentProposalLabelProvider extends LabelProvider {

    private LabelProvider internalLabelProvider = new LocalizedLabelProvider();

    @Override
    public String getText(Object element) {
        if (element instanceof IpsSrcFileContentProposal) {
            return internalLabelProvider.getText(((IpsSrcFileContentProposal)element).getIpsSrcFile());
        } else {
            return super.getText(element);
        }
    }

    @Override
    public Image getImage(Object element) {
        if (element instanceof IpsSrcFileContentProposal) {
            IpsSrcFileContentProposal proposal = (IpsSrcFileContentProposal)element;
            return internalLabelProvider.getImage(proposal.getIpsSrcFile());
        } else {
            return super.getImage(element);
        }

    }
}
