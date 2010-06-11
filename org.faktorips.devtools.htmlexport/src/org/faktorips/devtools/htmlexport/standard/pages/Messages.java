package org.faktorips.devtools.htmlexport.standard.pages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.htmlexport.standard.pages.messages"; //$NON-NLS-1$
    public static String ProjectOverviewPageElement_archiveEntries;
    public static String ProjectOverviewPageElement_created;
    public static String ProjectOverviewPageElement_noArchiveEntries;
    public static String ProjectOverviewPageElement_noReferencedProjects;
    public static String ProjectOverviewPageElement_noReferencingProjects;
    public static String ProjectOverviewPageElement_noSourceFolder;
    public static String ProjectOverviewPageElement_paths;
    public static String ProjectOverviewPageElement_project;
    public static String ProjectOverviewPageElement_referencedProjects;
    public static String ProjectOverviewPageElement_referencingProjects;
    public static String ProjectOverviewPageElement_sourceFolder;
    public static String ProjectOverviewPageElement_validationErros;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
