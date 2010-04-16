package org.faktorips.devtools.htmlexport.helper;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.htmlexport.helper.messages"; //$NON-NLS-1$
	public static String DocumentorUtil_defaultPackageName;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
