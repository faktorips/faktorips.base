/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.valueset;

import org.faktorips.devtools.abstraction.util.IpsNLS;

public class Messages extends IpsNLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.model.valueset.messages"; //$NON-NLS-1$

    static {
        IpsNLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String ValueSet_unrestrictedWithoutNull;
    public static String ValueSetType__allValues;
    public static String ValueSetType_range;
    public static String ValueSetType_enumeration;
    public static String ValueSetType_derived;
    public static String ValueSetType_stringLength;
    public static String ValueSetFormat_unrestricted;
    public static String ValueSetFormat_derived;
    public static String DerivedValueSet_MsgCantSetContainsNull;

}
