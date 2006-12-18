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

package org.faktorips.devtools.core.internal.model.testcase;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Thorsten Guenther
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.model.testcase.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String IpsTestRunner_Error_CouldNotConnect;
	public static String IpsTestRunner_Job_Name;
    public static String IpsTestRunner_lauchConfigurationDefaultName;
	public static String TestAttributeValue_ValidateError_TestAttributeNotFound;
	public static String TestAttributeValue_ValidateError_AttributeNotFound;
	public static String TestCase_Error_NoRelationOrPolicyCmptGiven;
	public static String TestCase_Error_TestPolicyCmptNotFound;
	public static String TestCase_ValidateError_TestCaseTypeNotFound;
	public static String TestPolicyCmpt_Error_RelationNotFound;
	public static String TestPolicyCmpt_ValidationError_TestCaseTypeNotFound;
	public static String TestPolicyCmpt_ValidationError_ProductCmptRequired;
	public static String TestPolicyCmptRelation_ValidationError_TestCaseTypeParamNotFound;
	public static String TestPolicyCmptRelation_ValidationError_ModelRelationNotFound;
	public static String TestPolicyCmptRelation_ValidationError_MinimumNotReached;
	public static String TestPolicyCmptRelation_ValidationError_MaximumReached;
	public static String TestPolicyCmptRelation_ValidationError_TestCaseTypeNotFound;
	public static String TestPolicyCmptRelation_ValidationError_AssoziationNotFound;
	public static String TestValue_ValidateError_TestValueParamNotFound;
	public static String TestValue_ValidateError_DatatypeNotFound;
    public static String IpsTestRunner_Error_WrongTestProtocol;
    public static String TestPolicyCmpt_ValidationWarning_PolicyCmptNotExists;
    public static String TestPolicyCmpt_ValidationWarning_ProductComponentNotExists;
    public static String TestCase_Error_WrongInstanceParam;
    public static String TestCase_Error_MoreThanOneObject;
    public static String TestCase_Error_WrongInstanceTestPolicyCmpt;
    public static String TestAttributeValue_Error_WrongType;
    public static String TestValue_ErrorWrongType;
    public static String IpsTestRunner_Error_WrongHeapSize;
    public static String TestPolicyCmpt_ValidationError_ProductCmptNotRequiredButIsRelatedToProductCmpt;
    public static String TestCase_Error_TestParameterNotFound;
    public static String TestRule_ValidationError_ValidationRuleNotAvailable;
    public static String TestRule_ValidationError_DuplicateValidationRule;
    public static String TestRule_ValidationError_TestRuleParameterNotFound;
}
