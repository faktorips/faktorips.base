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
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
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

        private String getCardinalityString(boolean showDefault,
                int minCardinality,
                int maxCardinality,
                int defaultCardinality) {
            StringBuilder cardinalityString = new StringBuilder(" ["); //$NON-NLS-1$
            cardinalityString.append(minCardinality);
            cardinalityString.append(".."); //$NON-NLS-1$
            cardinalityString.append(maxCardinality == IAssociation.CARDINALITY_MANY ? "*" : maxCardinality); //$NON-NLS-1$
            if (showDefault) {
                cardinalityString.append(", "); //$NON-NLS-1$
                cardinalityString.append(defaultCardinality);
            }
            cardinalityString.append("]"); //$NON-NLS-1$
            return cardinalityString.toString();
        }

        private String getCardinalitiesFromPolicy(IAssociation association) {
            String policyCardinalityString = ""; //$NON-NLS-1$
            try {
                IProductCmptTypeAssociation productCmptAssociation = null;
                IPolicyCmptTypeAssociation policyAssociation = null;

                if (association instanceof IProductCmptTypeAssociation) {
                    productCmptAssociation = (IProductCmptTypeAssociation)association;
                    policyAssociation = productCmptAssociation
                            .findMatchingPolicyCmptTypeAssociation(productCmptAssociation.getIpsProject());
                } else if (association instanceof IPolicyCmptTypeAssociation) {
                    policyAssociation = (IPolicyCmptTypeAssociation)association;
                }
                if (policyAssociation != null) {
                    policyCardinalityString = getCardinalityString(false, policyAssociation.getMinCardinality(),
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
                IAssociation association = associationViewItem.getAssociation();
                styledString.append(new StyledString(getCardinalitiesFromPolicy(association),
                        StyledString.COUNTER_STYLER));
            }
            return styledString;
        }
    }
}
