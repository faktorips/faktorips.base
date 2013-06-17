/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.formulalibrary.ui.editors;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * The <tt>MessageComposite</tt> is a <tt>Composite</tt> for showing text of a <tt>Message</tt>
 * including there <tt>Image</tt>.
 * <p>
 * This can be used to show <tt>ERROR</tt>, <tt>INFO</tt> and <tt>WARNING</tt> messages.
 * 
 * @author hbaagil
 */
public class MessageComposite extends Composite {

    private Image messageImage;
    private Text messageText;
    private Label messageLabel;
    private Message message;

    /**
     * Create a new <tt>MessageComposite</tt>.
     * 
     * @param parent The parent <tt>Comosite</tt> of this.
     */
    public MessageComposite(Composite parent) {
        super(parent, SWT.FILL);
        GridLayout gridLayout = new GridLayout(2, false);
        this.setLayout(gridLayout);
        this.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        createComposite();
    }

    private void createComposite() {
        createMessageLabel();
        createGridDataForMessageLabel();
        createMessageText();
        createGridDataForMessageText();
    }

    private void createMessageLabel() {
        messageLabel = new Label(this, SWT.NONE);
        messageLabel.setVisible(true);
    }

    private void createGridDataForMessageLabel() {
        GridData gridDataForMessageLabel = new GridData(SWT.FILL, SWT.BEGINNING, false, false);
        gridDataForMessageLabel.widthHint = 16;
        messageLabel.setLayoutData(gridDataForMessageLabel);
    }

    private void createMessageText() {
        messageText = new Text(this, SWT.MULTI | SWT.WRAP);
        messageText.setBackground(getBackground());
        messageText.setEditable(false);
    }

    private void createGridDataForMessageText() {
        GridData gridDataForMessageText = new GridData(SWT.FILL, SWT.FILL, true, false);
        messageText.setLayoutData(gridDataForMessageText);
    }

    /**
     * Sets <tt>Message</tt> from <tt>messageList</tt> with the highest severity to <tt>message</tt>
     * . If <tt>messageList</tt> is empty, <tt>message</tt> will be set to a <tt>Message</tt> with
     * empty code and text, and 0 severity.
     * 
     * @param messageList The <tt>MessageList</tt> to be analyzed.
     */
    public void setMessage(MessageList messageList) {
        if (!messageList.isEmpty()) {
            message = messageList.getMessageWithHighestSeverity();
        } else {
            message = new Message(StringUtils.EMPTY, StringUtils.EMPTY, 0);
        }
        refreshUi();
    }

    /**
     * Refreshing the UI by setting message text and message label.
     */
    public void refreshUi() {
        if (message != null) {
            if (!messageText.isDisposed() && !messageLabel.isDisposed()) {
                messageText.setText(message.getText());
                createErrorMessageImage(message, message.getSeverity());
                messageLabel.setImage(messageImage);
                getParent().layout();
            }
        }
    }

    /**
     * Get <tt>Message</tt> of this.
     * 
     * @return message
     */
    public Message getMessage() {
        return message;
    }

    private void createErrorMessageImage(Message message, int severity) {
        messageImage = null;
        if (message.getText() != null) {
            switch (severity) {
                case Message.NONE:
                    break;
                case Message.ERROR:
                    messageImage = JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_ERROR);
                    break;
                case Message.INFO:
                    messageImage = JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_INFO);
                    break;
                case Message.WARNING:
                    messageImage = JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_WARNING);
                    break;
                default:
                    break;
            }
        }
    }
}
