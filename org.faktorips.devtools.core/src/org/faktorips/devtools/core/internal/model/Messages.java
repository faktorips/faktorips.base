package org.faktorips.devtools.core.internal.model;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Thorsten Guenther
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.model.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String ValidationUtils_msgObjectDoesNotExist;

	public static String ValidationUtils_msgDatatypeDoesNotExist;

	public static String ValidationUtils_msgVoidNotAllowed;

	public static String ValidationUtils_msgPropertyMissing;
}
