package org.faktorips.devtools.core.ui.actions;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.actions.messages"; //$NON-NLS-1$

    private Messages() {
    }

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
    public static String FindReferenceAction_description;
    public static String FindReferenceAction_name;
    public static String ShowAttributesAction_description;
    public static String ShowAttributesAction_name;
    public static String ShowStructureAction_description;
    public static String ShowStructureAction_name;
    public static String OpenEditorAction_name;
    public static String OpenEditorAction_description;
    public static String OpenEditorAction_tooltip;
}
