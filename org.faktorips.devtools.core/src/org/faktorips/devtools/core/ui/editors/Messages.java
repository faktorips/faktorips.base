package org.faktorips.devtools.core.ui.editors;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Thorsten Guenther
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.editors.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String IpsPartEditDialog_description;
	public static String IpsPartsComposite_buttonNew;
	public static String IpsPartsComposite_buttonEdit;
	public static String IpsPartsComposite_buttonDelete;
	public static String IpsPartsComposite_buttonUp;
	public static String IpsPartsComposite_buttonDown;
	public static String DescriptionPage_description;
	public static String DescriptionSection_description;

}
