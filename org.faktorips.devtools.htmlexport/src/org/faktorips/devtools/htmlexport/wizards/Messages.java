/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.htmlexport.wizards;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.htmlexport.wizards.messages"; //$NON-NLS-1$
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
