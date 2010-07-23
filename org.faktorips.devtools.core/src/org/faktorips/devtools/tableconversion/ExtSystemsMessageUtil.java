/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.tableconversion;

import org.eclipse.osgi.util.NLS;
import org.faktorips.util.message.Message;

/**
 * Utility class to create common error or information messages during the value conversation.
 * 
 * @author Joerg Ortmann
 */
public class ExtSystemsMessageUtil {

    public static Message createConvertExtToIntLostValueErrorMessage(String value, String convertedValue) {
        String msg = NLS.bind(Messages.Messages_extToIntInformationLostError, new String[] { value, convertedValue });
        return new Message("", msg, Message.ERROR); //$NON-NLS-1$
    }

    public static Message createConvertExtToIntErrorMessage(String value, String externalType, String internalType) {
        String msg = NLS.bind(Messages.Messages_extToIntError, new String[] { value, externalType, internalType });
        return new Message("", msg, Message.ERROR); //$NON-NLS-1$
    }

    public static Message createConvertExtToIntInformation(String value, String externalType, String internalType) {
        String msgText = NLS.bind(Messages.Messages_extToIntInformationAutoConvert, new Object[] { value, externalType,
                internalType });
        return new Message("", msgText, Message.INFO); //$NON-NLS-1$
    }

    public static Message createConvertIntToExtErrorMessage(String ipsValue, String internalType, String externalType) {
        String msg = NLS.bind(Messages.Messages_intToExtError, new String[] { ipsValue, internalType, externalType });
        return new Message("", msg, Message.ERROR); //$NON-NLS-1$
    }

    private ExtSystemsMessageUtil() {
        // Utility class not to be instantiated.
    }

}
