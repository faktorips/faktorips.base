package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;

/**
 * Provides labels for relations. IProductCmptRelations are displayed as the target object.
 * 
 * @author Thorsten Guenther
 */
public class RelationsLabelProvider implements ILabelProvider{
	
	private ArrayList listeners;
	
	/**
	 * Overridden.
	 */
	public String getText(Object element) {
		if (element instanceof IProductCmptRelation) {
			IProductCmptRelation rel = ((IProductCmptRelation)element);
			try {
				IIpsObject ipsObj = rel.getIpsProject().findIpsObject(IpsObjectType.PRODUCT_CMPT, rel.getTarget());
				if (ipsObj != null) {
					return ipsObj.getName();
				}
			} catch (CoreException e) {
				IpsPlugin.log(e);
			}
		}
		else if (element instanceof IIpsObjectPart) {
			return ((IIpsObjectPart)element).getName();
		}
		return Messages.RelationsLabelProvider_undefined;
	}
	
	/**
	 * Overridden.
	 */
	public Image getImage(Object element) {
		if (element instanceof IProductCmptTypeRelation) {
			return ((IProductCmptTypeRelation)element).getImage();
		}
		else if (element instanceof IProductCmptRelation) {
			IProductCmptRelation rel = ((IProductCmptRelation)element);
			try {
				IIpsObject ipsObj = rel.getIpsProject().findIpsObject(IpsObjectType.PRODUCT_CMPT, rel.getTarget());
				if (ipsObj != null) {
					return ipsObj.getImage();
				}
			} catch (CoreException e) {
				IpsPlugin.log(e);
			}
		}
		return IpsPlugin.getDefault().getImage(Messages.RelationsLabelProvider_undefined);
	}
	
	/**
	 * Overridden.
	 */
	public void addListener(ILabelProviderListener listener) {
		if (listeners == null) {
			listeners = new ArrayList();
		}
	}
	
	/**
	 * Overridden.
	 */
	public void dispose() {
		listeners = null;
	}
	
	/**
	 * Overridden.
	 */
	public boolean isLabelProperty(Object element, String property) {
		return true;
	}
	
	/**
	 * Overridden.
	 */
	public void removeListener(ILabelProviderListener listener) {
		listeners.remove(listener);
	}
	
}
