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

package org.faktorips.devtools.core.ui.editors.testcase.deltapresentation;

import org.eclipse.osgi.util.NLS;

/**
 * @author Joerg Ortmann
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.editors.testcase.deltapresentation.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

    public static String TestCaseDeltaDialog_Title;
    public static String TestCaseDeltaDialog_Message;
    public static String TestCaseDeltaDialog_Label_DifferencesTree;
    public static String TestCaseDeltaDialog_Button_Fix;
    public static String TestCaseDeltaDialog_Button_Ignore;
    public static String TestCaseDeltaType_MissingRootTestObject;
    public static String TestCaseDeltaType_MissingTestAttributeValue;
    public static String TestCaseDeltaType_MissingTestParam;
    public static String TestCaseDeltaType_MissingTestAttribute;
    public static String TestCaseDeltaLabelProvider_Undefined;
    public static String TestCaseDeltaType_DifferentSortOrder;
}
