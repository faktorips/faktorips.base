/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.tablestructure;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.model.tablestructure.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String ColumnRange_msgMissingColumn;

    public static String TableStructure_msgMoreThanOneKeyNotAdvisableInFormulas;

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

    public static String UniqueKey_msgTooLessItems;
    public static String UniqueKey_msgKeyItemMismatch;
    public static String UniqueKey_wrong_sequence;

    public static String ColumnRange_msgParameterEmpty;
    public static String Column_msgPrimitvesArentSupported;

    public static String TableStructureType_labelSingleContent;
    public static String TableStructureType_lableMultipleContents;
    public static String TableStructureType_labelEnumTypeModel;

    public static String ColumnRange_msgDatatypeInvalidForRange;
    public static String ColumnRange_msgNameInvalidJavaIdentifier;
    public static String ColumnRange_msgTwoColumnRangeFromToColumnWithDifferentDatatype;

    public static String Column_msgInvalidName;

}
