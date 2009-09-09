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
import org.faktorips.devtools.tableconversion.ExtSystemsMessageUtil;
import org.faktorips.util.message.MessageList;

public class DoubleValueConverter extends NumberValueConverter {

    // FIXME rg: use ITableFormat's decimal separator char!
    public Object getExternalDataValue(String ipsValue, MessageList messageList) {
        if (ipsValue == null) {
            return null;
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

    public Datatype getSupportedDatatype() {
        return Datatype.DOUBLE;
    }
}
