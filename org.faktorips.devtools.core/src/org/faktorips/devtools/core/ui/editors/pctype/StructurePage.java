package org.faktorips.devtools.core.ui.editors.pctype;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;


/**
 * The structure page contain the general information section, the attributes section
 * and the relations section.
 */
public class StructurePage extends PctEditorPage {
    
    final static String PAGEID = "Structure"; //$NON-NLS-1$

    public StructurePage(PctEditor editor) {
        super(editor, PAGEID, "Structure");
    }
    
	protected void createPageContent(Composite formBody, UIToolkit toolkit) {
		formBody.setLayout(createPageLayout(1, false));
		new GeneralInfoSection(getPolicyCmptType(), formBody, toolkit); 
		
		Composite members = createGridComposite(toolkit, formBody, 2, true, GridData.FILL_BOTH);
		new AttributesSection(getPolicyCmptType(), members, toolkit);
		new RelationsSection(getPolicyCmptType(), members, toolkit);
	}

}
