/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.product;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * A naming strategy for product components that uses dates as version ids.
 * The next version is determined by the current working date.
 * 
 * @author Jan Ortmann
 */
public class DateBasedProductCmptNamingStrategy extends
		AbstractProductCmptNamingStrategy {

    private DateFormat dateFormat;
    private int numOfCharsInDate;
    private boolean allowAppendix;
	
	public DateBasedProductCmptNamingStrategy(
			String separator,
			String dateFormatPattern,
			boolean allowAppendix) {

		this(separator, new SimpleDateFormat(dateFormatPattern, Locale.getDefault()), allowAppendix, dateFormatPattern.length());
	}

	public DateBasedProductCmptNamingStrategy(
			String separator,
			DateFormat dateFormat,
			boolean allowAppendix,
			int numOfCharsInDate) {
		super("DateBasedNamingStrategy", separator);
		ArgumentCheck.notNull(dateFormat);
		this.dateFormat = dateFormat;
		this.allowAppendix = allowAppendix;
		this.numOfCharsInDate = numOfCharsInDate;
	}

	/**
	 * {@inheritDoc}
	 */
	public MessageList validateVersionId(String versionId) {
		if (allowAppendix) {
			versionId = versionId.substring(0, numOfCharsInDate);
		}
		MessageList list = new MessageList();
		try {
			dateFormat.parse(versionId);
			if (versionId.length()!=numOfCharsInDate) {
				throw new RuntimeException();
			}
		} catch (Exception e) {
			list.add(Message.newError(MSGCODE_ILLEGAL_VERSION_ID, "The version identification has the wrong format!"));
		}
		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getNextVersionId(IProductCmpt pc) {
		GregorianCalendar date = IpsPreferences.getWorkingDate();
		return dateFormat.format(date.getTime());
	}
	
}
