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
