/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype.relationwizard;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.editors.pctype.relationwizard.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String NewPcTypeRelationWizard_title;
	
	public static String NewPcTypeRelationWizard_target_title;	
	public static String NewPcTypeRelationWizard_target_description;
	public static String NewPcTypeRelationWizard_target_labelTarget;
	public static String NewPcTypeRelationWizard_target_labelType;
	public static String NewPcTypeRelationWizard_target_labelReadOnlyContainer;
	public static String NewPcTypeRelationWizard_target_labelDescription;
	public static String NewPcTypeRelationWizard_target_askForAutomaticallySavingTitle;
	public static String NewPcTypeRelationWizard_target_askForAutomaticallySaving;
	
	public static String NewPcTypeRelationWizard_containerRelation_title;
	public static String NewPcTypeRelationWizard_containerRelation_description;
	public static String NewPcTypeRelationWizard_containerRelation_labelContainerRelation;
	
	public static String NewPcTypeRelationWizard_properties_title;
	public static String NewPcTypeRelationWizard_properties_description;
	public static String NewPcTypeRelationWizard_properties_labelMinCardinality;
	public static String NewPcTypeRelationWizard_properties_labelMaxCardinality;
	public static String NewPcTypeRelationWizard_properties_labelTargetRoleSingular;
	public static String NewPcTypeRelationWizard_properties_labelTargetRolePlural;
	public static String NewPcTypeRelationWizard_properties_labelProdRelevant;
	public static String NewPcTypeRelationWizard_properties_labelMinCardinalityProdRelevant;
	public static String NewPcTypeRelationWizard_properties_labelMaxCardinalityProdRelevant;
	public static String NewPcTypeRelationWizard_properties_labelTargetRoleSingularProdRelevant;
	public static String NewPcTypeRelationWizard_properties_labelTargetRolePluralProdRelevant;
	public static String NewPcTypeRelationWizard_properties_labelGrpBoxPolicySide;
	public static String NewPcTypeRelationWizard_properties_labelGrpBoxProductSide;
	
	public static String NewPcTypeRelationWizard_reverseRelation_title;
	public static String NewPcTypeRelationWizard_reverseRelation_description;
	public static String NewPcTypeRelationWizard_reverseRelation_labelNewReverseRelation;
	public static String NewPcTypeRelationWizard_reverseRelation_labelUseExistingRelation;
	public static String NewPcTypeRelationWizard_reverseRelation_labelNoReverseRelation;	
	
	public static String NewPcTypeRelationWizard_reverseRelationProp_title;
	public static String NewPcTypeRelationWizard_reverseRelationProp_description_new;
	public static String NewPcTypeRelationWizard_reverseRelationProp_description_existing;
	public static String NewPcTypeRelationWizard_reverseRelationProp_labelTarget;
	public static String NewPcTypeRelationWizard_reverseRelationProp_labelType;
	public static String NewPcTypeRelationWizard_reverseRelationProp_labelExistingRelation;
	
	public static String NewPcTypeRelationWizard_error_title;
	public static String NewPcTypeRelationWizard_error_desciption;	
}
