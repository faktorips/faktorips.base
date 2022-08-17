/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.enumcontent;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.enumcontent.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String EnumContentPage_msgEnumContentAlreadyExists;
    public static String EnumContentPage_msgEnumContentExistsForNameExistsAlready;
    public static String EnumContentPage_msgEnumContentNameOfEnumTypeMissing;
    public static String EnumContentPage_msgEnumTypeMissing;
    public static String EnumContentPage_msgEnumTypeNotExisting;
    public static String Page_Title;
    public static String Fields_EnumType;

    public static String CreateMissingEnumContentsWizard_title;
    public static String CreateMissingEnumContentsWizard_labelOperation;
    public static String SelectEnumContentsPage_title;
    public static String SelectEnumContentsPage_prompt;
    public static String SelectEnumContentsPage_labelTargetSourceFolder;
    public static String SelectEnumContentsPage_buttonSelectAll;
    public static String SelectEnumContentsPage_buttonDeselectAll;
    public static String SelectEnumContentsPage_msgTargetSourceFolderNotSpecified;
    public static String SelectEnumContentsPage_msgTargetSourceFolderDoesNotExist;

}
