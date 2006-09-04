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

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import java.util.GregorianCalendar;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;
import org.faktorips.devtools.core.ui.wizards.NewIpsObjectWizard;


/**
 * Wizard to create a new product component.
 */
public class NewProductCmptWizard extends NewIpsObjectWizard {
    
    private ProductCmptPage productCmptPage;
    
    public NewProductCmptWizard() {
        super(IpsObjectType.PRODUCT_CMPT);
        this.setDefaultPageImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("wizards/NewProductCmptWizard.png"));
    }
    
    /** 
     * {@inheritDoc}
     */
    protected IpsObjectPage createFirstPage(IStructuredSelection selection) throws JavaModelException {
        productCmptPage = new ProductCmptPage(selection);
        return productCmptPage;
    }

    /** 
     * {@inheritDoc}
     */
    protected void createAdditionalPages() {
    }
    
    /** 
     * {@inheritDoc}
     */
    protected void finishIpsObject(IIpsObject pdObject) throws CoreException {
        IProductCmpt productCmpt = (IProductCmpt)pdObject;
        productCmpt.setPolicyCmptType(productCmptPage.getPolicyCmptType());
        GregorianCalendar date = IpsPlugin.getDefault().getIpsPreferences().getWorkingDate();
        if (date==null) {
            return;
        }
        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration();
        generation.setValidFrom(date);
        generation.fixDifferences(generation.computeDeltaToPolicyCmptType());
    }

}
