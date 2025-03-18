/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;

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

    @Override
    public void run() {
        MultiStatus multiStatus = new MultiStatus(IpsPlugin.PLUGIN_ID, 0, Messages.ResultDisplayer_reasonText, null);

        boolean containsWarningsOrInfos = messageList.getFirstMessage(Message.INFO) != null
                || messageList.getFirstMessage(Message.WARNING) != null;
        boolean containsErrors = messageList.containsErrorMsg();

        // create a multi-status based on the given message list,
        // oder errors ascending
        if (containsErrors) {
            for (Message msg : messageList) {
                switch (msg.getSeverity()) {
                    case ERROR -> multiStatus
                            .add(new IpsStatus(
                                    IStatus.ERROR,
                                    0,
                                    (containsWarningsOrInfos ? Messages.ResultDisplayer_Errors : "") //$NON-NLS-1$
                                            + getMessageText(msg),
                                    null));
                    default -> {
                        // skip
                    }
                }
            }
        }

        if (containsWarningsOrInfos) {
            for (Message msg : messageList) {
                switch (msg.getSeverity()) {
                    case WARNING -> multiStatus.add(new IpsStatus(IStatus.WARNING, 0,
                            (containsErrors ? Messages.ResultDisplayer_Warnings : "") + getMessageText(msg), null)); //$NON-NLS-1$
                    case INFO -> multiStatus
                            .add(new IpsStatus(IStatus.INFO, 0,
                                    (containsErrors ? Messages.ResultDisplayer_Informations : "") + getMessageText(msg), //$NON-NLS-1$
                                    null));
                    default -> {
                        // already added
                    }
                }
            }
        }

        String messageText = switch (multiStatus.getSeverity()) {
            case IStatus.WARNING -> NLS.bind(Messages.ResultDisplayer_msgWarnings, operationName);
            case IStatus.INFO -> NLS.bind(Messages.ResultDisplayer_msgInformations, operationName);
            default -> NLS.bind(Messages.ResultDisplayer_msgErrors, operationName);
        };

        ErrorDialog.openError(shell, NLS.bind(Messages.ResultDisplayer_titleResults, operationName), messageText,
                multiStatus);
    }

    /*
     * Returns the message text and if available adds the string representation of the object the
     * message was created for
     */
    private String getMessageText(Message msg) {
        String text = msg.getText();
        List<ObjectProperty> invalidObjectProperties = msg.getInvalidObjectProperties();
        if (invalidObjectProperties == null || invalidObjectProperties.size() == 0) {
            return text;
        }
        text += ": "; //$NON-NLS-1$
        for (int i = 0; i < invalidObjectProperties.size(); i++) {
            text += invalidObjectProperties.get(i).getObject();
            if (i + 1 < invalidObjectProperties.size()) {
                text += "\n  "; //$NON-NLS-1$
            }
        }
        return text;
    }
}
