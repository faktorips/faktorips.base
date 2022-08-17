/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.testcasecopy;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.testcasecopy.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String TestCaseCopyDesinationPage_ColumnReplaceWith;
    public static String TestCaseCopyDesinationPage_ColumnTitleToReplace;
    public static String TestCaseCopyDesinationPage_InfoMessage;
    public static String TestCaseCopyDesinationPage_InfoMessageReplacedVersion;
    public static String TestCaseCopyDesinationPage_LabelCopyExpectedResultValues;
    public static String TestCaseCopyDesinationPage_LabelCopyInputValues;
    public static String TestCaseCopyDesinationPage_LabelCopyTestValues;
    public static String TestCaseCopyDesinationPage_LabelDestinationPackage;
    public static String TestCaseCopyDesinationPage_LabelRadioBtnManualReplace;
    public static String TestCaseCopyDesinationPage_LabelRadioBtnReplaceProdCmptVersion;
    public static String TestCaseCopyDesinationPage_LabelSrcFolder;
    public static String TestCaseCopyDesinationPage_LabelTargetName;
    public static String TestCaseCopyDesinationPage_Title;
    public static String TestCaseCopyDesinationPage_TitleProductCmptReplaceGroup;
    public static String TestCaseCopyDesinationPage_TitleTargetGroup;
    public static String TestCaseCopyDesinationPage_ValidationErrorBadTarget;
    public static String TestCaseCopyDesinationPage_ValidationTargetAlreadyExists;
    public static String TestCaseCopyDesinationPage_ValidationWarningTargetPackageWillBeCreated;
    public static String TestCaseCopyWizard_title;
    public static String TestCaseStructurePage_LabelProductCmptCandidates;
    public static String TestCaseStructurePage_LabelTestCaseStructure;
    public static String TestCaseStructurePage_Title;
    public static String TestCaseStructurePage_ValidationErrorNoElementSelected;

}
