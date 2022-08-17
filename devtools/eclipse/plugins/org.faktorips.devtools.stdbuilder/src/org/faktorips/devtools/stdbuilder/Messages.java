/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.stdbuilder.messages"; //$NON-NLS-1$

    public static String AbstractBaseClassBuilderSetPropertyDef_CantLoadJavaClass;
    public static String AbstractBaseClassBuilderSetPropertyDef_NotSubclass;

    public static String ChangesOverTimeNamingConventionPropertyDef_msgDerivedPropertySetManually;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // prohibit instantiation
    }

    public static String bind(String message, String... bindings) {
        return NLS.bind(message, bindings);
    }
}
