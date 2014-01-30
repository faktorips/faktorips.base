/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.tableconversion.excel;

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.tableconversion.AbstractValueConverter;
import org.faktorips.devtools.tableconversion.ExtSystemsMessageUtil;
import org.faktorips.util.message.MessageList;

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
        } else if (externalDataValue instanceof Double) {
            long value = ((Double)externalDataValue).longValue();
            Double restored = new Double(value);
            if (!restored.equals(externalDataValue)) {
                messageList.add(ExtSystemsMessageUtil.createConvertExtToIntLostValueErrorMessage(
                        "" + externalDataValue, restored.toString())); //$NON-NLS-1$
                return externalDataValue.toString();
            }
            return new Long(value).toString();
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
                                    "" + externalDataValue, externalDataValue.getClass().getName(), getSupportedDatatype().getQualifiedName())); //$NON-NLS-1$
        }
        return externalDataValue.toString();
    }

    @Override
    public Object getExternalDataValue(String ipsValue, MessageList messageList) {
        if (ipsValue == null) {
            return null;
        }
        if (ipsValue.length() == 0) {
            return new Long(0);
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
