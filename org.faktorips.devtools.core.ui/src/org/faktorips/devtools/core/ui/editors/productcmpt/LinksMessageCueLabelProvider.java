/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeRelationReference;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.MessageCueLabelProvider;
import org.faktorips.util.StringUtil;
import org.faktorips.util.message.MessageList;

/**
 * Provides labels for relations. IProductCmptRelations are displayed as the target object including
 * a special cue label provider to get messages for product component type relations from the
 * generations instead of the product component type relation itself.
 * 
 * @author Thorsten Guenther
 * @author Cornelius Dirmeier
 */
public class LinksMessageCueLabelProvider extends MessageCueLabelProvider {

    private final IProductCmptGeneration generation;

    public LinksMessageCueLabelProvider(IProductCmptGeneration generation) {
        super(new InternalLabelProvider(), generation.getIpsProject());
        this.generation = generation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageList getMessages(Object element) throws CoreException {
        if (element instanceof String) {
            return generation.validate(generation.getIpsProject()).getMessagesFor(element);
        }
        if (element instanceof IProductCmptLink) {
            IProductCmptLink link = (IProductCmptLink)element;
            if (link != null) {
                return super.getMessages(link);
            }
        }
        if (element instanceof IProductCmptTypeRelationReference) {
            IProductCmptTypeRelationReference relationReference = (IProductCmptTypeRelationReference)element;
            return generation.validate(generation.getIpsProject()).getMessagesFor(
                    relationReference.getRelation().getName());
        }
        return super.getMessages(element);
    }

    private static class InternalLabelProvider extends LabelProvider {

        public InternalLabelProvider() {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getText(Object element) {
            if (element instanceof IProductCmptLink) {
                IProductCmptLink rel = ((IProductCmptLink)element);
                return StringUtil.unqualifiedName(rel.getTarget());
            } else if (element instanceof IProductCmptTypeRelationReference) {
                IProductCmptTypeRelationReference reference = (IProductCmptTypeRelationReference)element;
                return reference.getRelation().getName();
            }
            return element.toString();
        }

        /**
         * {@inheritDoc}
         */
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
            if (element instanceof IProductCmptTypeRelationReference) {
                IProductCmptTypeRelationReference reference = (IProductCmptTypeRelationReference)element;
                return IpsUIPlugin.getImageHandling().getImage(reference.getRelation());
            }
            return IpsUIPlugin.getImageHandling().getImage(ImageDescriptor.getMissingImageDescriptor());
        }

    }
}
