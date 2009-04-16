/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.tableconversion.csv;

import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.lang.time.DateUtils;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.classtypes.DateDatatype;
import org.faktorips.devtools.tableconversion.AbstractValueConverter;
import org.faktorips.devtools.tableconversion.ExtSystemsMessageUtil;
import org.faktorips.util.message.MessageList;
import org.faktorips.values.DateUtil;

public class DateValueConverter extends AbstractValueConverter {

    private DateDatatype datatype = new DateDatatype();
    
    /**
     * {@inheritDoc}
     */
    public Object getExternalDataValue(String ipsValue, MessageList messageList) {
        if (ipsValue == null) {
            return null;
        }
        try {
            Date date = (Date) datatype.getValue(ipsValue);
            DateUtil.dateToIsoDateString(date);
            return date;
        } catch (RuntimeException e) {
            messageList.add(ExtSystemsMessageUtil.createConvertIntToExtErrorMessage(ipsValue, getSupportedDatatype()
                    .getQualifiedName(), GregorianCalendar.class.getName())); //$NON-NLS-1$
            return ipsValue;
        }
    }

    /**
     * The only supported type for externalDataValue is String.
     * 
     * {@inheritDoc}
     */
    public String getIpsValue(Object externalDataValue, MessageList messageList) {
        if (externalDataValue instanceof String) {
            try {
                return  DateUtil.parseIsoDateStringToDate((String)externalDataValue).toString();
            } catch (IllegalArgumentException ignored) {
                // could not convert, try again using a date format 
            }
            try {
                String dateFormat = tableFormat.getProperty(CSVTableFormat.PROPERTY_DATE_FORMAT).toLowerCase();
                Date parseDate = DateUtils.parseDate((String)externalDataValue, new String[] {dateFormat});
                return DateUtil.dateToIsoDateString(parseDate);
            } catch (Exception ignored) {
                // could not convert, so add error messages to MessageList and return unconverted value
            }
        }
        
        messageList.add(ExtSystemsMessageUtil.createConvertExtToIntErrorMessage("" + externalDataValue, externalDataValue //$NON-NLS-1$
                .getClass().getName(), getSupportedDatatype().getQualifiedName())); 
        return externalDataValue.toString();            
    }

    /**
     * {@inheritDoc}
     */
    public Datatype getSupportedDatatype() {
        return datatype;
    }

}
