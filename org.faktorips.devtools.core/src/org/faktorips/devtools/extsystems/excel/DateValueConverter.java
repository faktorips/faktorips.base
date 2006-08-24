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

import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.classtypes.DateDatatype;
import org.faktorips.devtools.extsystems.IValueConverter;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Converter for the Date-Datatype.
 * 
 * @author Thorsten Guenther
 */
public class DateValueConverter implements IValueConverter {

    private DateDatatype datatype = new DateDatatype();
    
    /**
     * Supported type for the externalDataValue is Number or Date.
     * 
     * {@inheritDoc}
     */
    public String getIpsValue(Object externalDataValue, MessageList messageList) {
        Date date;
        if (externalDataValue instanceof Date) {
            date = (Date)externalDataValue;
        }
        else if (externalDataValue instanceof Number) {
            date = new Date(((Number)externalDataValue).longValue());
        }
        else {
            String msg = NLS.bind("Can not convert the external value of type {0} to {1}",
                    externalDataValue.getClass(), getSupportedDatatype().getQualifiedName());
            messageList.add(new Message("", msg, Message.ERROR));
            return externalDataValue.toString();
        }
        return datatype.valueToString(date);
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
        
        try {
            Date date = (Date)datatype.getValue(ipsValue);
            return date;
        }
        catch (RuntimeException e) {
            Object[] objects = new Object[3];
            objects[0] = ipsValue;
            objects[1] = getSupportedDatatype().getQualifiedName();
            objects[2] = GregorianCalendar.class.getName();
            String msg = NLS.bind("Can not convert the internal value \"{0}\" of type {1} to {2}", objects);
            messageList.add(new Message("", msg, Message.ERROR));
            return ipsValue;
        }
    }

    /**
     * {@inheritDoc}
     */
    public Datatype getSupportedDatatype() {
        return datatype;
    }

}
