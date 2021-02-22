/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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

    public static String IpsObjectExportPage_msgDuplicateQualifiedName;
    public static String IpsObjectExportPage_msgFileAlreadyExists;
    public static String IpsObjectExportPage_msgFilenameIsDirectory;
    public static String IpsObjectExportPage_msgMissingFileExtension;
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
