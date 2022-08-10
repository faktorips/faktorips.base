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
import org.faktorips.devtools.core.tableconversion.AbstractValueConverter;
import org.faktorips.devtools.core.tableconversion.ExtSystemsMessageUtil;
import org.faktorips.runtime.MessageList;

/**
 * Converts from Boolean to String and vice versa.
 * 
 * @author Thorsten Guenther
 */
public class BooleanValueConverter extends AbstractValueConverter {

    /**
     * Supported types for the externalDataValue are String and Boolean.
     */
    @Override
    public String getIpsValue(Object externalDataValue, MessageList messageList) {
        if (externalDataValue instanceof Boolean) {
            return ((Boolean)externalDataValue).toString();
        } else {
            String dataValue = String.valueOf(externalDataValue);
            if ("true".equalsIgnoreCase(dataValue) || "1".equalsIgnoreCase(dataValue)) { //$NON-NLS-1$ //$NON-NLS-2$
                return Boolean.toString(true);
            } else if ("false".equalsIgnoreCase(dataValue) || "0".equalsIgnoreCase(dataValue)) { //$NON-NLS-1$ //$NON-NLS-2$
                return Boolean.toString(false);
            } else {
                if (externalDataValue != null) {
                    messageList.add(ExtSystemsMessageUtil.createConvertExtToIntErrorMessage(dataValue,
                            externalDataValue.getClass().getName(), getSupportedDatatype().getQualifiedName()));
                } else {
                    messageList.add(
                            ExtSystemsMessageUtil.createConvertExtToIntLostValueErrorMessage(dataValue, dataValue));
                }
                return dataValue;
            }
        }
    }

    @Override
    public Object getExternalDataValue(String ipsValue, MessageList messageList) {
        if (ipsValue == null) {
            return null;
        }
        return Boolean.valueOf(ipsValue);
    }

    @Override
    public Datatype getSupportedDatatype() {
        return Datatype.BOOLEAN;
    }

}
