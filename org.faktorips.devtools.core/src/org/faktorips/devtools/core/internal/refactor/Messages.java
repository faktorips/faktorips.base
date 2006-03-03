package org.faktorips.devtools.core.internal.refactor;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Thorsten Guenther
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.refactor.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String MoveOperation_titleAborted;

	public static String MoveOperation_msgAborted;

	public static String MoveOperation_msgFileExists;

	public static String MoveOperation_msgPackageExists;

	public static String MoveOperation_msgSourceMissing;

	public static String MoveOperation_msgSourceModified;

	public static String MoveOperation_msgPackageMissing;

	public static String MoveOperation_msgUnsupportedType;

	public static String MoveOperation_msgMoveBetweenProjectsNotSupported;
}
