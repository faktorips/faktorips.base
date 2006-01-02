package org.faktorips.devtools.core.ui.editors.tablestructure;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.editors.DescriptionPage;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;


/**
 *
 */
public class TableStructureEditor extends IpsObjectEditor {

    /**
     * 
     */
    public TableStructureEditor() {
        super();
    }
    
    protected ITableStructure getTableStructure() {
        return (ITableStructure)getIpsObject();
    }

    /** 
     * Overridden method.
     * @see org.eclipse.ui.forms.editor.FormEditor#addPages()
     */
    protected void addPages() {
        try {
            addPage(new StructurePage(this));
            addPage(new DescriptionPage(this));
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.IpsObjectEditor#getUniformPageTitle()
     */
    protected String getUniformPageTitle() {
        return "Table Structure: " + getIpsObject().getName();
    }

}

