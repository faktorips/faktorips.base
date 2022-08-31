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

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.tableconversion.AbstractValueConverter;
import org.faktorips.devtools.core.tableconversion.ExtSystemsMessageUtil;
import org.faktorips.runtime.MessageList;

/**
 * Converts from Long to String and vice versa.
 * 
 * @author Thorsten Guenther
 */
public class LongValueConverter extends AbstractValueConverter {

    /**
     * Supported types for externalDataValue are Double and Long
     */
    @Override
    public String getIpsValue(Object externalDataValue, MessageList messageList) {
        if (externalDataValue instanceof Long) {
            return ((Long)externalDataValue).toString();
        } else if (externalDataValue instanceof BigDecimal) {
            return convertBigDecimalToLongString((BigDecimal)externalDataValue, messageList);
        }
        if (StringUtils.isNumeric("" + externalDataValue)) { //$NON-NLS-1$
            // if the excel datatype isn't Long or Double but the value is numeric then convert
            // the external value to Long and add an message to inform the user about this
            // conversation
            messageList.add(ExtSystemsMessageUtil.createConvertExtToIntInformation(externalDataValue.toString(),
                    externalDataValue.getClass().getName(), getSupportedDatatype().getQualifiedName()));
        } else {
            messageList
                    .add(ExtSystemsMessageUtil
                            .createConvertExtToIntErrorMessage(
                                    "" + externalDataValue, externalDataValue.getClass().getName(), //$NON-NLS-1$
                                    getSupportedDatatype().getQualifiedName()));
        }
        return externalDataValue.toString();
    }

    private String convertBigDecimalToLongString(BigDecimal externalValue, MessageList messageList) {
        long longValue = externalValue.longValue();
        BigDecimal restoredValue = new BigDecimal(longValue);
        // check whether conversion to long would change the value
        if (!restoredValue.equals(externalValue)) {
            messageList.add(ExtSystemsMessageUtil.createConvertExtToIntLostValueErrorMessage(externalValue.toString(),
                    restoredValue.toString()));
            return externalValue.toString();
        } else {
            return Long.toString(longValue);
        }
    }

    @Override
    public Object getExternalDataValue(String ipsValue, MessageList messageList) {
        if (ipsValue == null) {
            return null;
        }
        if (ipsValue.length() == 0) {
            return Long.valueOf(0);
        }
        try {
            return Long.valueOf(ipsValue);
        } catch (NumberFormatException e) {
            messageList.add(ExtSystemsMessageUtil.createConvertIntToExtErrorMessage(ipsValue, getSupportedDatatype()
                    .getQualifiedName(), Long.class.getName()));
        }
        return ipsValue;
    }

    @Override
    public Datatype getSupportedDatatype() {
        return Datatype.LONG;
    }
}
