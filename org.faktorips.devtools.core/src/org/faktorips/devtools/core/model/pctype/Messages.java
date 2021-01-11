/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.pctype;

import org.eclipse.osgi.util.NLS;

public class Messages {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.model.pctype.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String AttributeType_changeable;
    public static String AttributeType_derived_by_explicit_method_call;
    public static String AttributeType_derived_on_the_fly;
    public static String AttributeType_constant;

}
