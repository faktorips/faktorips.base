package org.faktorips.devtools.core.ui.views.modeldescription;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.views.modeldescription.messages"; //$NON-NLS-1$

    private Messages() {
    }

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

    public static String DefaultModelDescriptionPage_NoDescriptionAvailable;
    public static String DefaultModelDescriptionPage_SortDescription;
    public static String DefaultModelDescriptionPage_SortText;
    public static String DefaultModelDescriptionPage_SortTooltipText;
    public static String ModelDescriptionView_notSupported;
}
