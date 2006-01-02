package org.faktorips.devtools.core.ui.contentassist;


import org.eclipse.osgi.util.NLS;

/**
 * Helper class to get NLSed messages.
 *
 * @since 3.0
 */
final class ContentAssistMessages extends NLS {

	private static final String BUNDLE_NAME= ContentAssistMessages.class.getName();

	private ContentAssistMessages() {
		// Do not instantiate
	}

	public static String ContentAssistHandler_contentAssistAvailable;
	public static String ContentAssistHandler_contentAssistAvailableWithKeyBinding;

	static {
		NLS.initializeMessages(BUNDLE_NAME, ContentAssistMessages.class);
	}
}