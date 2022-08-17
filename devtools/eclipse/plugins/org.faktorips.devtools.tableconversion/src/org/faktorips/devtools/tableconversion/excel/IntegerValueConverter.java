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

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.tableconversion.AbstractValueConverter;
import org.faktorips.devtools.core.tableconversion.ExtSystemsMessageUtil;
import org.faktorips.runtime.MessageList;

/**
 * Converts from Integer to String and vice versa.
 * 
 * @author Thorsten Guenther
 */
public class IntegerValueConverter extends AbstractValueConverter {

    /**
     * Supported types for externalDataValue are Integer and Double.
     */
    @Override
    public String getIpsValue(Object externalDataValue, MessageList messageList) {
        if (externalDataValue instanceof Integer) {
            return ((Integer)externalDataValue).toString();
        } else if (externalDataValue instanceof BigDecimal) {
            int value = ((BigDecimal)externalDataValue).intValue();
            BigDecimal restored = new BigDecimal(value);
            if (!restored.equals(externalDataValue)) {
                messageList.add(ExtSystemsMessageUtil.createConvertExtToIntLostValueErrorMessage(
                        externalDataValue.toString(), restored.toString()));
                return externalDataValue.toString();
            }
            return Integer.toString(value);
        }
        if (StringUtils.isNumeric("" + externalDataValue)) { //$NON-NLS-1$
            // if the excel datatype isn't Integer or Double but the value is numeric then convert
            // the external value to Integer and add an message to inform the user about this
            // conversation
            messageList.add(ExtSystemsMessageUtil.createConvertExtToIntInformation(externalDataValue.toString(),
                    externalDataValue.getClass().getName(), getSupportedDatatype().getQualifiedName()));
        } else {
            messageList.add(ExtSystemsMessageUtil.createConvertExtToIntErrorMessage(externalDataValue.toString(),
                    externalDataValue.getClass().getName(), getSupportedDatatype().getQualifiedName()));
        }
        return externalDataValue.toString();
    }

    @Override
    public Object getExternalDataValue(String ipsValue, MessageList messageList) {
        if (ipsValue == null) {
            return null;
        }
        if (ipsValue.length() == 0) {
            return Integer.valueOf(0);
        }
        try {
            return Integer.valueOf(ipsValue);
        } catch (NumberFormatException e) {
            messageList.add(ExtSystemsMessageUtil.createConvertExtToIntErrorMessage(ipsValue, Integer.class.getName(),
                    getSupportedDatatype().getQualifiedName()));
        }
        return ipsValue;
    }

    @Override
    public Datatype getSupportedDatatype() {
        return Datatype.INTEGER;
    }
}
