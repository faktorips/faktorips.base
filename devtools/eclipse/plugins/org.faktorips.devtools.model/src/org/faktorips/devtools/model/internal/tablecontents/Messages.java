/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.tablecontents;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.model.internal.tablecontents.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String Row_caption;
    public static String Row_FromValueGreaterThanToValue;
    public static String Row_MissingValueForUniqueKey;
    public static String Row_NameMustBeValidJavaIdentifier;
    public static String Row_ValueNotParsable;
    public static String Row_NumberOfValuesIsInvalid;

    public static String TableContents_msgMissingTablestructure;
    public static String TableContents_msgColumncountMismatch;
    public static String TableContents_msgNameStructureAndContentsNotSameWhenEnum;
    public static String TableContents_msgTooManyContentsForSingleTableStructure;
    public static String TableContentsGeneration_dublicateEnumId;
    public static String TableContents_ReferencedColumnOrderingInvalid;
    public static String TableContents_ReferencedColumnCountInvalid;
    public static String TableContents_ReferencedColumnNamesInvalid;
    public static String TableContents_NumberOfColumnsInvalid;

    public static String UniqueKeyValidator_msgUniqueKeyViolation;
    public static String UniqueKeyValidatorRange_msgTooManyUniqueKeyViolations;

}
