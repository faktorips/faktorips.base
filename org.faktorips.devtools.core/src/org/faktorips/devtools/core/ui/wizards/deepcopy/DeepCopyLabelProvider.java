/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.deepcopy;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.product.IProductCmptReference;
import org.faktorips.devtools.core.model.product.IProductCmptTypeRelationReference;

public class DeepCopyLabelProvider implements ILabelProvider {

    private List listeners = new ArrayList();
    
    /**
     * {@inheritDoc}
     */
    public void addListener(ILabelProviderListener listener) {
        listeners.add(listener);
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        this.listeners = null;
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

    /**
     * {@inheritDoc}
     */
    public Image getImage(Object element) {
        if (element instanceof IProductCmptReference) {
            return ((IProductCmptReference)element).getProductCmpt().getImage();
        }
        else if (element instanceof IProductCmptTypeRelationReference) {
            return ((IProductCmptTypeRelationReference)element).getRelation().getImage();
        }
        
        return IpsPlugin.getDefault().getImage("<undefined>"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public String getText(Object element) {
        if (element instanceof IProductCmptReference) {
            return ((IProductCmptReference)element).getProductCmpt().getName();
        }
        else if (element instanceof IProductCmptTypeRelationReference) {
            return ((IProductCmptTypeRelationReference)element).getRelation().getName();
        }
        return Messages.DeepCopyLabelProvider_textUndefined;
    }

}
