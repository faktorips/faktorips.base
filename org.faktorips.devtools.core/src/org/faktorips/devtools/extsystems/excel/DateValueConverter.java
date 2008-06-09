/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) dürfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1
 * (vor Gründung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation
 * 
 **************************************************************************************************/

package org.faktorips.devtools.extsystems.excel;

import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.classtypes.DateDatatype;
import org.faktorips.devtools.extsystems.ExtSystemsMessageUtil;
import org.faktorips.devtools.extsystems.IValueConverter;
import org.faktorips.util.message.MessageList;
import org.faktorips.values.DateUtil;

/**
 * Converter for the Date-Datatype.
 * 
 * @author Thorsten Guenther
 */
public class DateValueConverter implements IValueConverter {

    private DateDatatype datatype = new DateDatatype();
    
    /**
     * Supported type for the externalDataValue is Number or Date.
     * 
     * {@inheritDoc}
     */
    public String getIpsValue(Object externalDataValue, MessageList messageList) {
        Date date = null;
        boolean error = true;
        if (externalDataValue instanceof Date) {
            date = (Date)externalDataValue;
            error = false;
        } else if (externalDataValue instanceof Number) {
            date = HSSFDateUtil.getJavaDate(((Number)externalDataValue).doubleValue());
            date = new Date();
            error = false;
        } else if (externalDataValue instanceof String) {
            try {
                date = DateUtil.parseIsoDateStringToDate((String)externalDataValue);
                error = false;
            } catch (IllegalArgumentException ignored) {
            }
        }
        
        if (error){
            messageList.add(ExtSystemsMessageUtil.createConvertExtToIntErrorMessage("" + externalDataValue, externalDataValue //$NON-NLS-1$
                    .getClass().getName(), getSupportedDatatype().getQualifiedName())); 
            return externalDataValue.toString();            
        }
        return datatype.valueToString(date);
    }

    /**
     * Returns a <code>java.util.Date</code> if successfully converted, the untouched ipsValue if
     * not and <code>null</code> if the given ipsValue is <code>null</code>.
     * 
     * {@inheritDoc}
     */
    public Object getExternalDataValue(String ipsValue, MessageList messageList) {
        if (ipsValue == null) {
            return null;
        }

        try {
            return (Date)datatype.getValue(ipsValue);
        } catch (RuntimeException e) {
            messageList.add(ExtSystemsMessageUtil.createConvertIntToExtErrorMessage(ipsValue, getSupportedDatatype()
                    .getQualifiedName(), GregorianCalendar.class.getName())); //$NON-NLS-1$
            return ipsValue;
        }
    }

    /**
     * {@inheritDoc}
     */
    public Datatype getSupportedDatatype() {
        return datatype;
    }

}
