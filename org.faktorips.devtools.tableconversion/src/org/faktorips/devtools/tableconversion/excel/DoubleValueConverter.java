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
import org.faktorips.devtools.core.tableconversion.AbstractExternalTableFormat;
import org.faktorips.devtools.core.tableconversion.AbstractValueConverter;
import org.faktorips.devtools.core.tableconversion.ExtSystemsMessageUtil;
import org.faktorips.runtime.MessageList;

/**
 * Converts from Double to String and vice versa.
 * 
 * @author Thorsten Guenther
 */
public class DoubleValueConverter extends AbstractValueConverter {

    /**
     * Only supported type for externalDataValue is Double
     */
    @Override
    public String getIpsValue(Object externalDataValue, MessageList messageList) {
        if (externalDataValue instanceof Double) {
            // format double values without decimal places to int string representation
            // maybe an import from excel returns a string formated cells (with numeric value
            // inside)
            // as double (e.g. 1 will be 1.0)
            return AbstractExternalTableFormat.doubleToStringWithoutDecimalPlaces((Double)externalDataValue);
        }
        if (externalDataValue instanceof BigDecimal) {
            // format converted double value of BigDecimal
            return AbstractExternalTableFormat.doubleToStringWithoutDecimalPlaces(((BigDecimal)externalDataValue)
                    .doubleValue());
        }
        messageList
                .add(ExtSystemsMessageUtil
                        .createConvertExtToIntErrorMessage(
                                "" + externalDataValue, externalDataValue.getClass().getName(), //$NON-NLS-1$
                                getSupportedDatatype().getQualifiedName()));
        return externalDataValue.toString();
    }

    @Override
    public Object getExternalDataValue(String ipsValue, MessageList messageList) {
        if (ipsValue == null) {
            return null;
        }
        if (ipsValue.length() == 0) {
            return Double.valueOf(0);
        }
        try {
            return Double.valueOf(ipsValue);
        } catch (NumberFormatException e) {
            messageList.add(ExtSystemsMessageUtil.createConvertIntToExtErrorMessage(ipsValue, getSupportedDatatype()
                    .getQualifiedName(), Double.class.getName()));
        }
        return ipsValue;
    }

    @Override
    public Datatype getSupportedDatatype() {
        return Datatype.DOUBLE;
    }
}
