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
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptType;
import org.faktorips.devtools.core.ui.UIToolkit;


/**
 *
 */
public class ProductCmptRefControl extends IpsObjectRefControl {

    private boolean includeCmptsForSubtypes = true;
    private IProductCmpt[] toExclude = new IProductCmpt[0];
    private IProductCmptType productCmptType;
    
    
    public ProductCmptRefControl(
            IIpsProject project, 
            Composite parent, 
            UIToolkit toolkit) {
        super(project, parent, toolkit, Messages.ProductCmptRefControl_title, Messages.ProductCmptRefControl_description);
    }
    
    /**
     * @param qPcTypeNmae The product component type for which product components should be selectable.
     * @param includeCmptsForSubtypes <code>true</code> if also product components for subtypes should be selectable.
     */
    public void setProductCmptType(IProductCmptType productCmptType, boolean includeCmptsForSubtypes) {
        this.productCmptType = productCmptType;
        this.includeCmptsForSubtypes = includeCmptsForSubtypes;
    }
    
    /** 
     * {@inheritDoc}
     */
    protected IIpsObject[] getIpsObjects() throws CoreException {
        if (getIpsProject()==null) {
            return new IIpsObject[0];
        }

        IProductCmpt[] cmpts = getIpsProject().findAllProductCmpts(productCmptType, includeCmptsForSubtypes);

    	List cmptList = new ArrayList();
    	cmptList.addAll(Arrays.asList(cmpts));
    	for (int i = 0; i < toExclude.length; i++) {
			cmptList.remove(toExclude[i]);
		}
    	
        return (IIpsObject[])cmptList.toArray(new IIpsObject[cmptList.size()]);
    }

	/**
	 * Set all product components to exclude from result.
	 * 
	 * @param cmpts All product components to exclude. 
	 */
	public void setProductCmptsToExclude(IProductCmpt[] cmpts) {
		if (cmpts == null) {
			toExclude = new IProductCmpt[0];
		}
		else {
			toExclude = cmpts;
		}
	}

}
