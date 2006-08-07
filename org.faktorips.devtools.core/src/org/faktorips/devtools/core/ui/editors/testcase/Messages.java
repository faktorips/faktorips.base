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

package org.faktorips.devtools.core.ui.editors.testcase;

import org.eclipse.osgi.util.NLS;

/**
 * @author Joerg Ortmann
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.editors.testcase.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String TestCaseEditor_title;
	public static String TestCasePageInput_title;
	public static String TestCasePageExpectedResult_title;
	
	public static String TestPolicyCmptTypeSection_title_input;
	public static String TestPolicyCmptTypeSection_title_inputDetail;
	public static String TestPolicyCmptTypeSection_title_expectedResult;
	public static String TestPolicyCmptTypeSection_title_expectedResultDetail;
	public static String TestPolicyCmptTypeSection_undefined;
	public static String TestPolicyCmptTypeSection_buttonAdd;
	public static String TestPolicyCmptTypeSection_buttonRemove;
	public static String TestPolicyCmptTypeSection_buttonProductCmpt;
}
