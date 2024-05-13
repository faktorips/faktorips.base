/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.editors.pctype.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String AssociationDerivedUnionGroup_groupDerivedUnion;

    public static String AssociationDerivedUnionGroup_labelDefinesSubsetOfDerivedUnion;

    public static String AssociationDerivedUnionGroup_labelIsDerivedUnion;

    public static String AssociationDerivedUnionGroup_labelSubsetTextField;

    public static String AssociationDerivedUnionGroup_toolTipDerivedUnion;

    public static String AssociationDerivedUnionGroup_toolTipOverride;

    public static String AssociationQualificationGroup_groupQualification;

    public static String AssociationQualificationGroup_labelIsQualified;

    public static String AssociationQualificationGroup_labelIsQualifiedByType;

    public static String AssociationQualificationGroup_labelNote;

    public static String AssociationQualificationGroup_labelNoteQualificationOnlyMasterDetail;

    public static String AssociationQualificationGroup_labelNoteQualificationOnlyTargetConfByProduct;

    public static String AssociationQualificationGroup_noteContrainHowTo;

    public static String AssociationQualificationGroup_noteIsConstrained;

    public static String AssociationQualificationGroup_noteIsNotConstrained;

    public static String AssociationQualificationGroup_noteQualifiedMultiplictyPerQualifiedInstance;

    public static String AssociationEditDialog_associationIsQualified;

    public static String AssociationEditDialog_associationTypeLabel;

    public static String AssociationEditDialog_generalGroup;

    public static String AssociationEditDialog_group_configuration;

    public static String AssociationEditDialog_group_selectExplicitly;

    public static String AssociationEditDialog_info_dialogAutoFixErrors;

    public static String AssociationEditDialog_check_configuration;

    public static String AssociationEditDialog_check_selectMatchingAssociationExpliclitly;

    public static String AssociationEditDialog_inverseAssociationLabel;

    public static String AssociationEditDialog_label_foundByFaktorIps;

    public static String AssociationEditDialog_label_matchingAssociation;

    public static String AssociationEditDialog_label_none;

    public static String AssociationEditDialog_label_productCmptType;

    public static String AssociationEditDialog_labelAssociationIsOwningSideOfManyToMany;

    public static String AssociationEditDialog_labelAssociationIsTransient;

    public static String AssociationEditDialog_labelCascadeType;

    public static String AssociationEditDialog_labelCascadeTypeMerge;

    public static String AssociationEditDialog_labelCascadeTypePersist;

    public static String AssociationEditDialog_labelCascadeTypeRefresh;

    public static String AssociationEditDialog_labelCascadeTypeRemove;

    public static String AssociationEditDialog_labelFetchType;

    public static String AssociationEditDialog_labelForeignKeyColumnName;

    public static String AssociationEditDialog_labelForeignKeyJoinColumn;

    public static String AssociationEditDialog_labelJoinColumnNameNullable;

    public static String AssociationEditDialog_labelJoinTable;

    public static String AssociationEditDialog_labelJoinTableName;

    public static String AssociationEditDialog_labelOrphanRemoval;

    public static String AssociationEditDialog_labelOverwriteDefaultCascadeTypes;

    public static String AssociationEditDialog_labelPersistence;

    public static String AssociationEditDialog_labelProperties;

    public static String AssociationEditDialog_labelSourceColumnName;

    public static String AssociationEditDialog_labelTargetColumnName;

    public static String AssociationEditDialog_maximumCardinality;

    public static String AssociationEditDialog_minimumCardinality;

    public static String AssociationEditDialog_multiplicityIsDefineddPerQualifier;

    public static String AssociationEditDialog_note;

    public static String AssociationEditDialog_noteForeignKeyColumnDefinedInInverseAssociation;

    public static String AssociationEditDialog_noteForeignKeyIsColumnOfTheTargetEntity;

    public static String AssociationEditDialog_qualificationGroup;

    public static String AssociationEditDialog_qualificationOnlyForMasterDetail;

    public static String AssociationEditDialog_qualificationOnlyIfTheTargetTypeIsConfigurable;

    public static String AssociationEditDialog_qualifiedByType;

    public static String AssociationEditDialog_sharedAssociations;

    public static String AssociationEditDialog_sharedAssociationsTooltip;

    public static String AssociationEditDialog_tab_productAssociation;

    public static String AssociationEditDialog_targetLabel;

    public static String AssociationEditDialog_targetRolePluralLabel;

    public static String AssociationEditDialog_targetRoleSingularLabel;

    public static String AssociationEditDialog_textFirstPage;

    public static String AssociationEditDialog_textNotSupportedByPersistenceProvider;

    public static String AssociationEditDialog_title;

    public static String AssociationEditDialog_constrain;

    public static String PolicyCmptTypeAssociationsSection_menuOpenTargetInNewEditor;

    public static String PolicyCmptTypeAssociationsSection_newButton;

    public static String PctEditor_title;

    public static String PersistentSection_labelIndexName;

    public static String PersistentAssociationSection_labelAssociationTarget;

    public static String PersistentAssociationSection_labelFetchType;

    public static String PersistentAssociationSection_labelJoinColumnName;

    public static String PersistentAssociationSection_labelJoinColumnNullable;

    public static String PersistentAssociationSection_labelJoinTableName;

    public static String PersistentAssociationSection_labelOrphanRemoval;

    public static String PersistentAssociationSection_labelSourceColumnName;

    public static String PersistentAssociationSection_labelTargetColumnName;

    public static String PersistentAssociationSection_titleAssociations;

    public static String PersistentAttributeSection_labelAttributeName;

    public static String PersistentAttributeSection_labelColumnDefinition;

    public static String PersistentAttributeSection_labelColumnName;

    public static String PersistentAttributeSection_labelConverter;

    public static String PersistentAttributeSection_labelNullable;

    public static String PersistentAttributeSection_labelPrecision;

    public static String PersistentAttributeSection_labelScale;

    public static String PersistentAttributeSection_labelSize;

    public static String PersistentAttributeSection_labelUnique;

    public static String PersistentAttributeSection_titleAttributes;

    public static String PersistentTypeInfoSection_labelColumnName;

    public static String PersistentTypeInfoSection_labelColumnLength;

    public static String PersistentTypeInfoSection_labelColumnValue;

    public static String PersistentTypeInfoSection_labelDatatype;

    public static String PersistentTypeInfoSection_labelDescriminator;

    public static String PersistentTypeInfoSection_labelInheritanceStrategy;

    public static String PersistentTypeInfoSection_labelPersistentType;

    public static String PersistentTypeInfoSection_labelTable;

    public static String PersistentTypeInfoSection_labelTableName;

    public static String PersistentTypeInfoSection_labelThisTypeDefinesTheDiscriminatorColumn;

    public static String PersistentTypeInfoSection_labelUseTableDefinedInSupertype;

    public static String PersistentTypeInfoSection_sectionTitleJpaEntityInformation;

    public static String PersistentTypeInfoSection_textRootEntityNotFound;

    public static String PersistentTypeInfoSection_textSupertypeNotFound;

    public static String AttributeEditDialog_attributeComputed;
    public static String AttributeEditDialog_changeOverTimeCheckbox;
    public static String AttributeEditDialog_ConfigurationGroup;
    public static String AttributeEditDialog_ConstantAttributesCantBeConfigured;
    public static String AttributeEditDialog_createNewMethod;
    public static String AttributeEditDialog_emptyString;
    public static String AttributeEditDialog_generalGroup;
    public static String AttributeEditDialog_Info;
    public static String AttributeEditDialog_labelActivateValidationRule;
    public static String AttributeEditDialog_labelGenericValidation;
    public static String AttributeEditDialog_labelAttributeIsTransient;
    public static String AttributeEditDialog_labelAttrType;
    public static String AttributeEditDialog_labelCategory;
    public static String AttributeEditDialog_labelColumnName;
    public static String AttributeEditDialog_labelColumnScale;
    public static String AttributeEditDialog_labelColumnSize;
    public static String AttributeEditDialog_labelDatatype;
    public static String AttributeEditDialog_labelDatatypeConverterClass;
    public static String AttributeEditDialog_labelDefaultValue;
    public static String AttributeEditDialog_labelIndexName;
    public static String AttributeEditDialog_labelModifier;
    public static String AttributeEditDialog_labelName;
    public static String AttributeEditDialog_labelNullable;
    public static String AttributeEditDialog_labelPersistence;
    public static String AttributeEditDialog_labelPersistentProperties;
    public static String AttributeEditDialog_labelPrecision;
    public static String AttributeEditDialog_labelSqlColumnDefinition;
    public static String AttributeEditDialog_labelTemporalType;
    public static String AttributeEditDialog_labelUnique;
    public static String AttributeEditDialog_MethodDoesNotExist;
    public static String AttributeEditDialog_methodLink;
    public static String AttributeEditDialog_methodNote;
    public static String AttributeEditDialog_overwritesNote;
    public static String AttributeEditDialog_propertiesTitle;
    public static String AttributeEditDialog_questionCreateMethod;
    public static String AttributeEditDialog_RelevanceConfiguredByProduct;
    public static String AttributeEditDialog_ruleTitle;
    public static String AttributeEditDialog_textNotSupportedByPersistenceProvider;
    public static String AttributeEditDialog_title;
    public static String AttributeEditDialog_tooltipActivateValidationRule;
    public static String AttributeEditDialog_tooltipGenericValidation;
    public static String AttributeEditDialog_TypeCantBeFound;
    public static String AttributeEditDialog_validationRuleTitle;
    public static String AttributeEditDialog_ValueSetConfiguredByProduct;
    public static String AttributeEditDialog_valuesetTitle;

    public static String AttributesSection_deleteMessage;
    public static String AttributesSection_deleteTitle;
    public static String BehaviourPage_title;
    public static String RulesSection_title;
    public static String RulesSection_labelSeparateValidatorClass;
    public static String StructurePage_title;

    public static String GeneralInfoSection_labelAbstractClass;
    public static String GeneralInfoSection_labelProduct;
    public static String GeneralInfoSection_labelType;
    public static String GeneralInfoSection_linkSuperclass;
    public static String GeneralInfoSection_title;

    public static String RuleEditDialog_ActivatedByDefault_CheckboxLabel;

    public static String RuleEditDialog_attrTitle;
    public static String RuleEditDialog_ChangingOverTime_CheckboxLabel;
    public static String RuleEditDialog_Configurable_CheckboxLabel;
    public static String RuleEditDialog_contains;
    public static String RuleEditDialog_labelCode;
    public static String RuleEditDialog_labelName;
    public static String RuleEditDialog_labelCategory;
    public static String RuleEditDialog_labelSeverity;
    public static String RuleEditDialog_labelSpecifiedInSrc;
    public static String RuleEditDialog_messageGroupTitle;
    public static String RuleEditDialog_generalTitle;

    public static String RuleEditDialog_title;
    public static String ValidatedAttributesControl_description;
    public static String ValidatedAttributesControl_label;

    public static String ValidationRuleMarkerUI_Label_NoMarkerEnumDefined;

    public static String ValidationRuleMarkerUI_TabName_Markers;

    public static String ValidationRuleMarkerPMO_Label_illegalEntry;

    public static String RuleEditDialog_groupText;

}
