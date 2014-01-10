/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.tableconversion.excel;

import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.classtypes.DateDatatype;
import org.faktorips.devtools.tableconversion.AbstractValueConverter;
import org.faktorips.devtools.tableconversion.ExtSystemsMessageUtil;
import org.faktorips.util.message.MessageList;
import org.faktorips.values.DateUtil;

/**
 * Converter for the Date-Datatype.
 * 
 * @author Thorsten Guenther
 */
public class DateValueConverter extends AbstractValueConverter {

    private DateDatatype datatype = new DateDatatype();

    /**
     * Supported type for the externalDataValue is Number or Date.
     */
    @Override
    public String getIpsValue(Object externalDataValue, MessageList messageList) {
        Date date = null;
        boolean error = true;
        if (externalDataValue instanceof Date) {
            date = (Date)externalDataValue;
            error = false;
        } else if (externalDataValue instanceof Number) {
            date = HSSFDateUtil.getJavaDate(((Number)externalDataValue).doubleValue());
            date = new Date();
            error = false;
        } else if (externalDataValue instanceof String) {
            try {
                date = DateUtil.parseIsoDateStringToDate((String)externalDataValue);
                error = false;
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (error) {
            messageList.add(ExtSystemsMessageUtil.createConvertExtToIntErrorMessage(
                    "" + externalDataValue, externalDataValue //$NON-NLS-1$
                            .getClass().getName(), getSupportedDatatype().getQualifiedName()));
            return externalDataValue.toString();
        }
        return datatype.valueToString(date);
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
            return datatype.getValue(ipsValue);
        } catch (RuntimeException e) {
            messageList.add(ExtSystemsMessageUtil.createConvertIntToExtErrorMessage(ipsValue, getSupportedDatatype()
                    .getQualifiedName(), GregorianCalendar.class.getName()));
            return ipsValue;
        }
    }

    @Override
    public Datatype getSupportedDatatype() {
        return datatype;
    }

}
