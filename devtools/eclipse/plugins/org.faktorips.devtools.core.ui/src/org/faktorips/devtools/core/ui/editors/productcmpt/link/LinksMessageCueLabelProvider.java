/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt.link;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.MessageCueLabelProvider;
import org.faktorips.devtools.core.ui.internal.IpsStyler;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IPolicyCmptLinkCardinality;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.util.StringUtil;

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
    public MessageList getMessages(Object element) {
        if (element instanceof AbstractAssociationViewItem viewItem) {
            var linkContainer = viewItem.getLinkContainer();
            MessageList msgList = linkContainer.validate(linkContainer.getIpsProject());
            return msgList.getMessagesFor(viewItem.getAssociationName());
        }
        if (element instanceof LinkViewItem viewItem) {
            return super.getMessages(viewItem.getLink());
        }
        return super.getMessages(element);
    }

    private static class InternalLabelProvider extends LabelProvider implements IStyledLabelProvider {

        public InternalLabelProvider() {
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
            if (element instanceof PolicyAssociationViewItem policyItem) {
                return getPolicyAssociationViewItemStyledText(policyItem);
            }
            if (element instanceof AssociationViewItem) {
                return getAssociationViewItemStyledText((AssociationViewItem)element);
            }
            if (element instanceof LinkViewItem) {
                return getLinkViewItemStyledString((LinkViewItem)element);
            }
            return new StyledString(getText(element));
        }

        private StyledString getPolicyAssociationViewItemStyledText(PolicyAssociationViewItem viewItem) {
            String cardinality = viewItem.findPolicyCmptLinkCardinality()
                    .map(LinksMessageCueLabelProvider.InternalLabelProvider::formatCardinality)
                    .orElseGet(() -> formatCardinality(viewItem.getAssociation().getMinCardinality(),
                            viewItem.getAssociation().getMaxCardinality()));
            StyledString nameStyledString = new StyledString(getText(viewItem));
            StyledString cardinalityStyledString = new StyledString(cardinality, IpsStyler.MODEL_CARDINALITY_STYLER);
            return nameStyledString.append(cardinalityStyledString);
        }

        private StyledString getAssociationViewItemStyledText(AssociationViewItem associationViewItem) {
            String cardinality = associationViewItem.findPolicyCmptLinkCardinality()
                    .map(LinksMessageCueLabelProvider.InternalLabelProvider::formatCardinality)
                    .orElse(IpsStringUtils.EMPTY);
            StyledString cardinalityStyledString = new StyledString(cardinality, IpsStyler.MODEL_CARDINALITY_STYLER);
            StyledString nameStyledString = new StyledString(getText(associationViewItem));
            return nameStyledString.append(cardinalityStyledString);
        }

        private static String formatCardinality(IPolicyCmptLinkCardinality c) {
            return formatCardinality(c.getMinCardinality(), c.getMaxCardinality());
        }

        private static String formatCardinality(int min, int max) {
            return StringUtil.BLANK + StringUtil.getRangeString(min, max);
        }

        private StyledString getLinkViewItemStyledString(LinkViewItem linkViewItem) {
            return new LinkViewItemLabelStyler(linkViewItem).getStyledLabel();
        }
    }

}
