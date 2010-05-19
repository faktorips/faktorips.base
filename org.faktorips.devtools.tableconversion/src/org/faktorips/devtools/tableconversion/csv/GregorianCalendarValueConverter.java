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

import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.classtypes.GregorianCalendarDatatype;
import org.faktorips.devtools.tableconversion.AbstractValueConverter;
import org.faktorips.devtools.tableconversion.ExtSystemsMessageUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.values.DateUtil;

public class GregorianCalendarValueConverter extends AbstractValueConverter {

    @Override
    public Object getExternalDataValue(String ipsValue, MessageList messageList) {
        if (ipsValue == null) {
            return null;
        }
        GregorianCalendarDatatype datatype = (GregorianCalendarDatatype)getSupportedDatatype();
        try {
            GregorianCalendar cal = (GregorianCalendar)datatype.getValue(ipsValue);
            Date date = cal.getTime();

            String datePattern = tableFormat.getProperty(CSVTableFormat.PROPERTY_DATE_FORMAT);
            return DateFormatUtils.format(date, datePattern);
        } catch (RuntimeException e) {
            messageList.add(new Message(ExtSystemsMessageUtil.createConvertIntToExtErrorMessage(ipsValue,
                    getSupportedDatatype().getQualifiedName(), GregorianCalendar.class.getName())));
            return ipsValue;
        }
    }

    /**
     * The only supported type for externalDataValue is String.
     * 
     * {@inheritDoc}
     */
    @Override
    public String getIpsValue(Object externalDataValue, MessageList messageList) {
        GregorianCalendar cal = new GregorianCalendar();
        GregorianCalendarDatatype datatype = (GregorianCalendarDatatype)getSupportedDatatype();
        if (externalDataValue instanceof String) {
            try {
                cal = DateUtil.parseIsoDateStringToGregorianCalendar((String)externalDataValue);
                return datatype.valueToString(cal);
            } catch (IllegalArgumentException ignored) {
            }
            try {
                String dateFormat = tableFormat.getProperty(CSVTableFormat.PROPERTY_DATE_FORMAT);
                Date parseDate = DateUtils.parseDate((String)externalDataValue, new String[] { dateFormat });
                return DateUtil.dateToIsoDateString(parseDate);
            } catch (ParseException ignored) {
            }
        }

        messageList
                .add(ExtSystemsMessageUtil
                        .createConvertExtToIntErrorMessage(
                                "" + externalDataValue, externalDataValue.getClass().getName(), getSupportedDatatype().getQualifiedName())); //$NON-NLS-1$
        return externalDataValue.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Datatype getSupportedDatatype() {
        return Datatype.GREGORIAN_CALENDAR;
    }

}
