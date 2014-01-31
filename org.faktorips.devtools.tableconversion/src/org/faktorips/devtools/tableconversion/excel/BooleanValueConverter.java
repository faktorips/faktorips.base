/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.tableconversion.excel;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.tableconversion.AbstractValueConverter;
import org.faktorips.devtools.tableconversion.ExtSystemsMessageUtil;
import org.faktorips.util.message.MessageList;

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
        if (externalDataValue instanceof String) {
            return Boolean.valueOf((String)externalDataValue).toString();
        } else if (externalDataValue instanceof Boolean) {
            return ((Boolean)externalDataValue).toString();
        }
        messageList.add(ExtSystemsMessageUtil.createConvertExtToIntErrorMessage(
                "" + externalDataValue, externalDataValue.getClass() //$NON-NLS-1$
                        .getName(), getSupportedDatatype().getQualifiedName()));
        return externalDataValue.toString();
    }

    @Override
    public Object getExternalDataValue(String ipsValue, MessageList messageList) {
        if (ipsValue == null) {
            return null;
        }
        return new Boolean(ipsValue);
    }

    @Override
    public Datatype getSupportedDatatype() {
        return Datatype.BOOLEAN;
    }

}
