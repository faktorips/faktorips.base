/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.tableconversion.csv;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.tableconversion.AbstractValueConverter;
import org.faktorips.util.message.MessageList;

public class StringValueConverter extends AbstractValueConverter {

    @Override
    public Object getExternalDataValue(String ipsValue, MessageList messageList) {
        return ipsValue;
    }

    @Override
    public String getIpsValue(Object externalDataValue, MessageList messageList) {
        return externalDataValue.toString();
    }

    @Override
    public Datatype getSupportedDatatype() {
        return Datatype.STRING;
    }

}
