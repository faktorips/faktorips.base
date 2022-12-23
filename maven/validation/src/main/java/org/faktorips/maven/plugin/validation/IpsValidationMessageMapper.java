/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.maven.plugin.validation;

import org.apache.maven.plugin.logging.Log;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

public class IpsValidationMessageMapper {

    /* private */ static final String MOJO_NAME = "[Faktor-IPS-Validation]";

    private IpsValidationMessageMapper() {
        // Utility class not to be instantiated.
    }

    /* public */ static void logMessages(MessageList messageList, Log log) {

        for (Message message : messageList) {
            StringBuilder logMessage = new StringBuilder()
                    .append(MOJO_NAME)
                    .append(" ").append(message.getText()).append(" ")
                    .append("(").append(message.getCode()).append(")");

            message.appendInvalidObjectProperties(logMessage);

            switch (message.getSeverity()) {
                case ERROR:
                    log.error(logMessage.toString());
                    break;
                case WARNING:
                    log.warn(logMessage.toString());
                    break;
                default:
                    log.info(logMessage.toString());
            }
        }
    }
}