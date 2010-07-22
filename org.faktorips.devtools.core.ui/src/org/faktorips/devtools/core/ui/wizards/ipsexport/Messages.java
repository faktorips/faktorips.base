/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.ipsexport;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.ipsexport.messages"; //$NON-NLS-1$

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String IpsObjectExportPage_msgFileAlreadyExists;
    public static String IpsObjectExportPage_msgFilenameIsDirectory;
    public static String IpsObjectExportPage_pageTitle;
    public static String IpsObjectExportPage_firstRowContainsHeader;
    public static String IpsObjectExportPage_labelFileFormat;
    public static String IpsObjectExportPage_labelName;
    public static String IpsObjectExportPage_labelNullRepresentation;
    public static String IpsObjectExportPage_labelProject;
    public static String IpsObjectExportPage_msgEmptyName;
    public static String IpsObjectExportPage_msgMissingFileFormat;
    public static String IpsObjectExportPage_msgNonExistingProject;
    public static String IpsObjectExportPage_msgProjectEmpty;
    public static String TableFormatPropertiesPage_configGroupLabel;
    public static String TableFormatPropertiesPage_title;

}
