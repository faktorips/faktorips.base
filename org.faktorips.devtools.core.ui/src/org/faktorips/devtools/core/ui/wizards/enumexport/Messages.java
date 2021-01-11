/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.enumexport;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.enumexport.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String EnumExportPage_enum_label;
    public static String EnumExportPage_messagearea_title;
    public static String EnumExportPage_msgAbstractEnumType;
    public static String EnumExportPage_msgEnumTypeNotContainingValues;
    public static String EnumExportPage_msgNonExistingEnum;
    public static String EnumExportPage_msgNonExistingEnumType;
    public static String EnumExportPage_msgEnumHasTooManyColumns;
    public static String EnumExportPage_title;
    public static String EnumExportWizard_msgFileExists;
    public static String EnumExportWizard_msgFileExistsTitle;
    public static String EnumExportWizard_operationName;
    public static String EnumExportWizard_title;
    public static String EnumExportPage_msgEnumEmpty;
    public static String EnumExportPage_msgEnumNotValid;
    public static String EnumExportPage_msgEnumTypeNotValid;

}
