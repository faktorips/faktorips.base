/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import java.util.Locale;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.faktorips.devtools.core.EnumTypeDisplay;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.model.ipsproject.IChangesOverTimeNamingConvention;

public class FaktorIpsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public FaktorIpsPreferencePage() {
        super(FieldEditorPreferencePage.GRID);
    }

    @Override
    protected void createFieldEditors() {
        createNullRepresentationField();
        createProductCmptPostfixField();
        createChangesOverTimeField();
        createEnumTypeDisplayField();
        createIpsTestRunnerMaxHeapSizeField();

        createEditRuntimeIdField();
        createEnableGeneratingField();
        createCanNavigateToModelField();
        createAdvancedTeamFunctionInProductDefExplorerField();
        createEasyContextMenuField();

        createWorkingModeField();
        createSectionsInTypeEditorsField();
        // TODO FIPS-1029
        // createRefactoringModeField();

        createDatatypeFormattingField();
    }

    private void createNullRepresentationField() {
        StringFieldEditor field = new StringFieldEditor(IpsPreferences.NULL_REPRESENTATION_STRING,
                Messages.FaktorIpsPreferencePage_labelNullValue, getFieldEditorParent());
        addField(field);
    }

    private void createProductCmptPostfixField() {
        StringFieldEditor field = new StringFieldEditor(IpsPreferences.DEFAULT_PRODUCT_CMPT_TYPE_POSTFIX,
                Messages.FaktorIpsPreferencePage_labelProductTypePostfix, getFieldEditorParent());
        addField(field);
    }

    private void createChangesOverTimeField() {
        IChangesOverTimeNamingConvention[] conventions = IpsPlugin.getDefault().getIpsModel()
                .getChangesOverTimeNamingConvention();
        String[][] nameValues = new String[conventions.length][2];
        for (int i = 0; i < conventions.length; i++) {
            nameValues[i][0] = conventions[i].getName();
            nameValues[i][1] = conventions[i].getId();
        }
        ComboFieldEditor field = new ComboFieldEditor(IpsPreferences.CHANGES_OVER_TIME_NAMING_CONCEPT,
                Messages.FaktorIpsPreferencePage_labelNamingScheme, nameValues, getFieldEditorParent());
        addField(field);
    }

    private void createEnumTypeDisplayField() {
        EnumTypeDisplay[] values = EnumTypeDisplay.values();
        String[][] enumTypeDisplayNameValues = new String[values.length][2];
        for (int i = 0; i < enumTypeDisplayNameValues.length; i++) {
            enumTypeDisplayNameValues[i][0] = values[i].getText();
            enumTypeDisplayNameValues[i][1] = values[i].getId();
        }
        ComboFieldEditor field = new ComboFieldEditor(IpsPreferences.ENUM_TYPE_DISPLAY,
                Messages.FaktorIpsPreferencePage_labelEnumTypeDisplay, enumTypeDisplayNameValues,
                getFieldEditorParent());
        addField(field);
    }

    private void createIpsTestRunnerMaxHeapSizeField() {
        StringFieldEditor field = new StringFieldEditor(IpsPreferences.IPSTESTRUNNER_MAX_HEAP_SIZE,
                Messages.FaktorIpsPreferencePage_labelMaxHeapSizeIpsTestRunner, getFieldEditorParent());
        addField(field);
    }

    private void createEditRuntimeIdField() {
        BooleanFieldEditor field = new BooleanFieldEditor(IpsPreferences.MODIFY_RUNTIME_ID,
                Messages.FaktorIpsPreferencePage_modifyRuntimeId, getFieldEditorParent());
        addField(field);
    }

    private void createEnableGeneratingField() {
        BooleanFieldEditor field = new BooleanFieldEditor(IpsPreferences.ENABLE_GENERATING,
                Messages.FaktorIpsPreferencePage_FaktorIpsPreferencePage_enableGenerating, getFieldEditorParent());
        addField(field);
    }

    private void createCanNavigateToModelField() {
        BooleanFieldEditor field = new BooleanFieldEditor(IpsPreferences.NAVIGATE_TO_MODEL_OR_SOURCE_CODE,
                Messages.FaktorIpsPreferencePage_labelCanNavigateToModelOrSourceCode, getFieldEditorParent());
        addField(field);
    }

    private void createAdvancedTeamFunctionInProductDefExplorerField() {
        BooleanFieldEditor field = new BooleanFieldEditor(
                IpsPreferences.ADVANCED_TEAM_FUNCTIONS_IN_PRODUCT_DEF_EXPLORER,
                Messages.FaktorIpsPreferencePage_advancedTeamFunctionsInProductDefExplorer, getFieldEditorParent());
        addField(field);
    }

    private void createEasyContextMenuField() {
        BooleanFieldEditor field = new BooleanFieldEditor(IpsPreferences.SIMPLE_CONTEXT_MENU,
                Messages.FaktorIpsPreferencePage_simpleContextMenu, getFieldEditorParent());
        addField(field);
    }

    private void createWorkingModeField() {
        RadioGroupFieldEditor field = new RadioGroupFieldEditor(
                IpsPreferences.WORKING_MODE,
                Messages.FaktorIpsPreferencePage_titleWorkingMode,
                2,
                new String[][] {
                        { Messages.FaktorIpsPreferencePage_labelWorkingModeBrowse, IpsPreferences.WORKING_MODE_BROWSE },
                        { Messages.FaktorIpsPreferencePage_labelWorkingModeEdit, IpsPreferences.WORKING_MODE_EDIT } },
                getFieldEditorParent(), true);
        addField(field);
    }

    private void createSectionsInTypeEditorsField() {
        RadioGroupFieldEditor field = new RadioGroupFieldEditor(IpsPreferences.SECTIONS_IN_TYPE_EDITORS,
                Messages.FaktorIpsPreferencePage_title_numberOfSections, 2, new String[][] {
                        { Messages.FaktorIpsPreferencePage_label_twoSections,
                                IpsPreferences.TWO_SECTIONS_IN_TYPE_EDITOR_PAGE },
                        { Messages.FaktorIpsPreferencePage_label_fourSections,
                                IpsPreferences.FOUR_SECTIONS_IN_TYPE_EDITOR_PAGE } }, getFieldEditorParent(), true);
        addField(field);
    }

    private void createRefactoringModeField() {
        RadioGroupFieldEditor field = new RadioGroupFieldEditor(
                IpsPreferences.REFACTORING_MODE,
                Messages.FaktorIpsPreferencePage_title_refactoringMode,
                2,
                new String[][] {
                        { Messages.FaktorIpsPreferencePage_label_direct, IpsPreferences.REFACTORING_MODE_DIRECT },
                        { Messages.FaktorIpsPreferencePage_label_explicit, IpsPreferences.REFACTORING_MODE_EXPLICIT } },
                getFieldEditorParent(), true);
        field.getRadioBoxControl(getFieldEditorParent()).getChildren()[0]
                .setToolTipText(Messages.FaktorIpsPreferencePage_tooltip_direct);
        field.getRadioBoxControl(getFieldEditorParent()).getChildren()[1]
                .setToolTipText(Messages.FaktorIpsPreferencePage_tooltip_explicit);
        addField(field);
    }

    protected void createDatatypeFormattingField() {
        Locale[] availableLocales = new Locale[] { Locale.GERMANY, Locale.US, Locale.UK };
        String[][] localeDisplayNameValues = new String[availableLocales.length][2];
        for (int i = 0; i < availableLocales.length; i++) {
            localeDisplayNameValues[i][0] = availableLocales[i].getDisplayName();
            localeDisplayNameValues[i][1] = availableLocales[i].toString();
        }
        ComboFieldEditor field = new ComboFieldEditor(IpsPreferences.DATATYPE_FORMATTING_LOCALE,
                Messages.FaktorIpsPreferencePage_LabelFormattingOfValues, localeDisplayNameValues,
                getFieldEditorParent());
        addField(field);
    }

    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(IpsPlugin.getDefault().getPreferenceStore());
    }

}
