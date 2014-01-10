/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.enumimport;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.enumimport.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String EnumImportWizard_operationName;
    public static String EnumImportWizard_title;
    public static String SelectEnumPage_locationLabel;
    public static String SelectEnumPage_msgMissingContent;
    public static String SelectEnumPage_msgEnumEmpty;
    public static String SelectEnumPage_msgEnumNotValid;
    public static String SelectEnumPage_msgAbstractEnumType;
    public static String SelectEnumPage_msgEnumTypeNotContainingValues;
    public static String SelectEnumPage_msgEnumTypeNotValid;
    public static String SelectEnumPage_targetTypeLabel;
    public static String SelectEnumPage_title;
    public static String SelectFileAndImportMethodPage_labelImportExisting;
    public static String SelectFileAndImportMethodPage_labelImportNew;

}
