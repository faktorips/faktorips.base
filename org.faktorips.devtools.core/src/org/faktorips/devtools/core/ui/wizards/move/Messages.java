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
}
