/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String AbstractCompletionProcessor_labelDefaultPackage;

    public static String DefaultLabelProvider_labelDefaultPackage;

    public static String FaktorIpsPreferencePage_advancedTeamFunctionsInProductDefExplorer;

    public static String FaktorIpsPreferencePage_label_fourSections;

    public static String FaktorIpsPreferencePage_label_twoSections;

    public static String FaktorIpsPreferencePage_labelEnumTypeDisplay;

    public static String FaktorIpsPreferencePage_labeRangeEditFieldsInOneRow;

    public static String FaktorIpsPreferencePage_title_numberOfSections;

    public static String FaktorIpsPreferencePage_title_refactoringMode;

    public static String FaktorIpsPreferencePage_label_direct;

    public static String FaktorIpsPreferencePage_label_explicit;

    public static String FaktorIpsPreferencePage_tooltip_direct;

    public static String FaktorIpsPreferencePage_tooltip_explicit;

    public static String PdPackageSelectionDialog_title;

    public static String PdPackageSelectionDialog_description;

    public static String FaktorIpsPreferencePage_labelWorkingDate;

    public static String FaktorIpsPreferencePage_labelNullValue;

    public static String FaktorIpsPreferencePage_labelProductTypePostfix;

    public static String FaktorIpsPreferencePage_labelNamingScheme;

    public static String PdObjectSelectionDialog_labelMatches;

    public static String PdObjectSelectionDialog_labelQualifier;

    public static String DatatypeSelectionDialog_title;

    public static String DatatypeSelectionDialog_description;

    public static String DatatypeSelectionDialog_labelMatchingDatatypes;

    public static String DatatypeSelectionDialog_msgLabelQualifier;

    public static String PdSourceRootSelectionDialog_title;

    public static String PdSourceRootSelectionDialog_description;

    public static String AbstractCompletionProcessor_msgNoProject;

    public static String AbstractCompletionProcessor_msgInternalError;

    public static String FaktorIpsPreferencePage_FaktorIpsPreferencePage_enableGenerating;

    public static String FaktorIpsPreferencePage_LabelFormattingOfValues;

    public static String FaktorIpsPreferencePage_labelEditRecentGenerations;

    public static String FaktorIpsPreferencePage_labelCanNavigateToModelOrSourceCode;

    public static String FaktorIpsPreferencePage_titleWorkingMode;

    public static String FaktorIpsPreferencePage_labelWorkingModeBrowse;

    public static String FaktorIpsPreferencePage_labelWorkingModeEdit;

    public static String FaktorIpsPreferencePage_modifyRuntimeId;

    public static String FaktorIpsPreferencePage_labelMaxHeapSizeIpsTestRunner;

    public static String LinkDropListener_selectAssociation;

}
