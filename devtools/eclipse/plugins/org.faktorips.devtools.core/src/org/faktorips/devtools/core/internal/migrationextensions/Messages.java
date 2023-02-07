/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.migrationextensions;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.migrationextensions.messages"; //$NON-NLS-1$

    public static String Migration_21_6_0_description;
    public static String Migration_21_6_0_IpsVersionTooOld;
    public static String Migration_21_12_0_description;
    public static String Migration_21_12_0_AddSchemas;
    public static String Migration_22_6_0_description;
    public static String Migration_22_6_0_unifyValueSet;
    public static String Migration_Option_Unify_Value_Set_Description;
    public static String Migration_22_12_0_description;
    public static String Migration_23_6_0_description;
    public static String Migration_23_6_0_jaxbVariant;
    public static String Migration_Option_JAXB_Variant_Description;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
