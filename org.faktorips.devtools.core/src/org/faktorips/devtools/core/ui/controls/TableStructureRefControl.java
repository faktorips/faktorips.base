package org.faktorips.devtools.core.ui.controls;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.UIToolkit;


/**
 * A control to edit policy component type references.  
 */
public class TableStructureRefControl extends IpsObjectRefControl {

    public TableStructureRefControl(
            IIpsProject project, 
            Composite parent, 
            UIToolkit toolkit) {
        super(project, parent, toolkit, "Table Structure Selection", "Select a table (?=any character, *=any string");
    }
    
    /**
     * Returns the table structure entered in this control. Returns <code>null</code>
     * if the text in the control does not identify a table structure.
     * 
     * @throws CoreException if an exception occurs while searching for
     * the table structure.
     */
    public ITableStructure findTableStructure() throws CoreException {
        return (ITableStructure)getPdProject().findIpsObject(IpsObjectType.TABLE_STRUCTURE, getText());
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controls.IpsObjectRefControl#getPdObjects()
     */
    protected IIpsObject[] getPdObjects() throws CoreException {
        return getPdProject().findIpsObjects(IpsObjectType.TABLE_STRUCTURE);
    }

}
