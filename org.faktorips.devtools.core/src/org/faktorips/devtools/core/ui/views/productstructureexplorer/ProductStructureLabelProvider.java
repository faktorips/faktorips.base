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

package org.faktorips.devtools.core.ui.views.productstructureexplorer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.product.IProductCmptReference;
import org.faktorips.devtools.core.model.product.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.core.model.product.IProductCmptTypeRelationReference;
import org.faktorips.devtools.core.model.product.ITableContentUsage;
import org.faktorips.util.StringUtil;

public class ProductStructureLabelProvider implements ILabelProvider {

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
        else if (element instanceof IProductCmptStructureTblUsageReference) {
            return ((IProductCmptStructureTblUsageReference)element).getTableContentUsage().getImage();
        }   
        else if (element instanceof ViewerLabel){
            return ((ViewerLabel)element).getImage();
        }
	    return IpsPlugin.getDefault().getImage(Messages.ProductStructureLabelProvider_undefined);
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
        else if (element instanceof IProductCmptStructureTblUsageReference) {
            ITableContentUsage tcu = ((IProductCmptStructureTblUsageReference)element).getTableContentUsage();
            return tcu.getStructureUsage() + " : " + StringUtil.unqualifiedName(tcu.getTableContentName());
        } 
        else if (element instanceof ViewerLabel){
            return ((ViewerLabel)element).getText();
        }
		return Messages.ProductStructureLabelProvider_undefined;
	}
}
