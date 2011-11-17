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

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.editors.productcmpttype.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String ProductCmptTypeAssociationsSection_menuOpenTargetInNewEditor;

    public static String AssociationEditDialog_associationLabel_AssociationNameInPolicyCmptType;

    public static String AssociationEditDialog_check_selectExplicitly;

    public static String AssociationEditDialog_derivedUnionCheckbox;
    public static String AssociationEditDialog_derivedUnionGroup;
    public static String AssociationEditDialog_derivedUnionLabel;
    public static String AssociationEditDialog_generalGroup;

    public static String AssociationEditDialog_group_selectExplicitly;

    public static String AssociationEditDialog_hint_AlreadyConfiguredIn;

    public static String AssociationEditDialog_info_dialogAutoFixErrors;

    public static String AssociationEditDialog_label_foundAssociation;

    public static String AssociationEditDialog_label_matchingAssociation;

    public static String AssociationEditDialog_label_none;
    public static String AssociationEditDialog_maxCardLabel;
    public static String AssociationEditDialog_minCardLabel;
    public static String AssociationEditDialog_properties;
    public static String AssociationEditDialog_rolePluralLabel;
    public static String AssociationEditDialog_roleSingularLabel;
    public static String AssociationEditDialog_subsetCheckbox;
    public static String AssociationEditDialog_tab_policyAssociation;

    public static String AssociationEditDialog_targetLabel;
    public static String AssociationEditDialog_title;
    public static String AssociationEditDialog_typeLabel;

    public static String LinksCategorySection_explanation;
    public static String LinksCategorySection_title;

    public static String AttributeEditDialog_changeOverTimeCheckbox;
    public static String AttributeEditDialog_changeOverTimeLabel;

    public static String AttributeEditDialog_datatypeLabel;
    public static String AttributeEditDialog_defaultvalueLabel;
    public static String AttributeEditDialog_categoryLabel;
    public static String AttributeEditDialog_general;
    public static String AttributeEditDialog_modifierLabel;
    public static String AttributeEditDialog_nameLabel;
    public static String AttributeEditDialog_title;
    public static String AttributeEditDialog_valueSetSection;
    public static String BehaviourPage_title_behaviour;
    public static String CustomIconPage_Page_Tab_Label;

    public static String CustomIconSection_BrowseButtonText;

    public static String CustomIconSection_ConfigurationDescription;

    public static String CustomIconSection_CustomPathText;

    public static String CustomIconSection_IconPreviewLabel;

    public static String CustomIconSection_SectionTitle;

    public static String CustomIconSection_SelectImageDialog_Description;

    public static String CustomIconSection_SelectImageDialog_Error_NoFileSelected;

    public static String CustomIconSection_SelectImageDialog_Title;

    public static String CustomIconSection_UnsupportedType_Label;

    public static String GeneralInfoSection_abstractLabel;
    public static String GeneralInfoSection_configuredTypeLabel;
    public static String GeneralInfoSection_configuresLabel;

    public static String GeneralInfoSection_label_layerSupertype;
    public static String GeneralInfoSection_supertypeLabel;
    public static String GeneralInfoSection_title;
    public static String ProductCmptTypeMethodsSection_title;
    public static String ProductCmptTypeEditor_title;
    public static String ProductCmptTypeMethodEditDialog_formulaCheckbox;
    public static String ProductCmptTypeMethodEditDialog_formulaGroup;
    public static String ProductCmptTypeMethodEditDialog_formulaNameLabel;
    public static String ProductCmptTypeMethodEditDialog_labelOverloadsFormula;
    public static String ProductCmptTypeMethodEditDialog_categoryLabel;
    public static String StructurePage_structurePageTitle;
    public static String TableStructureUsageSection_menuOpenTargetInNewEditorPlural;
    public static String TableStructureUsageSection_menuOpenTargetInNewEditorSingular;
    public static String TableStructureUsageSection_title;
    public static String TblsStructureUsageEditDialog_addButton;
    public static String TblsStructureUsageEditDialog_contentRequiredLabel;
    public static String TblsStructureUsageEditDialog_downButton;
    public static String TblsStructureUsageEditDialog_propertiesPageTtitle;
    public static String TblsStructureUsageEditDialog_removeButton;
    public static String TblsStructureUsageEditDialog_rolenameLabel;
    public static String TblsStructureUsageEditDialog_selectStructurDialogMessage;
    public static String TblsStructureUsageEditDialog_selectStructurDialogTitle;
    public static String TblsStructureUsageEditDialog_tableStructure;
    public static String TblsStructureUsageEditDialog_tableStructuresGroup;
    public static String TblsStructureUsageEditDialog_title;
    public static String TblsStructureUsageEditDialog_upButton;
    public static String TableStructureUsageEditDialog_categoryLabel;

    public static String CategoryPage_tagPageName;

    public static String CategoryCompositionSection_sectionTitle;

    public static String CategorySection_buttonUp;
    public static String CategorySection_buttonDown;
    public static String CategorySection_buttonChangeCategory;

    public static String NewCategoryAction_label;
    public static String NewCategoryAction_tooltip;

    public static String ChangeCategoryDialog_title;
    public static String ChangeCategoryDialog_message;

    public static String MoveCategoryUpAction_label;
    public static String MoveCategoryUpAction_tooltip;
    public static String MoveCategoryDownAction_label;
    public static String MoveCategoryDownAction_tooltip;
    public static String MoveCategoryLeftAction_label;
    public static String MoveCategoryLeftAction_tooltip;
    public static String MoveCategoryRightAction_label;
    public static String MoveCategoryRightAction_tooltip;
    public static String DeleteCategoryAction_label;
    public static String DeleteCategoryAction_tooltip;
    public static String EditCategoryAction_label;
    public static String EditCategoryAction_tooltip;

    public static String CategoryEditDialog_defaultFormulaSignatureDefinitions;
    public static String CategoryEditDialog_defaultPolicyComponentTypeAttributes;
    public static String CategoryEditDialog_defaultProductComponentTypeAttributes;
    public static String CategoryEditDialog_defaultsExplanationLabel;
    public static String CategoryEditDialog_defaultsGroup;
    public static String CategoryEditDialog_positionGroup;
    public static String CategoryEditDialog_positionLeft;
    public static String CategoryEditDialog_positionRight;
    public static String CategoryEditDialog_defaultTableStructureUsages;
    public static String CategoryEditDialog_defaultValidationRules;
    public static String CategoryEditDialog_title;
    public static String CategoryEditDialog_generalTabTitle;

    public static String CategoryEditDialog_nameLabel;

}
