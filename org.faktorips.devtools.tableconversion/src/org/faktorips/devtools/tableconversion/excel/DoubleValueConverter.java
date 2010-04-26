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
import org.faktorips.devtools.tableconversion.ExtSystemsMessageUtil;
import org.faktorips.util.message.MessageList;

/**
 * Converts from Double to String and vice versa.
 * 
 * @author Thorsten Guenther
 */
public class DoubleValueConverter extends AbstractValueConverter {

    /**
     * Only supported type for externalDataValue is Double
     * 
     * {@inheritDoc}
     */
    public String getIpsValue(Object externalDataValue, MessageList messageList) {
        if (externalDataValue instanceof Double) {
            // format double values without decimal places to int string representation
            // maybe an import from excel returns a string formated cells (with numeric value
            // inside)
            // as double (e.g. 1 will be 1.0)
            return AbstractExternalTableFormat.doubleToStringWithoutDecimalPlaces((Double)externalDataValue);
        }
        messageList
                .add(ExtSystemsMessageUtil
                        .createConvertExtToIntErrorMessage(
                                "" + externalDataValue, externalDataValue.getClass().getName(), getSupportedDatatype().getQualifiedName())); //$NON-NLS-1$
        return externalDataValue.toString();
    }

    /**
     * {@inheritDoc}
     */
    public Object getExternalDataValue(String ipsValue, MessageList messageList) {
        if (ipsValue == null) {
            return null;
        }
        if (ipsValue.length() == 0) {
            return new Double(0);
        }
        try {
            return Double.valueOf(ipsValue);
        } catch (NumberFormatException e) {
            messageList.add(ExtSystemsMessageUtil.createConvertIntToExtErrorMessage(ipsValue, getSupportedDatatype()
                    .getQualifiedName(), Double.class.getName()));
        }
        return ipsValue;
    }

    /**
     * {@inheritDoc}
     */
    public Datatype getSupportedDatatype() {
        return Datatype.DOUBLE;
    }
}
