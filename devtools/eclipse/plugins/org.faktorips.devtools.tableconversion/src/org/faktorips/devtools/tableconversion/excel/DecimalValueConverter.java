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

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.tableconversion.AbstractValueConverter;
import org.faktorips.devtools.core.tableconversion.ExtSystemsMessageUtil;
import org.faktorips.runtime.MessageList;
import org.faktorips.values.Decimal;

/**
 * Converter for Decimal
 *
 * @author Thorsten Guenther
 */
public class DecimalValueConverter extends AbstractValueConverter {

    /**
     * Supported types for the externalDataValue are String and Number.
     */
    @Override
    public String getIpsValue(Object externalDataValue, MessageList messageList) {
        if (externalDataValue instanceof String stringValue) {
            try {
                return Decimal.valueOf(stringValue).toString();
                // CSOFF: IllegalCatch
            } catch (RuntimeException e) {
                // fall through to error message
            }
            // CSON: IllegalCatch
        } else if (externalDataValue instanceof Number number) {
            return Decimal.valueOf(new BigDecimal(number.toString())).toString();
        }
        messageList.add(ExtSystemsMessageUtil.createConvertExtToIntErrorMessage(
                "" + externalDataValue, externalDataValue.getClass() //$NON-NLS-1$
                        .getName(),
                getSupportedDatatype().getQualifiedName()));
        return externalDataValue.toString();
    }

    @Override
    public Object getExternalDataValue(String ipsValue, MessageList messageList) {
        try {
            Decimal decimal = Decimal.valueOf(ipsValue);
            if (decimal == Decimal.NULL) {
                return null;
            }
            return decimal.doubleValue();
            // CSOFF: IllegalCatch
        } catch (RuntimeException e) {
            messageList.add(ExtSystemsMessageUtil.createConvertIntToExtErrorMessage(ipsValue, Decimal.class.getName(),
                    getSupportedDatatype().getQualifiedName()));
        }
        // CSON: IllegalCatch
        return ipsValue;
    }

    @Override
    public Datatype getSupportedDatatype() {
        return Datatype.DECIMAL;
    }

}
