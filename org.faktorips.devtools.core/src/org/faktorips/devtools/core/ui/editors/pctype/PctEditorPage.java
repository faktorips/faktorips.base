package org.faktorips.devtools.core.ui.editors.pctype;

import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;


/**
 *
 */
public abstract class PctEditorPage extends IpsObjectEditorPage {

    /**
     * @param editor
     * @param id
     * @param title
     */
    public PctEditorPage(PctEditor editor, String id, String tabPageName) {
        super(editor, id, tabPageName);
    }

    PctEditor getPctEditor() {
        return (PctEditor)getEditor();
    }
    
    IPolicyCmptType getPolicyCmptType() {
        return getPctEditor().getPolicyCmptType(); 
    }

}
