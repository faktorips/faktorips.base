/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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

    public static String NamedDataTypeDisplay_id;
    public static String NamedDataTypeDisplay_name;
    public static String NamedDataTypeDisplay_nameAndId;

    public static String IpsClasspathContainerInitializer_containerDescription;
    public static String IpsPlugin_languagePackLanguage;
    public static String IpsPlugin_languagePackCountry;
    public static String IpsPlugin_languagePackVariant;

}
