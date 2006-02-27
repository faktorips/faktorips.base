package org.faktorips.devtools.core.ui.editors.tablestructure;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;


/**
 *
 */
public class StructurePage extends IpsObjectEditorPage {

    final static String PAGE_ID = "Structure";  //$NON-NLS-1$

    public StructurePage(TableStructureEditor editor) {
        super(editor, PAGE_ID, Messages.StructurePage_title);
    }
    
    TableStructureEditor getTableEditor() {
        return (TableStructureEditor)getEditor();
    }
    
    ITableStructure getTableStructure() {
        return getTableEditor().getTableStructure(); 
    }

    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage#createPageContent(org.eclipse.swt.widgets.Composite, org.faktorips.devtools.core.ui.UIToolkit)
     */
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
		formBody.setLayout(createPageLayout(2, true));
        
		new ColumnsSection(getTableStructure(), formBody, toolkit);
		new UniqueKeysSection(getTableStructure(), formBody, toolkit);
		new RangesSection(getTableStructure(), formBody, toolkit);
		new ForeignKeysSection(getTableStructure(), formBody, toolkit);
    }

}
