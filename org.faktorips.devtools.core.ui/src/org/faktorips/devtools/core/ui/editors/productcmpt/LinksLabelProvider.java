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

import java.util.ArrayList;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeRelationReference;
import org.faktorips.util.StringUtil;

/**
 * Provides labels for relations. IProductCmptRelations are displayed as the target object.
 * 
 * @author Thorsten Guenther
 */
public class LinksLabelProvider implements ILabelProvider {

    private ArrayList<ILabelProviderListener> listeners;

    /**
     * {@inheritDoc}
     */
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
    public Image getImage(Object element) {
        if (element instanceof IProductCmptLink) {
            return IpsObjectType.PRODUCT_CMPT.getEnabledImage();
        }
        if (element instanceof IProductCmptTypeRelationReference) {
            return IpsPlugin.getDefault().getImage(AssociationType.COMPOSITION_MASTER_TO_DETAIL.getImageName());
        }
        return IpsPlugin.getDefault().getImage(Messages.RelationsLabelProvider_undefined);
    }

    /**
     * {@inheritDoc}
     */
    public void addListener(ILabelProviderListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<ILabelProviderListener>();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        listeners = null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isLabelProperty(Object element, String property) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void removeListener(ILabelProviderListener listener) {
        listeners.remove(listener);
    }

}
