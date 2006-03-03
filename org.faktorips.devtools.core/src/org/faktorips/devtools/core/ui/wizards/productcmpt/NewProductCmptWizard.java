package org.faktorips.devtools.core.ui.wizards.productcmpt;

import java.util.GregorianCalendar;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.faktorips.devtools.core.IpsPreferences;
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
        GregorianCalendar date = IpsPreferences.getWorkingDate();
        if (date==null) {
            return;
        }
        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration();
        generation.setValidFrom(date);
        generation.fixDifferences(generation.computeDeltaToPolicyCmptType());
    }

}
