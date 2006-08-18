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

import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.extsystems.IValueConverter;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;


/**
 * Converts from Long to String and vice versa.
 * 
 * @author Thorsten Guenther
 */
public class LongValueConverter implements IValueConverter {

	/**
	 * Supported types for externalDataValue are Double and Long
	 * 
	 * {@inheritDoc}
	 */
	public String getIpsValue(Object externalDataValue, MessageList messageList) {
		if (externalDataValue instanceof Long) {
		return ((Long)externalDataValue).toString();
		}
		else if (externalDataValue instanceof Double) {
			long value = ((Double)externalDataValue).longValue();
			Double restored = new Double(value); 
			if (!restored.equals((Double)externalDataValue)) {
				String msg = NLS.bind("Can not convert the value because information will be lost. Original value is \"{0}\", but converted value will be \"{1}\".", externalDataValue, restored);
				messageList.add(new Message("", msg, Message.ERROR));
				return externalDataValue.toString();
			}
			return new Long(value).toString();
		}
		String msg = NLS.bind("Can not convert the external value of type {0} to {1}", externalDataValue.getClass(), getSupportedDatatype().getQualifiedName());
		messageList.add(new Message("", msg, Message.ERROR));
		return externalDataValue.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getExternalDataValue(String ipsValue, MessageList messageList) {
		if (ipsValue == null) {
			return null;
		}
		if (ipsValue.length() == 0) {
			return new Long(0);
		}
		try {
			return Long.valueOf(ipsValue);
		} catch (NumberFormatException e) {
			Object[] objects = new Object[3];
			objects[0] = ipsValue;
			objects[1] = getSupportedDatatype().getQualifiedName();
			objects[2] = Long.class.getName(); 
			String msg = NLS.bind("Can not convert the internal value \"{0}\" of type {1} to {2}", objects);
			messageList.add(new Message("", msg, Message.ERROR));
		}
		return ipsValue;
	}


	/**
	 * {@inheritDoc}
	 */
	public Datatype getSupportedDatatype() {
		return Datatype.LONG;
	}
}
