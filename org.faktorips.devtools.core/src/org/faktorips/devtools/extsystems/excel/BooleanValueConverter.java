/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.extsystems.excel;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.extsystems.ExtSystemsMessageUtil;
import org.faktorips.devtools.extsystems.IValueConverter;
import org.faktorips.util.message.MessageList;

/**
 * Converts from Boolean to String and vice versa.
 * 
 * @author Thorsten Guenther
 */
public class BooleanValueConverter implements IValueConverter {

	/**
	 * Supported types for the externalDataValue are String and Boolean.
	 * 
	 * {@inheritDoc}
	 */
	public String getIpsValue(Object externalDataValue, MessageList messageList) {
        if (externalDataValue instanceof String) {
            return Boolean.valueOf((String)externalDataValue).toString();
        } else if (externalDataValue instanceof Boolean) {
            return ((Boolean)externalDataValue).toString();
        }
        messageList.add(ExtSystemsMessageUtil.createConvertExtToIntErrorMessage("" + externalDataValue, externalDataValue.getClass() //$NON-NLS-1$
                .getName(), getSupportedDatatype().getQualifiedName()));
        return externalDataValue.toString();
    }

	/**
     * {@inheritDoc}
     */
	public Object getExternalDataValue(String ipsValue, MessageList messageList) {
		if (ipsValue == null) {
			return null;
		}
		return new Boolean(ipsValue);
	}

	/**
	 * {@inheritDoc}
	 */
	public Datatype getSupportedDatatype() {
		return Datatype.BOOLEAN;
	}

}
