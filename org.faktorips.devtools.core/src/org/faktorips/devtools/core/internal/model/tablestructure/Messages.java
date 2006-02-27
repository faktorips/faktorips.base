package org.faktorips.devtools.core.internal.model.tablestructure;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Thorsten Guenther
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.model.tablestructure.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String ColumnRange_msgMissingColumn;

	public static String TableStructure_descriptionStart;

	public static String TableStructure_descriptionEnd;

	public static String ForeignKey_msgMissingUniqueKey;

	public static String ForeignKey_6;

	public static String ForeignKey_7;

	public static String ForeignKey_msgMalformedForeignKey;

	public static String ForeignKey_msgInvalidKeyItem;

	public static String ForeignKey_12;

	public static String ForeignKey_msgKeyItemMissmatch;

	public static String ForeignKey_msgNotARange;

	public static String ForeignKey_17;

	public static String ForeignKey_msgInvalidRange;

	public static String ForeignKey_msgReferencedRangeInvalid;

	public static String ForeignKey_msgForeignKeyDatatypeMismatch;

	public static String ForeignKey_24;

	public static String ForeignKey_25;

	public static String ForeignKey_msgColumnDatatypeMismatch;

	public static String ForeignKey_28;

	public static String ForeignKey_29;

	public static String ForeignKey_msgKeyMissmatch;

	public static String ForeignKey_msgNotAColumn;

	public static String ForeignKey_34;

	public static String ForeignKey_msgKeyDatatypeMismatch;

	public static String ForeignKey_37;

	public static String ForeignKey_38;
}
