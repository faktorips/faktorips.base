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

package org.faktorips.devtools.core.ui.views.testrunner;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.views.testrunner.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
	public static String IpsTestCounterPanel_Runs_Label;
	public static String IpsTestCounterPanel_Errors_Label;
	public static String IpsTestCounterPanel_Failures_Label;
	public static String IpsTestRunnerViewPart_Action_RerunLastTest_Text;
	public static String IpsTestRunnerViewPart_Action_RerunLastTest_ToolTip;
	public static String IpsTestRunnerViewPart_Job_RunTest_Title;
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
	public static String IpsTestRunnerViewPart_Job_UpdateUiTitle;
	public static String OpenTestInEditorAction_Title;
	public static String OpenTestInEditorAction_ErrorDialogTestCaseNotFound_Title;
	public static String OpenTestInEditorAction_ErrorDialogTestCaseNotFound_Description;
	public static String OpenTestInEditorAction_ErrorDialogCannotOpenEditor_Title;
	public static String OpenTestInEditorAction_ErrorDialogCannotOpenEditor_Description;
	public static String OpenTestInEditorAction_ErrorCannotOpenEditor_Message;
	public static String TestRunPane_ErrorStartingTest_Entry;
}
