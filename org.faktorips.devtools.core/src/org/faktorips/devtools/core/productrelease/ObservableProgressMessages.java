/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
