/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.productrelease;

import java.util.Observable;

import org.faktorips.util.message.Message;

public class ObservableProgressMessages extends Observable {

    private static final String INFO = "info"; //$NON-NLS-1$
    private static final String WARNING = "warning"; //$NON-NLS-1$
    private static final String ERROR = "error"; //$NON-NLS-1$

    public void addMessage(Message msg) {
        setChanged();
        notifyObservers(msg);
    }

    public void info(String messageText) {
        Message msg = new Message(INFO, messageText, Message.INFO);
        addMessage(msg);
    }

    public void warning(String messageText) {
        Message msg = new Message(WARNING, messageText, Message.WARNING);
        addMessage(msg);
    }

    public void error(String messageText) {
        Message msg = new Message(ERROR, messageText, Message.ERROR);
        addMessage(msg);
    }
}
