/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.eclipse.mapping;

import org.eclipse.core.runtime.IStatus;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

/**
 * Maps two concepts used to indicate an operation's results: {@link IStatus} (used by Eclipse) and
 * {@link MessageList} (used by Faktor-IPS).
 */
public class StatusMessageListMapping {

    private StatusMessageListMapping() {
        // util
    }

    public static MessageList toMessageList(IStatus status) {
        MessageList messageList = new MessageList();
        if (!status.isOK()) {
            messageList.add(toMessage(status));
        }
        for (IStatus childStatus : status.getChildren()) {
            messageList.add(toMessageList(childStatus));
        }
        return messageList;
    }

    private static Message toMessage(IStatus status) {
        return new Message(status.getPlugin() + status.getCode(), status.getMessage(),
                SeverityMapping.toIps(status.getSeverity()));
    }

}
