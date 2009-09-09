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

package org.faktorips.devtools.tableconversion.csv;

import java.text.DecimalFormat;
import java.text.ParseException;

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.classtypes.DecimalDatatype;
import org.faktorips.devtools.tableconversion.ExtSystemsMessageUtil;
import org.faktorips.util.message.MessageList;
import org.faktorips.values.Decimal;

public class DecimalValueConverter extends NumberValueConverter {

    /**
     * {@inheritDoc}
     */
    public Object getExternalDataValue(String ipsValue, MessageList messageList) {
        if (ipsValue == null) {
            return null;
        }
        try {
            Decimal decimal = (Decimal)new DecimalDatatype().getValue(ipsValue);
            if (tableFormat == null) {
                return decimal.toString();
            }

            DecimalFormat decimalFormat = getDecimalFormat(tableFormat);
            return decimalFormat.format(decimal);
        } catch (RuntimeException e) {
            messageList.add(ExtSystemsMessageUtil.createConvertIntToExtErrorMessage(ipsValue, Decimal.class.getName(),
                    getSupportedDatatype().getQualifiedName()));
        }
        return ipsValue;
    }

    /**
     * The only supported type for externalDataValue is String.
     */
    public String getIpsValue(Object externalDataValue, MessageList messageList) {
        if (externalDataValue instanceof String) {
            try {
                DecimalFormat decimalFormat = getDecimalFormat(tableFormat);

                Number number = decimalFormat.parse((String)externalDataValue);
                return number.toString();
            } catch (NumberFormatException e) {
                messageList.add(ExtSystemsMessageUtil.createConvertExtToIntErrorMessage(
                        "" + externalDataValue, externalDataValue //$NON-NLS-1$
                                .getClass().getName(), getSupportedDatatype().getQualifiedName()));
                return externalDataValue.toString();

            } catch (ParseException e) {
                messageList.add(ExtSystemsMessageUtil.createConvertExtToIntErrorMessage(
                        "" + externalDataValue, externalDataValue //$NON-NLS-1$
                                .getClass().getName(), getSupportedDatatype().getQualifiedName()));
                return externalDataValue.toString();
            }
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
    public Datatype getSupportedDatatype() {
        return Datatype.DECIMAL;
    }

}
