package org.faktorips.devtools.core.ui.search;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.search.messages"; //$NON-NLS-1$

    private Messages() {
    }

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
    public static String ReferencesToProductSearchQuery_ok;
    public static String ReferencesToProductSearchQuery_labelPrefix;
    public static String ReferencesToProductSearchResult_label;
}
