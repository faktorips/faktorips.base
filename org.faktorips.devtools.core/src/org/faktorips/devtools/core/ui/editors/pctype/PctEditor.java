package org.faktorips.devtools.core.ui.editors.pctype;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.ui.editors.DescriptionPage;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;


/**
 * The editor to edit policy component types.
 */
public class PctEditor extends IpsObjectEditor {
    
    public PctEditor() {
        super();
    }

    /** 
     * Overridden method.
     * @see org.eclipse.ui.forms.editor.FormEditor#addPages()
     */
    protected void addPages() {
        try {
            addPage(new StructurePage(this));
            addPage(new BehaviourPage(this));
            addPage(new DescriptionPage(this));
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    IPolicyCmptType getPolicyCmptType() {
        try {
            return (IPolicyCmptType)getPdSrcFile().getIpsObject();
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
            throw new RuntimeException(e);
        }
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.IpsObjectEditor#getUniformPageTitle()
     */
    protected String getUniformPageTitle() {
        return "Policy Class: " + getPolicyCmptType().getName();
    }
}
