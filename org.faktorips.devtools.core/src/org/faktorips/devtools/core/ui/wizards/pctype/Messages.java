package org.faktorips.devtools.core.ui.wizards.pctype;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Thorsten Guenther
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.pctype.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String PcTypePage_title;

	public static String PcTypePage_labelSuperclass;

	public static String PcTypePage_labelOption;

	public static String PcTypePage_labelOverride;
}
