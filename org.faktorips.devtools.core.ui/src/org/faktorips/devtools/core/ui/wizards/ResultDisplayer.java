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

import java.util.Iterator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

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
    public void run() {
        MultiStatus multiStatus = new MultiStatus(IpsPlugin.PLUGIN_ID, 0, Messages.ResultDisplayer_reasonText, null);

        boolean containsWarningsOrInfos = messageList.getFirstMessage(Message.INFO) != null
                || messageList.getFirstMessage(Message.WARNING) != null;
        boolean containsErrors = messageList.containsErrorMsg();

        // create a multistatus based on the given message list,
        // oder errors ascendng
        if (containsErrors) {
            for (Iterator<Message> iter = messageList.iterator(); iter.hasNext();) {
                Message msg = iter.next();
                switch (msg.getSeverity()) {
                    case Message.ERROR:
                        multiStatus
                                .add(new IpsStatus(
                                        IStatus.ERROR,
                                        0,
                                        (containsWarningsOrInfos ? Messages.ResultDisplayer_Errors : "") + msg.getText(), null)); //$NON-NLS-1$
                        break;
                }
            }
        }

        if (containsWarningsOrInfos) {
            for (Iterator<Message> iter = messageList.iterator(); iter.hasNext();) {
                Message msg = iter.next();
                switch (msg.getSeverity()) {
                    case Message.WARNING:
                        multiStatus.add(new IpsStatus(IStatus.WARNING, 0,
                                (containsErrors ? Messages.ResultDisplayer_Warnings : "") + msg.getText(), null)); //$NON-NLS-1$
                        break;
                    case Message.INFO:
                        multiStatus.add(new IpsStatus(IStatus.INFO, 0,
                                (containsErrors ? Messages.ResultDisplayer_Informations : "") + msg.getText(), null)); //$NON-NLS-1$
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
}
