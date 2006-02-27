package org.faktorips.devtools.core.ui.controls.spreadsheet;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * 
 * @author Thorsten Guenther
 */
public class Messages {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.controls.spreadsheet.messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	private Messages() {
	}

	public static String getString(String key) {
		// TODO Auto-generated method stub
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
