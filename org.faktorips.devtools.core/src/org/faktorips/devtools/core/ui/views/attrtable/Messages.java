package org.faktorips.devtools.core.ui.views.attrtable;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.views.attrtable.messages"; //$NON-NLS-1$

    private Messages() {
    }

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
    public static String AttributeLabelProvider_undefined;
    public static String AttributeLabelProvider_invalid;
}
