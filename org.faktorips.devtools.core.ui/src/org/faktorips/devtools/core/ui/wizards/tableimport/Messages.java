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

package org.faktorips.devtools.core.ui.wizards.tableimport;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Thorsten Waertel
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.tableimport.messages"; //$NON-NLS-1$

    private Messages() {

    }

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    public static String TableImport_title;
    public static String TableImportWizard_operationName;
    public static String SelectTableContentsPage_title;
    public static String SelectTableContentsPage_labelContents;
    public static String SelectTableContentsPage_msgMissingContent;
    public static String SelectTableContentsPage_labelProject;
    public static String SelectTableContentsPage_msgProjectEmpty;
    public static String SelectTableContentsPage_msgNonExistingProject;
    public static String SelectTableContentsPage_msgContentsEmpty;
    public static String SelectTableContentsPage_msgContentsNotValid;
    public static String SelectTableContentsPage_msgStructureNotValid;
    public static String SelectFileAndImportMethodPage_labelImportExisting;
    public static String SelectFileAndImportMethodPage_labelImportNew;
}
