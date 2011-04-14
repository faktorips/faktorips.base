/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.MessageCueLabelProvider;
import org.faktorips.devtools.core.ui.workbenchadapters.AssociationWorkbenchAdapter;
import org.faktorips.util.StringUtil;
import org.faktorips.util.message.MessageList;

/**
 * Provides labels for links. {@link IProductCmptLink}s are displayed as the target object including
 * a special cue label provider to get messages from the generations instead of the link itself.
 * 
 * @author Thorsten Guenther
 * @author Cornelius Dirmeier
 */
public class LinksMessageCueLabelProvider extends MessageCueLabelProvider {

    private final IProductCmptGeneration generation;

    public LinksMessageCueLabelProvider(IProductCmptGeneration generation) {
        super(new InternalLabelProvider(generation), generation.getIpsProject());
        this.generation = generation;
    }

    @Override
    public MessageList getMessages(Object element) throws CoreException {
        if (element instanceof String) {
            return generation.validate(generation.getIpsProject()).getMessagesFor(element);
        }
        if (element instanceof IProductCmptLink) {
            IProductCmptLink link = (IProductCmptLink)element;
            return super.getMessages(link);
        }
        if (element instanceof String) {
            String elementName = (String)element;
            return generation.validate(generation.getIpsProject()).getMessagesFor(elementName);
        }
        return super.getMessages(element);
    }

    private static class InternalLabelProvider extends LabelProvider {

        private final IProductCmptGeneration generation;

        private final IProductCmptType productCmptType;

        public InternalLabelProvider(IProductCmptGeneration generation) {
            this.generation = generation;
            try {
                productCmptType = generation.findProductCmptType(generation.getIpsProject());
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String getText(Object element) {
            if (element instanceof String) {
                try {
                    // the reason why we use String elements at all is that we want to be able to
                    // show the association names event if we cannot find the association nor the
                    // product component type
                    if (productCmptType == null) {
                        return element.toString();
                    }
                    IAssociation association = productCmptType.findAssociation((String)element,
                            productCmptType.getIpsProject());
                    if (association == null) {
                        return element.toString();
                    }
                    if (association.is1ToMany()) {
                        return IpsPlugin.getMultiLanguageSupport().getLocalizedPluralLabel(association);
                    } else {
                        return IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(association);
                    }
                } catch (CoreException e) {
                    throw new RuntimeException(e);
                }
            }
            if (element instanceof IProductCmptLink) {
                IProductCmptLink rel = ((IProductCmptLink)element);
                return StringUtil.unqualifiedName(rel.getTarget());
            }
            return element.toString();
        }

        @Override
        public Image getImage(Object element) {
            if (element instanceof IProductCmptLink) {
                IProductCmptLink link = (IProductCmptLink)element;
                IProductCmpt product;
                try {
                    Image image;
                    product = link.findTarget(link.getIpsProject());
                    if (product == null) {
                        image = IpsUIPlugin.getImageHandling().getDefaultImage(ProductCmpt.class);
                    } else {
                        image = IpsUIPlugin.getImageHandling().getImage(product);
                    }
                    return image;
                } catch (CoreException e) {
                    IpsPlugin.log(e);
                }
            }
            if (element instanceof String) {
                try {
                    IProductCmptType type = generation.getProductCmpt().findProductCmptType(generation.getIpsProject());
                    if (type != null) {
                        IAssociation association = type.findAssociation((String)element, generation.getIpsProject());
                        return IpsUIPlugin.getImageHandling().getImage(association);
                    }
                } catch (Exception e) {
                    IpsPlugin.log(e);
                }
                return IpsUIPlugin.getImageHandling().getImage(
                        new AssociationWorkbenchAdapter().getDefaultImageDescriptor());
            }
            return IpsUIPlugin.getImageHandling().getImage(ImageDescriptor.getMissingImageDescriptor());
        }
    }
}
