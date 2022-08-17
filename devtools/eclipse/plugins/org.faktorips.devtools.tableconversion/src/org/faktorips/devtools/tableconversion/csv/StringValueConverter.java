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
import org.faktorips.runtime.MessageList;

public class StringValueConverter extends AbstractValueConverter {

    @Override
    public Object getExternalDataValue(String ipsValue, MessageList messageList) {
        return ipsValue;
    }

    @Override
    public String getIpsValue(Object externalDataValue, MessageList messageList) {
        return externalDataValue.toString().trim();
    }

    @Override
    public Datatype getSupportedDatatype() {
        return Datatype.STRING;
    }

}
