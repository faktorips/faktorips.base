package org.faktorips.devtools.core.ui;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;


/**
 *
 */
public class FaktorIpsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public FaktorIpsPreferencePage() {
        super(FieldEditorPreferencePage.GRID);
    }

    /** 
     * Overridden method.
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    protected void createFieldEditors() {
        StringFieldEditor workingDateField = new StringFieldEditor(
                IpsPreferences.WORKING_DATE, 
                "Product Changes Effective Date (YYYY-MM-DD):", getFieldEditorParent());
        addField(workingDateField);

        StringFieldEditor nullRepresentation = new StringFieldEditor(
                IpsPreferences.NULL_REPRESENTATION_STRING, 
                "String-Representation for NULL-Values:", getFieldEditorParent());
        addField(nullRepresentation);
    }

    /** 
     * Overridden method.
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) {
        setPreferenceStore(IpsPlugin.getDefault().getPreferenceStore());
    }

}
