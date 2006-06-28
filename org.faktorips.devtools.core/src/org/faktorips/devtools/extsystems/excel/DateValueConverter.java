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

package org.faktorips.devtools.extsystems.excel;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.faktorips.devtools.extsystems.ValueConverter;

/**
 * 
 * @author Thorsten Waertel
 */
public class DateValueConverter extends ValueConverter {
	
	private final static DateFormat format = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$

	/**
	 * {@inheritDoc}
	 */
	public String getIpsValue(Object externalDataValue) {
		return format.format((Date) externalDataValue);
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getExternalDataValue(String ipsValue) {
		try {
			return format.parse(ipsValue);
		} catch (ParseException e) {
			return null;
		}
	}

}
