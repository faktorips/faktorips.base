/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.tablestructure;

import org.faktorips.devtools.abstraction.util.IpsNLS;

public class Messages extends IpsNLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.model.internal.tablestructure.messages"; //$NON-NLS-1$

    static {
        IpsNLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String ColumnRange_msgMissingColumn;

    public static String ForeignKey_msgMissingUniqueKey;
    public static String ForeignKey_msgMalformedForeignKey;
    public static String ForeignKey_msgInvalidKeyItem;
    public static String ForeignKey_msgKeyItemMissmatch;
    public static String ForeignKey_msgNotARange;
    public static String ForeignKey_msgInvalidRange;
    public static String ForeignKey_msgReferencedRangeInvalid;
    public static String ForeignKey_msgForeignKeyDatatypeMismatch;
    public static String ForeignKey_msgKeyMissmatch;
    public static String ForeignKey_msgNotAColumn;
    public static String ForeignKey_msgKeyDatatypeMismatch;

    public static String Index_msgTooLessItems;
    public static String Index_msgKeyItemMismatch;
    public static String Index_wrong_sequence;

    public static String ColumnRange_msgParameterEmpty;
    public static String Column_msgPrimitvesArentSupported;

    public static String TableStructureType_labelEnumTypeModel;

    public static String ColumnRange_msgNameInvalidJavaIdentifier;
    public static String ColumnRange_msgTwoColumnRangeFromToColumnWithDifferentDatatype;

    public static String Column_msgInvalidName;

    public static String TableAccessFunctionDescription;

}
