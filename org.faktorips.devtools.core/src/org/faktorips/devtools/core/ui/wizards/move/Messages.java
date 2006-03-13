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

package org.faktorips.devtools.core.ui.wizards.move;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Thorsten Guenther
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.move.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String MovePage_title;

	public static String MovePage_description;

	public static String MovePage_targetLabel;

	public static String MovePage_infoPackageWillBeCreated;

	public static String MoveWizard_titleMove;

	public static String MoveWizard_titleRename;

	public static String MoveWizard_warnInvalidOperation;

	public static String MoveWizard_errorUnsupported;

	public static String MoveWizard_errorToManySelected;

	public static String MoveWizard_error;

	public static String ErrorPage_error;

	public static String RenamePage_rename;

	public static String RenamePage_msgChooseNewName;

	public static String RenamePage_newName;

	public static String RenamePage_errorNameIsEmpty;

	public static String RenamePage_errorNameQualified;

	public static String errorNameNotValid;

	public static String RenamePage_warningDiscouraged;

	public static String RenamePage_errorFileExists;

	public static String RenamePage_errorFolderExists;

	public static String MovePage_labelDefaultPackage;
}
