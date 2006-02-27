package org.faktorips.devtools.core.ui;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Thorsten Guenther
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String NewFolderActionDelegate_titleNewFolder;

	public static String NewFolderActionDelegate_descriptionNewFolder;

	public static String NewFolderActionDelegate_valueNewFolder;

	public static String DatatypeCompletionProcessor_msgNoProjectContext;

	public static String DatatypeCompletionProcessor_msgInternalError;

	public static String PdPackageSelectionDialog_title;

	public static String PdPackageSelectionDialog_description;

	public static String FaktorIpsPreferencePage_labelWorkingDate;

	public static String FaktorIpsPreferencePage_labelNullValue;

	public static String FaktorIpsPreferencePage_labelProductTypePostfix;

	public static String FaktorIpsPreferencePage_labelNamingScheme;

	public static String PdObjectSelectionDialog_labelMatches;

	public static String PdObjectSelectionDialog_labelQualifier;
}
