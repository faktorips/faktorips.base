/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.tableconversion.excel;

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.tableconversion.AbstractValueConverter;
import org.faktorips.devtools.tableconversion.ExtSystemsMessageUtil;
import org.faktorips.util.message.MessageList;

/**
 * Converts from Integer to String and vice versa.
 * 
 * @author Thorsten Guenther
 */
public class IntegerValueConverter extends AbstractValueConverter {

    /**
     * Supported types for externalDataValue are Integer and Double.
     */
    @Override
    public String getIpsValue(Object externalDataValue, MessageList messageList) {
        if (externalDataValue instanceof Integer) {
            return ((Integer)externalDataValue).toString();
        } else if (externalDataValue instanceof Double) {
            int value = ((Double)externalDataValue).intValue();
            Double restored = new Double(value);
            if (!restored.equals(externalDataValue)) {
                messageList.add(ExtSystemsMessageUtil.createConvertExtToIntLostValueErrorMessage(
                        externalDataValue.toString(), restored.toString()));
                return externalDataValue.toString();
            }
            return new Integer(value).toString();
        }
        if (StringUtils.isNumeric("" + externalDataValue)) { //$NON-NLS-1$
            // if the excel datatype isn't Integer or Double but the value is numeric then convert
            // the external value to Integer and add an message to inform the user about this
            // conversation
            messageList.add(ExtSystemsMessageUtil.createConvertExtToIntInformation(externalDataValue.toString(),
                    externalDataValue.getClass().getName(), getSupportedDatatype().getQualifiedName()));
        } else {
            messageList.add(ExtSystemsMessageUtil.createConvertExtToIntErrorMessage(externalDataValue.toString(),
                    externalDataValue.getClass().getName(), getSupportedDatatype().getQualifiedName()));
        }
        return externalDataValue.toString();
    }

    @Override
    public Object getExternalDataValue(String ipsValue, MessageList messageList) {
        if (ipsValue == null) {
            return null;
        }
        if (ipsValue.length() == 0) {
            return new Integer(0);
        }
        try {
            return Integer.valueOf(ipsValue);
        } catch (NumberFormatException e) {
            messageList.add(ExtSystemsMessageUtil.createConvertExtToIntErrorMessage(ipsValue, Integer.class.getName(),
                    getSupportedDatatype().getQualifiedName()));
        }
        return ipsValue;
    }

    @Override
    public Datatype getSupportedDatatype() {
        return Datatype.INTEGER;
    }
}
