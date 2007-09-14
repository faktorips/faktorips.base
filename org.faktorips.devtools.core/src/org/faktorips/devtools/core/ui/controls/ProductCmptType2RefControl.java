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

package org.faktorips.devtools.core.ui.controls;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptType;
import org.faktorips.devtools.core.ui.UIToolkit;


/**
 * Control to edit references to product component types.
 */
public class ProductCmptType2RefControl extends IpsObjectRefControl {

    private boolean excludeAbstractTypes;
    
    public ProductCmptType2RefControl(
            IIpsProject project, 
            Composite parent, 
            UIToolkit toolkit,
            boolean excludeAbstractTypes) {
        super(project, parent, toolkit, Messages.ProductCmptTypeRefControl_title, Messages.ProductCmptTypeRefControl_description);
        this.excludeAbstractTypes = excludeAbstractTypes;
    }
    
    /** 
     * {@inheritDoc}
     */
    protected IIpsObject[] getIpsObjects() throws CoreException {
    	if (getIpsProject()==null) {
    		return new IIpsObject[0];
    	}
    	IIpsObject[] allProductCmptTypes = getIpsProject().findIpsObjects(IpsObjectType.PRODUCT_CMPT_TYPE_V2);
    	ArrayList result = new ArrayList();
    	for (int i = 0; i < allProductCmptTypes.length; i++) {
            IProductCmptType type = (IProductCmptType)allProductCmptTypes[i];
			if (!excludeAbstractTypes || !type.isAbstract()) {
				result.add(allProductCmptTypes[i]);
			}
		}
        return (IIpsObject[])result.toArray(new IIpsObject[result.size()]);
    }

    /**
     * Returns the product component type entered in this control. Returns <code>null</code>
     * if the text in the control does not identify a product component type.
     * 
     * @throws CoreException if an exception occurs while searching for
     * the type.
     */
    public IProductCmptType findProductCmptType() throws CoreException {
    	IIpsProject project = getIpsProject();
    	if (project != null) {
            return (IProductCmptType)project.findIpsObject(IpsObjectType.OLD_PRODUCT_CMPT_TYPE, getText());
    	}
    	return null;
    }
}
