package org.faktorips.devtools.htmlexport.actions;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.htmlexport.actions.messages"; //$NON-NLS-1$
	public static String CreateHtmlExportAction_Export;
	public static String CreateHtmlExportAction_HtmlExport;
	public static String CreateHtmlExportAction_SelectJustOneProject;
	public static String CreateHtmlExportAction_SelectOneProject;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
