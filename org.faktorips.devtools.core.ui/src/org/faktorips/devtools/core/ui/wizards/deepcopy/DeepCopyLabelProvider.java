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

package org.faktorips.devtools.core.ui.wizards.deepcopy;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeRelationReference;
import org.faktorips.util.StringUtil;

public class DeepCopyLabelProvider implements ILabelProvider {

    private List<ILabelProviderListener> listeners = new ArrayList<ILabelProviderListener>();

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

    /**
     * {@inheritDoc}
     */
    public Image getImage(Object element) {
        if (element instanceof IProductCmptReference) {
            return ((IProductCmptReference)element).getProductCmpt().getImage();
        } else if (element instanceof IProductCmptTypeRelationReference) {
            return ((IProductCmptTypeRelationReference)element).getRelation().getImage();
        } else if (element instanceof IProductCmptStructureTblUsageReference) {
            return ((IProductCmptStructureTblUsageReference)element).getTableContentUsage().getImage();
        }

        return IpsPlugin.getDefault().getImage("<undefined>"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public String getText(Object element) {
        if (element instanceof IProductCmptReference) {
            IProductCmptReference productCmptReference = (IProductCmptReference)element;
            return productCmptReference.getProductCmpt().getName();
        } else if (element instanceof IProductCmptTypeRelationReference) {
            return ((IProductCmptTypeRelationReference)element).getRelation().getName();
        } else if (element instanceof IProductCmptStructureTblUsageReference) {
            return StringUtil.unqualifiedName(((IProductCmptStructureTblUsageReference)element).getTableContentUsage()
                    .getTableContentName());
        }
        return Messages.DeepCopyLabelProvider_textUndefined;
    }

    public Image getErrorImage() {
        return IpsPlugin.getDefault().getImage("error_tsk.gif"); //$NON-NLS-1$
    }
}
