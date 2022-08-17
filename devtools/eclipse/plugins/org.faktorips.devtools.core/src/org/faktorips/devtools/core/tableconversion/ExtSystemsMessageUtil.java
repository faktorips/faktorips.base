/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.tableconversion;

import org.eclipse.osgi.util.NLS;
import org.faktorips.runtime.Message;

/**
 * Utility class to create common error or information messages during the value conversation.
 * 
 * @author Joerg Ortmann
 */
public class ExtSystemsMessageUtil {

    private ExtSystemsMessageUtil() {
        // Utility class not to be instantiated.
    }

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
}
