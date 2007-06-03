/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) dürfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1
 * (vor Gründung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation
 * 
 **************************************************************************************************/

package org.faktorips.devtools.extsystems.excel;

import java.util.Date;
import java.util.GregorianCalendar;

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.classtypes.GregorianCalendarDatatype;
import org.faktorips.devtools.extsystems.ExtSystemsMessageUtil;
import org.faktorips.devtools.extsystems.IValueConverter;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Converter for the GregorianCalendarDatatype.
 * 
 * @author Thorsten Guenther
 */
public class GregorianCalendarValueConverter implements IValueConverter {

    /**
     * Supported type for the externalDataValue is Number or Date.
     * 
     * {@inheritDoc}
     */
    public String getIpsValue(Object externalDataValue, MessageList messageList) {
        GregorianCalendarDatatype datatype = (GregorianCalendarDatatype)getSupportedDatatype();
        GregorianCalendar cal = new GregorianCalendar();
        if (externalDataValue instanceof Date) {
            cal.setTime((Date)externalDataValue);
        } else if (externalDataValue instanceof Number) {
            Date date = new Date(((Number)externalDataValue).longValue());
            cal.setTime(date);
        } else {
            messageList.add(ExtSystemsMessageUtil.createConvertExtToIntErrorMessage(
                                    "" + externalDataValue, externalDataValue.getClass().getName(), getSupportedDatatype().getQualifiedName())); //$NON-NLS-1$
            return externalDataValue.toString();
        }
        return datatype.valueToString(cal);
    }

    /**
     * Returns a <code>java.util.Date</code> if successfully converted, the untouched ipsValue if
     * not and <code>null</code> if the given ipsValue is <code>null</code>.
     * 
     * {@inheritDoc}
     */
    public Object getExternalDataValue(String ipsValue, MessageList messageList) {
        if (ipsValue == null) {
            return null;
        }
        
        GregorianCalendarDatatype datatype = (GregorianCalendarDatatype)getSupportedDatatype();
        try {
            GregorianCalendar cal = (GregorianCalendar)datatype.getValue(ipsValue);
            return cal.getTime();
        } catch (RuntimeException e) {
            messageList.add(new Message(ExtSystemsMessageUtil.createConvertIntToExtErrorMessage(
                    ipsValue, getSupportedDatatype().getQualifiedName(), GregorianCalendar.class.getName()))); //$NON-NLS-1$
            return ipsValue;
        }
    }

    /**
     * {@inheritDoc}
     */
    public Datatype getSupportedDatatype() {
        return Datatype.GREGORIAN_CALENDAR_DATE;
    }

}
