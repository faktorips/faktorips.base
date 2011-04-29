/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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
    public static String IpsProjectHtmlExportWizard_errorHtmlExport;
    public static String IpsProjectHtmlExportWizard_messageErrorHtmlExport;
    public static String IpsProjectHtmlExportWizard_messageWarningHtmlExport;
    public static String IpsProjectHtmlExportWizard_warningHtmlExport;
    public static String IpsProjectHtmlExportWizard_windowTitle;
    public static String IpsProjectHtmlExportWizardPage_browse;
    public static String IpsProjectHtmlExportWizardPage_description;
    public static String IpsProjectHtmlExportWizardPage_destination;
    public static String IpsProjectHtmlExportWizardPage_directoryDialogText;
    public static String IpsProjectHtmlExportWizardPage_htmlExport;
    public static String IpsProjectHtmlExportWizardPage_noProjectSelected;
    public static String IpsProjectHtmlExportWizardPage_objectTypes;
    public static String IpsProjectHtmlExportWizardPage_policy;
    public static String IpsProjectHtmlExportWizardPage_product;
    public static String IpsProjectHtmlExportWizardPage_project;
    public static String IpsProjectHtmlExportWizardPage_projectName;
    public static String IpsProjectHtmlExportWizardPage_showValidationErrors;
    public static String IpsProjectHtmlExportWizardPage_supportedLanguage;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        //
    }
}
