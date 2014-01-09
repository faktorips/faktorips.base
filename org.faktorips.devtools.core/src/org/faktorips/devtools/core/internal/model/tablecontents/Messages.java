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

package org.faktorips.devtools.core.internal.model.tablecontents;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.model.tablecontents.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String Row_FromValueGreaterThanToValue;
    public static String Row_MissingValueForUniqueKey;
    public static String Row_NameMustBeValidJavaIdentifier;
    public static String Row_ValueNotParsable;

    public static String TableContents_msgMissingTablestructure;
    public static String TableContents_msgColumncountMismatch;
    public static String TableContents_msgNameStructureAndContentsNotSameWhenEnum;
    public static String TableContentsGeneration_dublicateEnumId;

    public static String UniqueKeyValidator_msgUniqueKeyViolation;
    public static String UniqueKeyValidatorRange_msgToManyUniqueKeyViolations;

}
