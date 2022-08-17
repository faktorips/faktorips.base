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

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.tableconversion.AbstractValueConverter;
import org.faktorips.devtools.core.tableconversion.ExtSystemsMessageUtil;
import org.faktorips.runtime.MessageList;

public class BooleanValueConverter extends AbstractValueConverter {

    @Override
    public Object getExternalDataValue(String ipsValue, MessageList messageList) {
        if (ipsValue == null) {
            return null;
        }
        return String.valueOf(ipsValue);
    }

    /**
     * The only supported type for externalDataValue is String.
     */
    @Override
    public String getIpsValue(Object externalDataValue, MessageList messageList) {
        if (externalDataValue instanceof String) {
            return Boolean.valueOf((String)externalDataValue).toString();
        }
        messageList.add(ExtSystemsMessageUtil.createConvertExtToIntErrorMessage("" + externalDataValue, //$NON-NLS-1$
                externalDataValue.getClass()
                        .getName(),
                getSupportedDatatype().getQualifiedName()));
        return externalDataValue.toString();
    }

    @Override
    public Datatype getSupportedDatatype() {
        return Datatype.BOOLEAN;
    }

}
