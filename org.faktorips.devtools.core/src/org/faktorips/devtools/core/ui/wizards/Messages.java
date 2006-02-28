package org.faktorips.devtools.core.ui.wizards;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.messages"; //$NON-NLS-1$

    private Messages() {
    }

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
    public static String ProductStructureLabelProvider_undefined;
    public static String NewIpsObjectWizard_title;
    public static String IpsObjectPage_msgNew; 
    public static String IpsObjectPage_labelSrcFolder;
    public static String IpsObjectPage_labelPackage;
    public static String IpsObjectPage_labelName;
    public static String IpsObjectPage_msgRootMissing;
    public static String IpsObjectPage_msgRootNoIPSSrcFolder;
    public static String IpsObjectPage_msgPackageMissing;
    public static String IpsObjectPage_msgEmptyName;
    public static String IpsObjectPage_msgNameMustNotBeQualified;
    public static String IpsObjectPage_msgInvalidName;
    public static String IpsObjectPage_msgNameDiscouraged;
    public static String IpsObjectPage_msgObjectAllreadyExists;
}
