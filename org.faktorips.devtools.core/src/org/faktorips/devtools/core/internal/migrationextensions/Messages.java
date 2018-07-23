/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
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
    public static String Migration_3_7_0_description;
    public static String Migration_3_8_0_description;
    public static String Migration_3_9_0_description;
    public static String Migration_3_11_0_description;
    public static String Migration_3_12_0_description;
    public static String Migration_3_13_0_description;
    public static String Migration_3_14_0_description;
    public static String Migration_3_15_0_description;
    public static String Migration_3_16_0_description;
    public static String Migration_3_18_0_description;
    public static String Migration_3_19_0_description;
    public static String Migration_3_20_0_description;
    public static String Migration_3_21_0_description;
    public static String Migration_3_22_0_description;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
