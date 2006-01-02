package org.faktorips.devtools.core.ui.wizards.tablestructure;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;


/**
 *
 */
public class TableStructurePage extends IpsObjectPage {
    
    /**
     * @param pageName
     * @param selection
     * @throws JavaModelException
     */
    public TableStructurePage(IStructuredSelection selection) throws JavaModelException {
        super(selection, "NewTableStructure");
    }
    
}
