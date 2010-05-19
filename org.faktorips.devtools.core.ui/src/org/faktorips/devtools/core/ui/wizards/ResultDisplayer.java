/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;

/**
 * This class can be used to display a <code>MessageList</code> by using the
 * <code>ErrorDialog</code> of Eclipse. Because often it is neccessary to display results produced
 * with a thread which is not the UI-thread, this class implements <code>Runnable</code>, so it can
 * be used simply in these other threads by calling
 * <code>getShell().getDisplay().syncExec(new ResultDisplayer(getShell(), messageList));</code>
 * 
 * @author Thorsten Guenther
 */
public class ResultDisplayer implements Runnable {
    /** The list of messages to display */
    private MessageList messageList;

    /** The shell to open the result dialog within */
    private Shell shell;

    /** Name of the operation (String representation) */
    private String operationName;

    /**
     * Creates a new ResultDisplayer.
     * 
     * @param shell The shell to open the result dialog within.
     * @param operationName The name of the operation, will be displayed in the dialog.
     * @param messageList The list of messages to display.
     */
    public ResultDisplayer(Shell shell, String operationName, MessageList messageList) {
        this.messageList = messageList;
        this.operationName = operationName;
        this.shell = shell;
    }

    /**
     * This method has to be called in the UI-thread. If it is not shure that this method allways
     * will be executed in the UI-thread, use
     * <code>getShell().getDisplay().syncExec(new ResultDisplayer(getShell(), messageList));</code>
     * instead.
     * <p>
     * Displays the MessageList using the ErrorDialog of Eclipse.
     */
    public void displayResults() {
        run();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        MultiStatus multiStatus = new MultiStatus(IpsPlugin.PLUGIN_ID, 0, Messages.ResultDisplayer_reasonText, null);

        boolean containsWarningsOrInfos = messageList.getFirstMessage(Message.INFO) != null
                || messageList.getFirstMessage(Message.WARNING) != null;
        boolean containsErrors = messageList.containsErrorMsg();

        // create a multistatus based on the given message list,
        // oder errors ascendng
        if (containsErrors) {
            for (Message msg : messageList) {
                switch (msg.getSeverity()) {
                    case Message.ERROR:
                        multiStatus
                                .add(new IpsStatus(
                                        IStatus.ERROR,
                                        0,
                                        (containsWarningsOrInfos ? Messages.ResultDisplayer_Errors : "") + getMessageText(msg), null)); //$NON-NLS-1$
                        break;
                }
            }
        }

        if (containsWarningsOrInfos) {
            for (Message msg : messageList) {
                switch (msg.getSeverity()) {
                    case Message.WARNING:
                        multiStatus.add(new IpsStatus(IStatus.WARNING, 0,
                                (containsErrors ? Messages.ResultDisplayer_Warnings : "") + getMessageText(msg), null)); //$NON-NLS-1$
                        break;
                    case Message.INFO:
                        multiStatus
                                .add(new IpsStatus(
                                        IStatus.INFO,
                                        0,
                                        (containsErrors ? Messages.ResultDisplayer_Informations : "") + getMessageText(msg), null)); //$NON-NLS-1$
                        break;
                }
            }
        }

        String messageText;
        if (multiStatus.getSeverity() == IStatus.WARNING) {
            messageText = NLS.bind(Messages.ResultDisplayer_msgWarnings, operationName);
        } else if (multiStatus.getSeverity() == IStatus.INFO) {
            messageText = NLS.bind(Messages.ResultDisplayer_msgInformations, operationName);
        } else {
            messageText = NLS.bind(Messages.ResultDisplayer_msgErrors, operationName);
        }

        ErrorDialog.openError(shell, NLS.bind(Messages.ResultDisplayer_titleResults, operationName), messageText,
                multiStatus);
    }

    /*
     * Returns the message text and if available adds the string representation of the object the
     * message was created for
     */
    private String getMessageText(Message msg) {
        String text = msg.getText();
        ObjectProperty[] invalidObjectProperties = msg.getInvalidObjectProperties();
        if (invalidObjectProperties == null || invalidObjectProperties.length == 0) {
            return text;
        }
        text += ": "; //$NON-NLS-1$
        for (int i = 0; i < invalidObjectProperties.length; i++) {
            text += invalidObjectProperties[i].getObject();
            if (i + 1 < invalidObjectProperties.length) {
                text += "\n  "; //$NON-NLS-1$
            }
        }
        return text;
    }
}
