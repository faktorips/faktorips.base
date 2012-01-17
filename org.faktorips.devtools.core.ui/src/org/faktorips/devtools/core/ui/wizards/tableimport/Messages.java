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

package org.faktorips.devtools.core.ui.wizards.tableimport;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.tableimport.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
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
    public static String SelectTableContentsPage_msgStructureNotValid;
    public static String SelectFileAndImportMethodPage_labelImportExisting;
    public static String SelectFileAndImportMethodPage_labelImportNew;
    public static String TableContentsPage_title;
    public static String TableContentsPage_labelStructure;
    public static String TableContentsPage_msgMissingStructure;
    public static String TableContentsPage_tableStructureHasntGotAnyColumns;
    public static String TableContentsPage_msgStructureEmpty;

}
