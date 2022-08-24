/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.valueset;

import org.faktorips.devtools.abstraction.util.IpsNLS;

public class Messages extends IpsNLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.model.internal.valueset.messages"; //$NON-NLS-1$

    static {
        IpsNLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String ValueSet_msgNullNotSupported;

    public static String EnumValueSet_abstract;

    public static String EnumValueSet_msgValueNotParsable;
    public static String EnumValueSet_msgDuplicateValue;
    public static String EnumValueSet_msgDatatypeUnknown;

    public static String Range_msgUnknownDatatype;
    public static String Range_msgLowerboundGreaterUpperbound;
    public static String Range_msgPropertyValueNotParsable;

    public static String StringLength_msgNegativeValue;
    public static String StringLength_canonicalDesc;
    public static String StringLength_unlimitedLength;

    public static String ValueSet_includingNull;
    public static String ValueSet_excludingNull;

    public static String RangeValueSet_msgStepRangeMismatch;
    public static String RangeValueSet_msgStepWithLowerNull;
    public static String RangeValueSet_msgDatatypeNotNumeric;
    public static String RangeValueSet_unlimited;

}
