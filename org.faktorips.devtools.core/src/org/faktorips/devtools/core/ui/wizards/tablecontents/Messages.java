package org.faktorips.devtools.core.ui.wizards.tablecontents;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Thorsten Guenther
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.tablecontents.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String TableContentsPage_title;

	public static String TableContentsPage_labelStructure;

	public static String TableContentsPage_msgMissingStructure;

	public static String TableContentsPage_4;
}
