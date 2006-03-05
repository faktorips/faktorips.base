package org.faktorips.devtools.core.ui;

import java.util.Locale;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.model.IChangesOverTimeNamingConvention;

/**
 * 
 */
public class FaktorIpsPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {

	public FaktorIpsPreferencePage() {
		super(FieldEditorPreferencePage.GRID);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void createFieldEditors() {
		StringFieldEditor workingDateField = new StringFieldEditor(
				IpsPreferences.WORKING_DATE,
				Messages.FaktorIpsPreferencePage_labelWorkingDate,
				getFieldEditorParent());
		addField(workingDateField);
        
		StringFieldEditor nullRepresentation = new StringFieldEditor(
				IpsPreferences.NULL_REPRESENTATION_STRING,
				Messages.FaktorIpsPreferencePage_labelNullValue,
				getFieldEditorParent());
		addField(nullRepresentation);

		StringFieldEditor productCmptPostfixField = new StringFieldEditor(
				IpsPreferences.DEFAULT_PRODUCT_CMPT_TYPE_POSTFIX,
				Messages.FaktorIpsPreferencePage_labelProductTypePostfix,
				getFieldEditorParent());
		addField(productCmptPostfixField);

		IChangesOverTimeNamingConvention[] conventions = IpsPlugin.getDefault()
				.getIpsModel().getChangesOverTimeNamingConvention();
		String[][] nameValues = new String[conventions.length][2];
		Locale locale = Locale.getDefault();
		for (int i = 0; i < conventions.length; i++) {
			nameValues[i][0] = conventions[i].getName(locale);
			nameValues[i][1] = conventions[i].getId();
		}
		ComboFieldEditor changeOverTimeField = new ComboFieldEditor(
				IpsPreferences.CHANGES_OVER_TIME_NAMING_CONCEPT,
				Messages.FaktorIpsPreferencePage_labelNamingScheme, nameValues,
				getFieldEditorParent());
		addField(changeOverTimeField);

        String label = NLS.bind(Messages.FaktorIpsPreferencePage_labelEditRecentGenerations, IpsPlugin.getDefault()
				.getIpsPreferences().getChangesOverTimeNamingConvention()
				.getGenerationConceptNamePlural(Locale.getDefault()));
		BooleanFieldEditor editRecentGernations = new BooleanFieldEditor(
				IpsPreferences.EDIT_RECENT_GENERATION, label, getFieldEditorParent());
		addField(editRecentGernations);

		label = NLS.bind(Messages.FaktorIpsPreferencePage_labelEditGenerationsWithSuccessor, IpsPlugin.getDefault()
				.getIpsPreferences().getChangesOverTimeNamingConvention()
				.getGenerationConceptNamePlural(Locale.getDefault()));
		BooleanFieldEditor editGernationsWithSuccessor = new BooleanFieldEditor(
				IpsPreferences.EDIT_GENERATION_WITH_SUCCESSOR, label, getFieldEditorParent());
		addField(editGernationsWithSuccessor);

		BooleanFieldEditor enableGeneratingField = new BooleanFieldEditor(
				IpsPreferences.ENABLE_GENERATING,
				Messages.FaktorIpsPreferencePage_FaktorIpsPreferencePage_enableGenerating, getFieldEditorParent());
		addField(enableGeneratingField);
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(IWorkbench workbench) {
		setPreferenceStore(IpsPlugin.getDefault().getPreferenceStore());
	}

}
