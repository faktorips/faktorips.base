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

package org.faktorips.devtools.core.ui.editors.testcase.deltapresentation;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.editors.testcase.deltapresentation.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
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
