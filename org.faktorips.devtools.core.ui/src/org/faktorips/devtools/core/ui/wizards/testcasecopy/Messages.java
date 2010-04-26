/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.testcasecopy;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.testcasecopy.messages"; //$NON-NLS-1$
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
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
