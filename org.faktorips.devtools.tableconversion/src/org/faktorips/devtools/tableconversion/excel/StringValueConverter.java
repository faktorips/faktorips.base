/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.tableconversion.excel;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.tableconversion.AbstractExternalTableFormat;
import org.faktorips.devtools.tableconversion.AbstractValueConverter;
import org.faktorips.util.message.MessageList;

/**
 * Converts from String to String :-)
 * 
 * @author Thorsten Guenther
 */
public class StringValueConverter extends AbstractValueConverter {

    /**
     * Every type is allowed for externalDataValue.
     * 
     * {@inheritDoc}
     */
    @Override
    public String getIpsValue(Object externalDataValue, MessageList messageList) {
        if (externalDataValue instanceof Double) {
            return AbstractExternalTableFormat.doubleToStringWithoutDecimalPlaces((Double)externalDataValue);
        }
        return externalDataValue == null ? null : externalDataValue.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getExternalDataValue(String ipsValue, MessageList messageList) {
        return ipsValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Datatype getSupportedDatatype() {
        return Datatype.STRING;
    }
}
