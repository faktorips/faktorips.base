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

package org.faktorips.devtools.extsystems;

import org.eclipse.osgi.util.NLS;
import org.faktorips.util.message.Message;

/**
 * Utility class to create common error or information messages durring the value conversation.
 * 
 * @author Joerg Ortmann
 */
public class ExtSystemsMessageUtil {

    public static Message createConvertExtToIntLostValueErrorMessage(String value, String convertedValue) {
        String msg = NLS.bind(Messages.Messages_extToIntInformationLostError, new String[]{value, convertedValue});
        return new Message("", msg, Message.ERROR); //$NON-NLS-1$
    }

    public static Message createConvertExtToIntErrorMessage(String value, String externalType, String internalType) {
        String msg = NLS.bind(Messages.Messages_extToIntError, new String[] { value,
                externalType, internalType });
        return new Message("", msg, Message.ERROR); //$NON-NLS-1$
    }

    public static Message createConvertExtToIntInformation(String value, String externalType, String internalType) {
        String msgText = NLS.bind(Messages.Messages_extToIntInformationAutoConvert,
                new Object[] { value,
                externalType, internalType });
        return new Message("", msgText, Message.INFO); //$NON-NLS-1$
    }

    public static Message createConvertIntToExtErrorMessage(String ipsValue, String internalType, String externalType) {
        String msg = NLS.bind(Messages.Messages_intToExtError, new String[]{ipsValue});
        return new Message("", msg, Message.ERROR); //$NON-NLS-1$
    }
    
    private ExtSystemsMessageUtil(){
    }
}
