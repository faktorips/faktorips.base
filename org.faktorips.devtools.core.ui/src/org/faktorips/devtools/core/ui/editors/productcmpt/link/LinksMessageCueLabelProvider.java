/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt.link;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.MessageCueLabelProvider;
import org.faktorips.util.StringUtil;
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
        if (element instanceof AbstractAssociationViewItem) {
            AbstractAssociationViewItem viewItem = (AbstractAssociationViewItem)element;
            IProductCmpt productCmpt = viewItem.getProductCmpt();
            MessageList msgList = productCmpt.validate(productCmpt.getIpsProject());
            return msgList.getMessagesFor(viewItem.getAssociationName());
        }
        if (element instanceof LinkViewItem) {
            LinkViewItem viewItem = (LinkViewItem)element;
            return super.getMessages(viewItem.getLink());
        }
        return super.getMessages(element);
    }

    private static class InternalLabelProvider extends LabelProvider implements IStyledLabelProvider {

        public InternalLabelProvider() {
        }

        private String getCardinalitiesFromPolicy(IProductCmptTypeAssociation association) {
            String policyCardinalityString = ""; //$NON-NLS-1$
            try {
                IPolicyCmptTypeAssociation policyAssociation = association
                        .findMatchingPolicyCmptTypeAssociation(association.getIpsProject());
                if (policyAssociation != null) {
                    policyCardinalityString = StringUtil.getRangeString(false, policyAssociation.getMinCardinality(),
                            policyAssociation.getMaxCardinality(), 0);
                }
            } catch (CoreException e) {
                // Ignore, because in this case we simply do not show any cardinality
                // information.
            }
            return policyCardinalityString;
        }

        @Override
        public String getText(Object element) {
            if (element instanceof ILinkSectionViewItem) {
                return ((ILinkSectionViewItem)element).getText();
            }
            return element.toString();
        }

        @Override
        public Image getImage(Object element) {
            if (element instanceof ILinkSectionViewItem) {
                return ((ILinkSectionViewItem)element).getImage();
            }
            return IpsUIPlugin.getImageHandling().getImage(ImageDescriptor.getMissingImageDescriptor());
        }

        @Override
        public StyledString getStyledText(Object element) {
            StyledString styledString = new StyledString(getText(element));
            if (element instanceof AssociationViewItem) {
                AssociationViewItem associationViewItem = (AssociationViewItem)element;
                IProductCmptTypeAssociation association = associationViewItem.getAssociation();
                styledString.append(new StyledString(getCardinalitiesFromPolicy(association),
                        StyledString.COUNTER_STYLER));
            }
            return styledString;
        }
    }
}
