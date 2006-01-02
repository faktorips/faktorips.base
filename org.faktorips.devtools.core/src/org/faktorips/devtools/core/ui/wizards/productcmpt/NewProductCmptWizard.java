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
 *
 */
public class NewProductCmptWizard extends NewIpsObjectWizard {
    
    private ProductCmptPage productCmptPage;
    
    public NewProductCmptWizard() {
        super(IpsObjectType.PRODUCT_CMPT);
    }
    
    /** 
     * Overridden method.
     * @throws JavaModelException
     * @see org.faktorips.devtools.core.ui.wizards.NewIpsObjectWizard#createFirstPage()
     */
    protected IpsObjectPage createFirstPage(IStructuredSelection selection) throws JavaModelException {
        productCmptPage = new ProductCmptPage(selection);
        return productCmptPage;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.wizards.NewIpsObjectWizard#createAdditionalPages()
     */
    protected void createAdditionalPages() {
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.wizards.NewIpsObjectWizard#finishPdObject(org.faktorips.devtools.core.model.IIpsObject)
     */
    protected void finishPdObject(IIpsObject pdObject) throws CoreException {
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
