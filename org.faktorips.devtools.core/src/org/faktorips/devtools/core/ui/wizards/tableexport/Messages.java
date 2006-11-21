/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
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

	public static String TableExport_title;
	public static String TableExportPage_title;
	public static String TableExportPage_labelContents;
    public static String TableExportPage_labelSrcFolder;
    public static String TableExportPage_labelPackage;
    public static String TableExportPage_labelName;
    public static String TableExportPage_msgRootMissing;
    public static String TableExportPage_msgRootNoIPSSrcFolder;
    public static String TableExportPage_msgPackageMissing;
    public static String TableExportPage_msgEmptyName;
    public static String TableExportPage_labelProject;
    public static String TableExportPage_msgInvalidProject;
    public static String TableExportPage_msgNonExistingProject;
    public static String TableExportPage_msgInvalidContents;
    public static String TableExportPage_msgNonExisitingContents;
    public static String TableExportPage_msgValidateContentsError;
    public static String TableExportWizard_msgFileExistsTitle;
    public static String TableExportWizard_msgFileExists;
    public static String TableExportPage_labelFileFormat;
    public static String TableExportPage_msgMissingFileFormat;
    public static String TableExportPage_labelNullRepresentation;
}
