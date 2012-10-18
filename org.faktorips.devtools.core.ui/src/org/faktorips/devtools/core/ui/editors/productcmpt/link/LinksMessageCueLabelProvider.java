/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt.link;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.internal.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.MessageCueLabelProvider;
import org.faktorips.util.message.MessageList;

/**
 * Provides labels for links. {@link IProductCmptLink}s are displayed as the target object including
 * a special cue label provider to get messages from the generations instead of the link itself.
 * 
 * @author Thorsten Guenther
 * @author Cornelius Dirmeier
 * @author Stefan Widmaier
 */
public class LinksMessageCueLabelProvider extends MessageCueLabelProvider {

    public LinksMessageCueLabelProvider(IIpsProject ipsProject) {
        super(new InternalLabelProvider(), ipsProject);
    }

    @Override
    public MessageList getMessages(Object element) throws CoreException {
        if (element instanceof DetachedAssociationViewItem) {
            DetachedAssociationViewItem viewItem = (DetachedAssociationViewItem)element;
            IProductCmptLinkContainer linkContainer = viewItem.getLinkContainer();
            return linkContainer.validate(linkContainer.getIpsProject()).getMessagesFor(viewItem.getAssociationName());
        }
        if (element instanceof AssociationViewItem) {
            AssociationViewItem viewItem = (AssociationViewItem)element;
            return super.getMessages(viewItem.getAssociation());
        }
        if (element instanceof LinkViewItem) {
            LinkViewItem viewItem = (LinkViewItem)element;
            return super.getMessages(viewItem.getLink());
        }
        return super.getMessages(element);
    }

    private static class InternalLabelProvider extends LabelProvider {

        public InternalLabelProvider() {
        }

        @Override
        public String getText(Object element) {
            if (element instanceof LinkSectionViewItem) {
                return ((LinkSectionViewItem)element).getText();
            }
            return element.toString();
        }

        @Override
        public Image getImage(Object element) {
            if (element instanceof LinkSectionViewItem) {
                return ((LinkSectionViewItem)element).getImage();
            }
            return IpsUIPlugin.getImageHandling().getImage(ImageDescriptor.getMissingImageDescriptor());
        }
    }
}
