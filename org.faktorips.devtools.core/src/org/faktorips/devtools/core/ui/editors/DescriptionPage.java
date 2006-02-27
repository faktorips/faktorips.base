package org.faktorips.devtools.core.ui.editors;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;


/**
 * A page to edit the object's description.
 */
public class DescriptionPage extends IpsObjectEditorPage {

    final static String PAGEID = "Description"; //$NON-NLS-1$
    
    public DescriptionPage(IpsObjectEditor editor) {
        super(editor, PAGEID, Messages.DescriptionPage_description);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage#createPageContent(org.eclipse.swt.widgets.Composite, org.faktorips.devtools.core.ui.UIToolkit)
     */
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
		formBody.setLayout(createPageLayout(1, false));
		new DescriptionSection(getPdObject(), formBody, toolkit); 
    }

}
