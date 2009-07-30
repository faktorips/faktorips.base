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

package org.faktorips.devtools.core.ui.wizards.tableexport;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Thorsten Waertel
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.tableexport.messages"; //$NON-NLS-1$

    private Messages() {

    }

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    public static String IpsObjectExportPage_pageTitle;
    public static String TableExport_title;
    public static String TableExportPage_firstRowContainsHeader;
    public static String TableExportPage_title;
    public static String TableExportPage_labelContents;
    public static String TableExportPage_labelName;
    public static String TableExportPage_msgEmptyName;
    public static String TableExportPage_labelProject;
    public static String TableExportPage_msgNonExistingProject;
    public static String TableExportPage_msgProjectEmpty;
    public static String TableExportPage_msgContentsEmpty;
    public static String TableExportPage_msgNonExisitingContents;
    public static String TableExportWizard_msgFileExistsTitle;
    public static String TableExportWizard_msgFileExists;
    public static String TableExportPage_labelFileFormat;
    public static String TableExportPage_msgMissingFileFormat;
    public static String TableExportPage_labelNullRepresentation;
    public static String TableExportWizard_operationName;
    public static String TableExportPage_msgContentsNotValid;
    public static String TableExportPage_msgStructureNotValid;

}
