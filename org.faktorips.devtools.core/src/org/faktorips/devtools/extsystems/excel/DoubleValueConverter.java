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
import org.faktorips.devtools.extsystems.ExtSystemsMessageUtil;
import org.faktorips.devtools.extsystems.IValueConverter;
import org.faktorips.util.message.MessageList;

/**
 * Converts from Double to String and vice versa.
 * 
 * @author Thorsten Guenther
 */
public class DoubleValueConverter implements IValueConverter {

	/**
	 * Only supported type for externalDataValue is Double
	 * 
	 * {@inheritDoc}
	 */
	public String getIpsValue(Object externalDataValue, MessageList messageList) {
        if (externalDataValue instanceof Double) {
            // format double values without decimal places to int string representation
            // maybe an import from excel returns a string formated cells (with numeric value
            // inside)
            // as double (e.g. 1 will be 1.0)
            return AbstractExternalTableFormat.doubleToStringWithoutDecimalPlaces((Double)externalDataValue);
        }
        messageList.add(ExtSystemsMessageUtil.createConvertExtToIntErrorMessage(
                        "" + externalDataValue, externalDataValue.getClass().getName(), getSupportedDatatype().getQualifiedName())); //$NON-NLS-1$
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
			return new Double(0);
		}
		try {
			return Double.valueOf(ipsValue);
		} catch (NumberFormatException e) {
			messageList.add(ExtSystemsMessageUtil.createConvertIntToExtErrorMessage(
                    ipsValue, getSupportedDatatype().getQualifiedName(), Double.class.getName()));
		}
		return ipsValue;
	}

	/**
	 * {@inheritDoc}
	 */
	public Datatype getSupportedDatatype() {
		return Datatype.DOUBLE;
	}
}
