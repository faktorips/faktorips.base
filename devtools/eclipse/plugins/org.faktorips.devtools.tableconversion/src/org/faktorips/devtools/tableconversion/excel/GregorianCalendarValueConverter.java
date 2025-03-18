/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.tableconversion.excel;

import java.util.Date;
import java.util.GregorianCalendar;

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.classtypes.DateDatatype;
import org.faktorips.datatype.classtypes.GregorianCalendarDatatype;
import org.faktorips.devtools.core.tableconversion.AbstractValueConverter;
import org.faktorips.devtools.core.tableconversion.ExtSystemsMessageUtil;
import org.faktorips.runtime.MessageList;
import org.faktorips.values.DateUtil;

/**
 * Converter for the GregorianCalendarDatatype.
 *
 * @author Thorsten Guenther
 */
public class GregorianCalendarValueConverter extends AbstractValueConverter {

    /**
     * Supported type for the externalDataValue is String, Number, or Date.
     */
    @Override
    public String getIpsValue(Object externalDataValue, MessageList messageList) {
        GregorianCalendarDatatype datatype = (GregorianCalendarDatatype)getSupportedDatatype();
        GregorianCalendar cal = new GregorianCalendar();
        boolean error;
        switch (externalDataValue) {
            case Date date -> {
                cal.setTime(date);
                error = false;
            }
            case Number number -> {
                Date date = new Date(number.longValue());
                cal.setTime(date);
                error = false;
            }
            case String string -> {
                try {
                    cal = DateUtil.parseIsoDateStringToGregorianCalendar(string);
                    error = false;
                } catch (IllegalArgumentException ignored) {
                    error = true;
                }
            }
            default -> error = false;
        }

        if (error) {
            messageList
                    .add(ExtSystemsMessageUtil
                            .createConvertExtToIntErrorMessage(
                                    "" + externalDataValue, externalDataValue.getClass().getName(), //$NON-NLS-1$
                                    getSupportedDatatype().getQualifiedName()));
            return externalDataValue.toString();
        }
        return datatype.valueToString(cal);
    }

    /**
     * Returns a <code>java.util.Date</code> if successfully converted, the untouched ipsValue if
     * not and <code>null</code> if the given ipsValue is <code>null</code>.
     */
    @Override
    public Object getExternalDataValue(String ipsValue, MessageList messageList) {
        if (ipsValue == null) {
            return null;
        }

        try {
            return new DateDatatype().getValue(ipsValue);
        } catch (IllegalArgumentException e) {
            messageList.add(ExtSystemsMessageUtil.createConvertIntToExtErrorMessage(ipsValue, getSupportedDatatype()
                    .getQualifiedName(), GregorianCalendar.class.getName()));
            return ipsValue;
        }
    }

    @Override
    public Datatype getSupportedDatatype() {
        return Datatype.GREGORIAN_CALENDAR;
    }

}
