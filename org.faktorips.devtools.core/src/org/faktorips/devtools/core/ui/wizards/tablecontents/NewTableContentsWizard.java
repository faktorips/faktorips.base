package org.faktorips.devtools.core.ui.wizards.tablecontents;

import java.util.GregorianCalendar;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;
import org.faktorips.devtools.core.ui.wizards.NewIpsObjectWizard;


/**
 *
 */
public class NewTableContentsWizard extends NewIpsObjectWizard {
    
    private TableContentsPage page;
    
    public NewTableContentsWizard() {
        super(IpsObjectType.TABLE_CONTENTS);
    }
    
    /** 
     * Overridden method.
     * @throws JavaModelException
     * @see org.faktorips.devtools.core.ui.wizards.NewIpsObjectWizard#createFirstPage()
     */
    protected IpsObjectPage createFirstPage(IStructuredSelection selection) throws JavaModelException {
        page = new TableContentsPage(selection);
        return page;
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
        ITableContents table = (ITableContents)pdObject;
        table.setTableStructure(page.getTableStructure());
        GregorianCalendar date = IpsPreferences.getWorkingDate();
        if (date==null) {
            return;
        }
        IIpsObjectGeneration generation = table.newGeneration();
        generation.setValidFrom(date);
        ITableStructure structure = (ITableStructure)table.getIpsProject().findIpsObject(IpsObjectType.TABLE_STRUCTURE, table.getTableStructure());
        if (structure!=null) {
            for (int i=0; i<structure.getNumOfColumns(); i++) {
                table.newColumn(""); //$NON-NLS-1$
            }
        }
    }

}
