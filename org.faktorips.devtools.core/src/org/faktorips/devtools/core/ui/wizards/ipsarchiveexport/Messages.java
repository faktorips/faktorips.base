/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.ipsarchiveexport;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.ipsarchiveexport.messages"; //$NON-NLS-1$


    private Messages() {
    }

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
    
    public static String IpsArchivePackageWizardPage_Title;
    public static String IpsArchiveExportWizard_Export;
    public static String IpsArchivePackageWizardPage_Description_EnterDestination;
    public static String IpsArchivePackageWizardPage_Label_IncludeJavaSources;
    public static String IpsArchivePackageWizardPage_Label_IncludeJavaBinaries;
    public static String IpsArchivePackageWizardPage_Label_Target;
    public static String IpsArchivePackageWizardPage_Label_Browse;
    public static String IpsArchivePackageWizardPage_Description_EnterValidDestination;
    public static String IpsArchivePackageWizardPage_Description_DefineWhichResource;
    public static String IpsArchivePackageWizardPage_Description;

}
