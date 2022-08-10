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

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.tableconversion.AbstractExternalTableFormat;
import org.faktorips.devtools.core.tableconversion.AbstractValueConverter;
import org.faktorips.runtime.MessageList;

/**
 * Converts from String to String :-)
 * 
 * @author Thorsten Guenther
 */
public class StringValueConverter extends AbstractValueConverter {

    /**
     * Every type is allowed for externalDataValue.
     */
    @Override
    public String getIpsValue(Object externalDataValue, MessageList messageList) {
        if (externalDataValue instanceof Double) {
            return AbstractExternalTableFormat.doubleToStringWithoutDecimalPlaces((Double)externalDataValue);
        }
        return externalDataValue == null ? null : externalDataValue.toString().trim();
    }

    @Override
    public Object getExternalDataValue(String ipsValue, MessageList messageList) {
        return ipsValue;
    }

    @Override
    public Datatype getSupportedDatatype() {
        return Datatype.STRING;
    }
}
