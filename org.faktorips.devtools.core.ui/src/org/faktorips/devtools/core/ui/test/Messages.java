/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.test;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.test.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String TestSelectionComposite_labelButtonDelete;
    public static String TestSelectionTab_ErrorUnknownProjekt;
    public static String TestSelectionTab_groupParameter;
    public static String TestSelectionTab_labelGroupProject;
    public static String TestSelectionTab_labelGroupTestSelection;
    public static String TestSelectionTab_labelMaxHeapSize;
    public static String TestSelectionTab_labelProject;
    public static String TestSelectionTab_title;
    public static String TestSelectionComposite_dialogTextSelectTestCase;
    public static String TestSelectionComposite_dialogTitleSelectTestCase;
    public static String TestSelectionComposite_errorProjectNotDetermined;
    public static String TestSelectionComposite_labelButtonAddTestCase;
    public static String TestSelectionComposite_labelButtonAddTestSuite;
    public static String TestSelectionComposite_labelButtonDown;
    public static String TestSelectionComposite_labelButtonUp;

}
