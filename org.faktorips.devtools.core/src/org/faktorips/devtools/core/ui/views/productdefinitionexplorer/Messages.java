package org.faktorips.devtools.core.ui.views.productdefinitionexplorer;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.views.productexplorer.messages"; //$NON-NLS-1$

    private Messages() {
    }

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
    public static String ProductContentProvider_tooMuchIpsPackageFragmentRoots;
    public static String ProductContentProvider_tooMuchIpsObjects;
}
