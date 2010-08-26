/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.testcase.transform;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.testcase.transform.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String TestCaseTransformer_Error_DuplicateTestAttributeValue;
    public static String TestCaseTransformer_Error_FileAlreadyExists;
    public static String TestCaseTransformer_Error_TestAttributeWithTypeNotFound;
    public static String TestCaseTransformer_Error_TestPolicyCmptTypeNotFound;
    public static String TestCaseTransformer_MessageTextInput;
    public static String TestCaseTransformer_MessageTextInputExpectedResult;
    public static String TestCaseTransformer_WarningNoTestCasesFound;
    public static String TestCaseTransformer_Error_NoTypeAttributeSpecified;
    public static String TestCaseTransformer_Error_PackageFragmentNotFound;
    public static String TestCaseTransformer_Job_Title;
    public static String TestCaseTransformer_Error_ImportPackageEqualsTargetPackage;
    public static String TestCaseTransformer_Error_Skip_Because_ImportPackageEqualsTargetPackage;
    public static String TestCaseTransformer_Error_TestCaseType_Not_Found;
    public static String TestCaseTransformer_Error_WrongTypeOfTestPolicyCmpt;

    public static String TransformWizard_title;
    public static String TransformWizard_SelectTarget_title;
    public static String TransformWizard_SelectTarget_description;
    public static String TransformWizard_SelectTestCaseType_title;
    public static String TransformWizard_SelectTestCaseType_description;
    public static String TransformWizard_SelectTestCaseType_TestCaseTypeLabel;
    public static String TransformWizard_SelectTestCaseType_ExtensionLabel;

    public static String TransformRuntimeTestCaseWizard_Error_TestCaseTypeNotExists;

}
