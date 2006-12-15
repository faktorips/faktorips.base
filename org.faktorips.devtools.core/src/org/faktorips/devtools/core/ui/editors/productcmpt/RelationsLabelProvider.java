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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.util.StringUtil;

/**
 * Provides labels for relations. IProductCmptRelations are displayed as the target object.
 * 
 * @author Thorsten Guenther
 */
public class RelationsLabelProvider implements ILabelProvider{
	
	private ArrayList listeners;
	
	/**
     * {@inheritDoc}
	 */
	public String getText(Object element) {
		if (element instanceof IProductCmptRelation) {
			IProductCmptRelation rel = ((IProductCmptRelation)element);
			return StringUtil.unqualifiedName(rel.getTarget());
		}
        return element.toString();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Image getImage(Object element) {
        if (element instanceof IProductCmptRelation) {
            return IpsObjectType.PRODUCT_CMPT.getEnabledImage();
        }
		if (element instanceof String) {
			return IpsPlugin.getDefault().getImage("Composition.gif");  //$NON-NLS-1$ 
		}
		return IpsPlugin.getDefault().getImage(Messages.RelationsLabelProvider_undefined);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addListener(ILabelProviderListener listener) {
		if (listeners == null) {
			listeners = new ArrayList();
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
