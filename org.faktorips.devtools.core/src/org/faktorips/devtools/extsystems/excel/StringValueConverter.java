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

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.extsystems.AbstractExternalTableFormat;
import org.faktorips.devtools.extsystems.IValueConverter;
import org.faktorips.util.message.MessageList;


/**
 * Converts from String to String :-)
 * 
 * @author Thorsten Guenther
 */
public class StringValueConverter implements IValueConverter {

	/**
	 * Every type is allowed for externalDataValue.
	 * 
	 * {@inheritDoc}
	 */
	public String getIpsValue(Object externalDataValue, MessageList messageList) {
        if (externalDataValue instanceof Double){
            return AbstractExternalTableFormat.doubleToStringWithoutDecimalPlaces((Double)externalDataValue);
        }
		return externalDataValue==null?null:externalDataValue.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getExternalDataValue(String ipsValue, MessageList messageList) {
		return ipsValue;
	}

	/**
	 * {@inheritDoc}
	 */
	public Datatype getSupportedDatatype() {
		return Datatype.STRING;
	}
}
