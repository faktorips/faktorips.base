package org.faktorips.devtools.core.ui.wizards.tablestructure;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;
import org.faktorips.devtools.core.ui.wizards.NewIpsObjectWizard;


/**
 *
 */
public class NewTableStructureWizard extends NewIpsObjectWizard {
    
    public NewTableStructureWizard() {
        super(IpsObjectType.TABLE_STRUCTURE);
    }
    
    /** 
     * Overridden method.
     * @throws JavaModelException
     * @see org.faktorips.devtools.core.ui.wizards.NewIpsObjectWizard#createFirstPage()
     */
    protected IpsObjectPage createFirstPage(IStructuredSelection selection) throws JavaModelException {
        return new TableStructurePage(selection);
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
    }

}
