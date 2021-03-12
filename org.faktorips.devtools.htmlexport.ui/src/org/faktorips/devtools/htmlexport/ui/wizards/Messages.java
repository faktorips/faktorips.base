/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.ui.wizards;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.htmlexport.ui.wizards.messages"; //$NON-NLS-1$
    public static String HtmlExportWizardPage_inheritedObjectPartsWithinTable;
    public static String HtmlExportWizard_errorHtmlExport;
    public static String HtmlExportWizard_messageErrorHtmlExport;
    public static String HtmlExportWizard_messageWarningHtmlExport;
    public static String HtmlExportWizard_warningHtmlExport;
    public static String HtmlExportWizard_windowTitle;
    public static String HtmlExportWizardPage_browse;
    public static String HtmlExportWizardPage_description;
    public static String HtmlExportWizardPage_destination;
    public static String HtmlExportWizardPage_directoryDialogText;
    public static String HtmlExportWizardPage_htmlExport;
    public static String HtmlExportWizardPage_noProjectSelected;
    public static String HtmlExportWizardPage_objectTypes;
    public static String HtmlExportWizardPage_policy;
    public static String HtmlExportWizardPage_product;
    public static String HtmlExportWizardPage_project;
    public static String HtmlExportWizardPage_projectName;
    public static String HtmlExportWizardPage_showValidationErrors;
    public static String HtmlExportWizardPage_supportedLanguage;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        //
    }
}
