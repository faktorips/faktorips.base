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

package org.faktorips.devtools.core.internal.model.testcasetype;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Thorsten Guenther
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.model.testcasetype.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String TestValueParameter_ValidateError_ValueDatatypeNotFound;
    public static String TestPolicyCmptTypeParameter_ValidationError_PolicyCmptTypeNotExists;
    public static String TestPolicyCmptTypeParameter_ValidationError_MinGreaterThanMax;
    public static String TestPolicyCmptTypeParameter_ValidationError_MaxLessThanMin;
    public static String TestPolicyCmptTypeParameter_ValidationError_RoleNotAllowed;
    public static String TestValueParameter_ValidationError_RoleNotAllowed;
    public static String TestParameter_ValidationError_DuplicateName;
    public static String TestCaseType_Error_MoreThanOneValueParamWithRoleAndName;
    public static String TestCaseType_Error_MoreThanOnePolicyParamWithRoleAndName;
    public static String TestCaseType_Error_MoreThanOneParamWithName;
    public static String TestCaseType_Error_MoreThanOneParamWithRoleAndName;
    public static String TestPolicyCmptTypeParameter_ValidationError_RelationNotExists;
    public static String TestPolicyCmptTypeParameter_ValidationError_TargetOfRelationNotExists;
    public static String TestPolicyCmptTypeParameter_ValidationError_PolicyCmptNotAllowedForRelation;


}
