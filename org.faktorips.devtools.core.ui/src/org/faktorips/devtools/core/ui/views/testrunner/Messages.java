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

package org.faktorips.devtools.core.ui.views.testrunner;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.views.testrunner.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String FailurePane_MenuLabel_CopyInClipboard;
    public static String IpsTestCounterPanel_Runs_Label;
    public static String IpsTestCounterPanel_Errors_Label;
    public static String IpsTestCounterPanel_Failures_Label;
    public static String IpsTestRunnerViewPart_Action_RerunLastTest_Text;
    public static String IpsTestRunnerViewPart_Action_RerunLastTest_ToolTip;
    public static String IpsTestRunnerViewPart_Menu_ScrollLock;
    public static String IpsTestRunnerViewPart_Menu_ScrollLockTooltip;
    public static String IpsTestRunnerViewPart_TestRunPane_Text;
    public static String IpsTestRunnerViewPart_TestFailurePane_Text;
    public static String IpsTestRunnerViewPart_Menu_HorizontalOrientation;
    public static String IpsTestRunnerViewPart_Menu_VerticalOrientation;
    public static String IpsTestRunnerViewPart_Menu_AutomaticOrientation;
    public static String IpsTestRunnerViewPart_FailureFormat_FailureIn;
    public static String IpsTestRunnerViewPart_FailureFormat_Actual;
    public static String IpsTestRunnerViewPart_FailureFormat_Expected;
    public static String IpsTestRunnerViewPart_FailureFormat_Attribute;
    public static String IpsTestRunnerViewPart_FailureFormat_Object;
    public static String IpsTestRunnerViewPart_FailureFormat_Message;
    public static String IpsTestRunnerViewPart_Job_UpdateUiTitle;
    public static String OpenTestInEditorAction_Title;
    public static String OpenTestInEditorAction_ErrorDialogTestCaseNotFound_Title;
    public static String OpenTestInEditorAction_ErrorDialogTestCaseNotFound_Description;
    public static String OpenTestInEditorAction_ErrorDialogCannotOpenEditor_Title;
    public static String OpenTestInEditorAction_ErrorDialogCannotOpenEditor_Description;
    public static String OpenTestInEditorAction_ErrorCannotOpenEditor_Message;
    public static String TestRunPane_ErrorStartingTest_Entry;
    public static String IpsTestRunnerViewPart_Message_TestFinishedAfterNSeconds;
    public static String IpsTestRunnerViewPart_Action_StopTest;
    public static String IpsTestRunnerViewPart_Action_StopTest_ToolTip;
    public static String IpsTestRunnerViewPart_Action_NextFailure;
    public static String IpsTestRunnerViewPart_Action_NextFailureToolTip;
    public static String IpsTestRunnerViewPart_Action_PrevFailure;
    public static String IpsTestRunnerViewPart_Action_PrevFailureToolTip;
    public static String IpsTestRunnerViewPart_Action_ShowFailuresOnly;
    public static String IpsTestRunnerViewPart_Action_ShowFailuresOnly_ToolTip;
    public static String IpsTestRunnerViewPart_Action_ShowStackTrace;
    public static String IpsTestRunnerViewPart_Action_ShowStackTraceToolTip;
    public static String FailurePane_DialogClassNotFound_Title;
    public static String FailurePane_DialogClassNotFound_Description;
    public static String FailurePane_DialogClassNotFoundInSrcFolder_Title;
    public static String FailurePane_DialogClassNotFoundInSrcFolder_Description;
    public static String TestRunPane_Menu_GoToFile;
    public static String TestRunPane_Menu_Run;
    public static String TestRunPane_MenuDebug;

}
