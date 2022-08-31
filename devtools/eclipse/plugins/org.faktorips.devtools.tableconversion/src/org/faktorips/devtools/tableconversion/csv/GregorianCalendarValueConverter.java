/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.tableconversion.csv;

import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.classtypes.GregorianCalendarDatatype;
import org.faktorips.devtools.core.tableconversion.AbstractValueConverter;
import org.faktorips.devtools.core.tableconversion.ExtSystemsMessageUtil;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
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
            // CSOFF: IllegalCatch
        } catch (RuntimeException e) {
            // CSON: IllegalCatch
            messageList.add(new Message(ExtSystemsMessageUtil.createConvertIntToExtErrorMessage(ipsValue,
                    getSupportedDatatype().getQualifiedName(), GregorianCalendar.class.getName())));
            return ipsValue;
        }
    }

    /**
     * The only supported type for externalDataValue is String.
     */
    @Override
    public String getIpsValue(Object externalDataValue, MessageList messageList) {
        GregorianCalendar cal = new GregorianCalendar();
        GregorianCalendarDatatype datatype = (GregorianCalendarDatatype)getSupportedDatatype();
        if (externalDataValue instanceof String) {
            // CSOFF: EmptyBlockCheck
            try {
                cal = DateUtil.parseIsoDateStringToGregorianCalendar((String)externalDataValue);
                return datatype.valueToString(cal);
            } catch (IllegalArgumentException ignored) {
                // generic error message is created outside of this block
            }
            try {
                String dateFormat = tableFormat.getProperty(CSVTableFormat.PROPERTY_DATE_FORMAT);
                Date parseDate = DateUtils.parseDate((String)externalDataValue, dateFormat);
                return DateUtil.dateToIsoDateString(parseDate);
            } catch (ParseException ignored) {
                // generic error message is created outside of this block
            }
            // CSON: EmptyBlockCheck
        }

        messageList.add(ExtSystemsMessageUtil.createConvertExtToIntErrorMessage("" + externalDataValue, //$NON-NLS-1$
                externalDataValue.getClass().getName(), getSupportedDatatype().getQualifiedName()));
        return externalDataValue.toString();
    }

    @Override
    public Datatype getSupportedDatatype() {
        return Datatype.GREGORIAN_CALENDAR;
    }

}
