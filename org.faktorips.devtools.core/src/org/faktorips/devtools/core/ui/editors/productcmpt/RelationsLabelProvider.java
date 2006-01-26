package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;

public class RelationsLabelProvider implements ILabelProvider{

        public String getText(Object element) {
			if (element instanceof IProductCmptRelation) {
				IProductCmptRelation rel = ((IProductCmptRelation)element);
				try {
					return rel.getIpsProject().findIpsObject(IpsObjectType.PRODUCT_CMPT, rel.getTarget()).getName();
				} catch (CoreException e) {
					IpsPlugin.log(e);
				}
			}
			else if (element instanceof IIpsObjectPart) {
        		return ((IIpsObjectPart)element).getName();
        	}
        	return "<undefined>";
        }

		public Image getImage(Object element) {
			if (element instanceof IProductCmptTypeRelation) {
				return ((IProductCmptTypeRelation)element).getImage();
			}
			else if (element instanceof IProductCmptRelation) {
				IProductCmptRelation rel = ((IProductCmptRelation)element);
				try {
					return rel.getIpsProject().findIpsObject(IpsObjectType.PRODUCT_CMPT, rel.getTarget()).getImage();
				} catch (CoreException e) {
					IpsPlugin.log(e);
				}
			}
			return IpsPlugin.getDefault().getImage("<undefined>");
		}

		public void addListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub
			
		}

		public void dispose() {
			// TODO Auto-generated method stub
			
		}

		public boolean isLabelProperty(Object element, String property) {
			// TODO Auto-generated method stub
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub
			
		}

}
