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
import org.faktorips.datatype.classtypes.DateDatatype;
import org.faktorips.devtools.core.tableconversion.AbstractValueConverter;
import org.faktorips.devtools.core.tableconversion.ExtSystemsMessageUtil;
import org.faktorips.runtime.MessageList;
import org.faktorips.values.DateUtil;

public class DateValueConverter extends AbstractValueConverter {

    private DateDatatype datatype = new DateDatatype();

    @Override
    public Object getExternalDataValue(String ipsValue, MessageList messageList) {
        if (ipsValue == null) {
            return null;
        }
        try {
            Date date = (Date)datatype.getValue(ipsValue);
            if (tableFormat == null) {
                return DateUtil.dateToIsoDateString(date);
            }
            String datePattern = tableFormat.getProperty(CSVTableFormat.PROPERTY_DATE_FORMAT);
            return DateFormatUtils.format(date, datePattern);
            // CSOFF: Illegal Catch
        } catch (RuntimeException e) {
            messageList.add(ExtSystemsMessageUtil.createConvertIntToExtErrorMessage(ipsValue, getSupportedDatatype()
                    .getQualifiedName(), GregorianCalendar.class.getName()));
            return ipsValue;
        }
        // CSON: Illegal Catch
    }

    /**
     * The supported type for externalDataValue is String or java.util.Date.
     */
    @Override
    public String getIpsValue(Object externalDataValue, MessageList messageList) {
        if (externalDataValue instanceof Date) {
            return DateUtil.dateToIsoDateString((Date)externalDataValue);
        }
        if (externalDataValue instanceof String) {
            try {
                String dateFormat = tableFormat.getProperty(CSVTableFormat.PROPERTY_DATE_FORMAT);
                Date parseDate = DateUtils.parseDate((String)externalDataValue, new String[] { dateFormat });
                return DateUtil.dateToIsoDateString(parseDate);
                // CSOFF: Empty Statement
            } catch (ParseException ignored) {
                // could not convert, so add error messages to MessageList and return unconverted
                // value
            }
            // CSON: Empty Statement
        }

        messageList.add(ExtSystemsMessageUtil.createConvertExtToIntErrorMessage(
                "" + externalDataValue, externalDataValue //$NON-NLS-1$
                        .getClass().getName(),
                getSupportedDatatype().getQualifiedName()));
        return externalDataValue.toString();
    }

    @Override
    public Datatype getSupportedDatatype() {
        return datatype;
    }

}
