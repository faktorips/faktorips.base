/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.testcase;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.model.internal.testcase.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String IpsTestRunner_Error_CouldNotConnect;
    public static String IpsTestRunner_Error_ProjectTheTestBelongsToNotFound;
    public static String IpsTestRunner_InfoDialogProjectWasNotBuild_Text;
    public static String IpsTestRunner_InfoDialogTestAlreadyRunning_Text;
    public static String IpsTestRunner_InfoDialogTestCouldNotStarted_Title;
    public static String IpsTestRunner_InfoDialogErrorsInProject_Text;
    public static String IpsTestRunner_Job_Name;
    public static String IpsTestRunner_lauchConfigurationDefaultName;
    public static String IpsTestRunner_validationErrorInvalidName;
    public static String IpsTestRunner_Error_WrongTestProtocol;
    public static String IpsTestRunner_Error_WrongHeapSize;

    public static String TestAttributeValue_ValidateError_TestAttributeNotFound;
    public static String TestAttributeValue_ValidateError_AttributeNotFound;
    public static String TestAttributeValue_Error_WrongType;

    public static String TestCase_Error_NoLinkOrPolicyCmptGiven;
    public static String TestCase_Error_TestPolicyCmptNotFound;
    public static String TestCase_ValidateError_TestCaseTypeNotFound;
    public static String TestCase_Error_WrongInstanceParam;
    public static String TestCase_Error_MoreThanOneObject;
    public static String TestCase_Error_WrongInstanceTestPolicyCmpt;
    public static String TestCase_Error_TestParameterNotFound;

    public static String TestPolicyCmpt_Error_MoveNotPossibleBelongsToNoLink;
    public static String TestPolicyCmpt_Error_LinkNotFound;
    public static String TestPolicyCmpt_Error_ProductCmpAndPolicyCmptTypeGiven;
    public static String TestPolicyCmpt_TestPolicyCmpt_ValidationError_PolicyCmptIsAbstract;
    public static String TestPolicyCmpt_TestPolicyCmpt_ValidationError_PolicyCmptTypeNotAllowedIfProductCmptIsSet;
    public static String TestPolicyCmpt_TestPolicyCmpt_ValidationError_PolicyCmptTypeNotExists;
    public static String TestPolicyCmpt_TestPolicyCmpt_ValidationError_ProductCmpCouldNotValidatedParentNotFound;
    public static String TestPolicyCmpt_TestPolicyCmpt_ValidationError_ProductCmpNotAllowed;
    public static String TestPolicyCmpt_TestPolicyCmpt_ValidationError_ProductCmpNotAllowedRoot;
    public static String TestPolicyCmpt_TestPolicyCmpt_ValidationErrorPolicyCmptTypeNoSubtypeOrSameTypeParam;
    public static String TestPolicyCmpt_ValidationError_TestCaseTypeParamNotFound;
    public static String TestPolicyCmpt_ValidationError_ProductCmptRequired;
    public static String TestPolicyCmpt_ValidationError_ProductCmptNotRequiredButIsRelatedToProductCmpt;
    public static String TestPolicyCmpt_ValidationWarning_PolicyCmptNotExists;
    public static String TestPolicyCmpt_ValidationWarning_ProductComponentNotExists;

    public static String TestPolicyCmptLink_ValidationError_TestCaseTypeParamNotFound;
    public static String TestPolicyCmptLink_ValidationError_ModelAssociationNotFound;
    public static String TestPolicyCmptLink_ValidationError_MinimumNotReached;
    public static String TestPolicyCmptLink_ValidationError_MaximumReached;
    public static String TestPolicyCmptLink_ValidationError_TestCaseTypeNotFound;
    public static String TestPolicyCmptLink_ValidationError_AssoziationNotFound;

    public static String TestValue_ValidateError_TestValueParamNotFound;
    public static String TestValue_ValidateError_DatatypeNotFound;
    public static String TestValue_ErrorWrongType;

    public static String TestRule_ValidationError_ValidationRuleNotAvailable;
    public static String TestRule_ValidationError_DuplicateValidationRule;
    public static String TestRule_ValidationError_TestRuleParameterNotFound;

}
