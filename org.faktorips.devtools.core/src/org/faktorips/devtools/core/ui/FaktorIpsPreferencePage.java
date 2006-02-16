package org.faktorips.devtools.core.ui;

import java.util.Locale;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.model.IChangesOverTimeNamingConvention;


/**
 *
 */
public class FaktorIpsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public FaktorIpsPreferencePage() {
        super(FieldEditorPreferencePage.GRID);
    }

    /** 
     * {@inheritDoc}
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
        
        StringFieldEditor productCmptPostfixField = new StringFieldEditor(
                IpsPreferences.DEFAULT_PRODUCT_CMPT_TYPE_POSTFIX, 
                "Default Product Component Type Postfix:", getFieldEditorParent());
        addField(productCmptPostfixField);

        IChangesOverTimeNamingConvention[] conventions = IpsPlugin.getDefault().getIpsModel().getChangesOverTimeNamingConvention();
        String[][] nameValues = new String[conventions.length][2];
        Locale locale = Locale.getDefault();
        for (int i = 0; i < conventions.length; i++) {
			nameValues[i][0] = conventions[i].getName(locale);
			nameValues[i][1] = conventions[i].getId();
		}
        ComboFieldEditor changeOverTimeField = new ComboFieldEditor(IpsPreferences.CHANGES_OVER_TIME_NAMING_CONCEPT,
        		"Naming scheme for changes over time:", nameValues, getFieldEditorParent());
        addField(changeOverTimeField);
    }

    /** 
     * {@inheritDoc}
     */
    public void init(IWorkbench workbench) {
        setPreferenceStore(IpsPlugin.getDefault().getPreferenceStore());
    }

}
