package org.faktorips.devtools.htmlexport.standard;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.htmlexport.standard.messages"; //$NON-NLS-1$
    public static String StandardDocumentorScript_documentation;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
