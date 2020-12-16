/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.ipspackage;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.ipspackage.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String IpsPackagePage_msgNew;
    public static String IpsPackagePage_labelSrcFolder;
    public static String IpsPackagePage_labelName;
    public static String IpsPackagePage_msgRootMissing;
    public static String IpsPackagePage_msgRootNoIPSSrcFolder;
    public static String IpsPackagePage_msgPackageMissing;
    public static String IpsPackagePage_msgEmptyName;
    public static String IpsPackagePage_msgRootRequired;
    public static String IpsPackagePage_msgSelectSourceFolder;
    public static String IpsPackagePage_title;
    public static String IpsPackagePage_PackageNameMustNotContainBlanks;
    public static String IpsPackagePage_InvalidPackageName;
    public static String IpsPackagePage_PackageAllreadyExists;
    public static String IpsPackagePage_packagePath;

}
