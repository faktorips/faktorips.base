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

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.ui.LocalizedLabelProvider;

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