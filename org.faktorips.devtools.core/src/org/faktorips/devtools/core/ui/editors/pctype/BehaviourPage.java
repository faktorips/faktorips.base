package org.faktorips.devtools.core.ui.editors.pctype;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;


/**
 *
 */
public class BehaviourPage extends PctEditorPage {
    
    final static String PAGE_ID = "BehaviourPage";
    
    public BehaviourPage(PctEditor editor) {
        super(editor, PAGE_ID, "Behaviour"); 
    }

    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage#createPageContent(org.eclipse.swt.widgets.Composite, org.faktorips.devtools.core.ui.controls.UIToolkit)
     */ 
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
		formBody.setLayout(createPageLayout(2, true));
		new MethodsSection(getPolicyCmptType(), formBody, toolkit);
		new RulesSection(getPolicyCmptType(), formBody, toolkit);
    }

}
