/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.eclipse.productrelease;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.faktorips.runtime.Message;

public class ObservableProgressMessages {

    private static final String INFO = "info"; //$NON-NLS-1$
    private static final String WARNING = "warning"; //$NON-NLS-1$
    private static final String ERROR = "error"; //$NON-NLS-1$

    private PropertyChangeSupport changes = new PropertyChangeSupport(this);

    public void addMessage(Message msg) {
        changes.firePropertyChange("msg", null, msg); //$NON-NLS-1$
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

    public void addPropertyChangeListener(PropertyChangeListener l) {
        changes.addPropertyChangeListener(l);
    }
}
