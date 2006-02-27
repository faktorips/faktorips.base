package org.faktorips.devtools.core.ui.wizards.productcmpt;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Thorsten Guenther
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.productcmpt.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String ProductCmptPage_title;

	public static String ProductCmptPage_labelName;

	public static String ProductCmptPage_msgPolicyClassMissing;

	public static String ProductCmptPage_3;
}
