package org.faktorips.devtools.core.ui.views.productdefinitionexplorer;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.views.productdefinitionexplorer.messages"; //$NON-NLS-1$

    private Messages() {
    }

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
    public static String ProductContentProvider_tooMuchIpsPackageFragmentRoots;
    public static String ProductContentProvider_tooMuchIpsObjects;
	public static String ProductExplorer_submenuNew;
	public static String ProductExplorer_submenuRefactor;
	public static String ProductExplorer_submenuTeam;
	public static String ProductExplorer_actionCommit;
	public static String ProductExplorer_actionUpdate;
	public static String ProductExplorer_actionReplace;
	public static String ProductExplorer_actionAdd;
}
