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

import java.text.DecimalFormat;
import java.text.ParseException;

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.classtypes.DecimalDatatype;
import org.faktorips.devtools.core.tableconversion.ExtSystemsMessageUtil;
import org.faktorips.runtime.MessageList;
import org.faktorips.values.Decimal;

public class DecimalValueConverter extends NumberValueConverter {

    @Override
    public Object getExternalDataValue(String ipsValue, MessageList messageList) {
        if (ipsValue == null) {
            return null;
        }
        try {
            Decimal d = (Decimal)new DecimalDatatype().getValue(ipsValue);
            String result = d.toString();
            if (tableFormat != null) {
                String decimalSeparator = tableFormat.getProperty(CSVTableFormat.PROPERTY_DECIMAL_SEPARATOR_CHAR);
                if (decimalSeparator.length() == 1) {
                    result = result.replace(".", //$NON-NLS-1$
                            tableFormat.getProperty(CSVTableFormat.PROPERTY_DECIMAL_SEPARATOR_CHAR));
                }
            }

            return result;
            // CSOFF: IllegalCatch
        } catch (RuntimeException e) {
            messageList.add(ExtSystemsMessageUtil.createConvertIntToExtErrorMessage(ipsValue, Decimal.class.getName(),
                    getSupportedDatatype().getQualifiedName()));
        }
        // CSON: IllegalCatch
        return ipsValue;
    }

    /**
     * The only supported type for externalDataValue is String.
     */
    @Override
    public String getIpsValue(Object externalDataValue, MessageList messageList) {
        if (externalDataValue instanceof String) {
            try {
                DecimalFormat decimalFormat = getDecimalFormat(tableFormat);

                Number number = decimalFormat.parse((String)externalDataValue);
                return number.toString();
            } catch (NumberFormatException | ParseException e) {
                // fall through to error message
            }
        }
        messageList
                .add(ExtSystemsMessageUtil
                        .createConvertExtToIntErrorMessage(
                                "" + externalDataValue, externalDataValue.getClass().getName(), //$NON-NLS-1$
                                getSupportedDatatype().getQualifiedName()));
        return externalDataValue.toString();
    }

    @Override
    public Datatype getSupportedDatatype() {
        return Datatype.DECIMAL;
    }

}
