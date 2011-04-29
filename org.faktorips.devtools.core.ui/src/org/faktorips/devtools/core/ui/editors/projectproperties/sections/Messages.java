/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.projectproperties.sections;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.editors.projectproperties.sections.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String IpsObjectEditor_fileHasChangesOnDiskMessage;
    public static String IpsObjectEditor_fileHasChangesOnDiskNoButton;
    public static String IpsObjectEditor_fileHasChangesOnDiskTitle;
    public static String IpsObjectEditor_fileHasChangesOnDiskYesButton;

    public static String IpsPartEditDialog_tabItemLabel;
    public static String IpsPartEditDialog_tabItemDescription;

    public static String IpsPartsComposite_buttonNew;
    public static String IpsPartsComposite_buttonEdit;
    public static String IpsPartsComposite_buttonShow;
    public static String IpsPartsComposite_buttonDelete;
    public static String IpsPartsComposite_buttonUp;
    public static String IpsPartsComposite_buttonDown;

    public static String TimedIpsObjectEditor_actualWorkingDate;

    public static String UnparsableFilePage_fileContentIsNotParsable;

    public static String UnreachableFilePage_msgUnreachableFile;

    public static String DescriptionPage_description;

    public static String DescriptionSection_description;

    public static String LabelEditComposite_tableColumnHeaderLanguage;
    public static String LabelEditComposite_tableColumnHeaderLabel;
    public static String LabelEditComposite_tableColumnHeaderPluralLabel;

    public static String DatatypePropertiesPage_description;
    public static String DatatypesSection_enumtype;
    public static String DatatypesSection_supportingName;
    public static String DatatypesSection_javaClass;
    public static String DatatypesSection_valueObject;
    public static String DatatypesSection_null;

    public static String ModelPropertiesPage_description;

    public static String OptionalConstraints_title;
    public static String OptionalConstraints_derivedUnionIsImplemented;
    public static String OptionalConstraints_referencedProductComponentsAreValidOnThisGenerationsValidFromDate;
    public static String OptionalConstraints_rulesWithoutReferencesAllowed;

    public static String PersistenceOptions_title;
    public static String PersistenceOptions_allowLazyFetchForSingleValuedAssociations;
    public static String PersistenceOptions_maxColumnNameLength;
    public static String PersistenceOptions_maxTableNameLength;
    public static String PersistenceOptions_tableNamingStrategy;
    public static String PersistenceOptions_tableColumnNamingStrategy;

    public static String Overview_requieredIpsFeature;
    public static String Overview_minVersion;
    public static String Overview_title;
    public static String Overview_changesOverTimeNamingConventionIdForGeneratedCode;
    public static String Overview_javaProjectContainsClassesForDynamicDatatypes;
    public static String Overview_modelProject;
    public static String Overview_persistenceSupportEnabled;
    public static String Overview_productDefinitionProject;
    public static String Overview_runtimeIdPrefix;

    public static String Generator_title;
    public static String Language_title;
    public static String DefinedDatatypes_title;
    public static String PredefinedDatatypes_title;

    public static String LanguageEditDialog_title;

    public static String ProductDefinitionComposite_treeViewer_label;
    public static String ProductDefinitionComposite_add_folder_text;
    public static String ProductDefinitionComposite_remove_folder_text;
    public static String ProductDefinitionComposite_title;

    public static String Generator_tableColumnLabel_Property;
    public static String Generator_tableViewerLabel;
    public static String Generator_tableColumnLabel_Value;

    public static String PredefinedDataypeDialog_Label;
    public static String PredefinedDataypeDialog_title;

    public static String DefinedDatatypeDialog_title;
    public static String DefinedDataypeDialog_name;

    public static String LanguageEditDialog_label;

    public static String LanguageSection_title;

}
