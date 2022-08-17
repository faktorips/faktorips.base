/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.ui.LocalizedLabelProvider;

/**
 * The label provider for {@link DatatypeContentProposal}.
 * 
 * @see DatatypeContentProposal
 * 
 * @author hbaagil
 */
public class DatatypeContentProposalLabelProvider extends LabelProvider {

    private LabelProvider internalLabelProvider = new LocalizedLabelProvider();

    @Override
    public String getText(Object element) {
        if (element instanceof DatatypeContentProposal) {
            return ((DatatypeContentProposal)element).getDataype().getName();
        } else {
            return super.getText(element);
        }
    }

    @Override
    public Image getImage(Object element) {
        if (element instanceof DatatypeContentProposal) {
            DatatypeContentProposal proposal = (DatatypeContentProposal)element;
            return internalLabelProvider.getImage(proposal.getDataype());
        } else {
            return super.getImage(element);
        }
    }
}
