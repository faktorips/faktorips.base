package org.faktorips.devtools.core.ui.wizards.deepcopy;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.views.productstructureexplorer.messages"; //$NON-NLS-1$

    private Messages() {
    }

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
    public static String ProductStructureLabelProvider_undefined;
    public static String DeepCopyWizard_title;
    public static String SourcePage_title;
    public static String SourcePage_pageTitle;
    public static String SourcePage_description;
    public static String SourcePage_msgSelect;
	public static String ReferenceAndPreviewPage_title;
	public static String ReferenceAndPreviewPage_pageTitle;
	public static String ReferenceAndPreviewPage_description;
	public static String ReferenceAndPreviewPage_labelValidFrom;
	public static String ReferenceAndPreviewPage_labelTargetPackage;
	public static String ReferenceAndPreviewPage_labelSearchPattern;
	public static String ReferenceAndPreviewPage_labelReplacePattern;
	public static String ReferenceAndPreviewPage_msgCopyNotPossible;
	public static String ReferenceAndPreviewPage_msgCanNotCreateFile;
	public static String ReferenceAndPreviewPage_msgFileAllreadyExists;
	public static String ReferenceAndPreviewPage_msgNameCollision;
	public static String ReferenceAndPreviewPage_errorLabelInsert;
}
