/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Jan Ortmann
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.editors.productcmpttype.messages"; //$NON-NLS-1$
    public static String AssociationEditDialog_derivedUnionCheckbox;
    public static String AssociationEditDialog_derivedUnionGroup;
    public static String AssociationEditDialog_derivedUnionLabel;
    public static String AssociationEditDialog_generalGroup;
    public static String AssociationEditDialog_maxCardLabel;
    public static String AssociationEditDialog_minCardLabel;
    public static String AssociationEditDialog_properties;
    public static String AssociationEditDialog_rolePluralLabel;
    public static String AssociationEditDialog_roleSingularLabel;
    public static String AssociationEditDialog_subsetCheckbox;
    public static String AssociationEditDialog_targetLabel;
    public static String AssociationEditDialog_title;
    public static String AssociationEditDialog_typeLabel;
    public static String AssociationsSection_menuOpenTargetInNewEditor;
    public static String AssociationsSection_title;
    public static String AttributeEditDialog_datatypeLabel;
    public static String AttributeEditDialog_defaultvalueLabel;
    public static String AttributeEditDialog_general;
    public static String AttributeEditDialog_modifierLabel;
    public static String AttributeEditDialog_nameLabel;
    public static String AttributeEditDialog_title;
    public static String AttributeEditDialog_valueSetSection;
    public static String AttributesSection_title;
    public static String BehaviourPage_title_behaviour;
    public static String GeneralInfoSection_abstractLabel;
    public static String GeneralInfoSection_configuredTypeLabel;
    public static String GeneralInfoSection_configuresLabel;
    public static String GeneralInfoSection_supertypeLabel;
    public static String GeneralInfoSection_title;
    public static String MethodsAndFormulaSection_title;
    public static String ProductCmptTypeEditor_title;
    public static String ProductCmptTypeMethodEditDialog_formulaCheckbox;
    public static String ProductCmptTypeMethodEditDialog_formulaGroup;
    public static String ProductCmptTypeMethodEditDialog_formulaNameLabel;
    public static String ProductCmptTypeMethodEditDialog_labelOverloadedFormulaMethod;
    public static String StructurePage_structurePageTitle;
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
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
