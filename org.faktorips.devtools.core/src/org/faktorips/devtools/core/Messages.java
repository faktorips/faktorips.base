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

package org.faktorips.devtools.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String DatatypeFormatter_booleanFalse;
    public static String DatatypeFormatter_booleanTrue;

    public static String EnumTypeDisplay_id;
    public static String EnumTypeDisplay_name;
    public static String EnumTypeDisplay_nameAndId;

    public static String IpsClasspathContainerInitializer_containerDescription;
    public static String IpsPlugin_infoDefaultTextEditorWasOpened;
    public static String IpsPlugin_titleErrorDialog;
    public static String IpsPlugin_msgUnexpectedError;
    public static String IpsPlugin_languagePackLanguage;
    public static String IpsPlugin_languagePackCountry;
    public static String IpsPlugin_languagePackVariant;
    public static String IpsPlugin_errorNoDatatypeControlFactoryFound;
    public static String IpsPlugin_dialogSaveDirtyEditorMessageMany;
    public static String IpsPlugin_dialogSaveDirtyEditorMessageSimple;
    public static String IpsPlugin_dialogSaveDirtyEditorTitle;

}
