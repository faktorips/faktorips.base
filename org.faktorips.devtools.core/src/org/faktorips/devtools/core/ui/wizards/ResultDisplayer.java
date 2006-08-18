/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.util.message.MessageList;

/**
 * This class can be used to display a <code>MessageList</code> by using
 * the <code>ErrorDialog</code> of Eclipse. 
 * Because often it is neccessary to display results produced with a thread 
 * which is not the UI-thread, this class implements <code>Runnable</code>,
 * so it can be used simply in these other threads by calling
 * <code>getShell().getDisplay().syncExec(new ResultDisplayer(getShell(), messageList));</code>
 * 
 * @author Thorsten Guenther
 */
public class ResultDisplayer implements Runnable {
	/**
	 * The list of messages to display
	 */
	private MessageList messageList;
	
	/**
	 * The shell to open the result dialog within
	 */
	private Shell shell;
	
	/**
	 * Creates a new ResultDisplayer.
	 * 
	 * @param shell The shell to open the result dialog within.
	 * @param messageList The list of messages to display.
	 */
	public ResultDisplayer(Shell shell, MessageList messageList) {
		this.messageList = messageList;
		this.shell = shell;
	}
	
	/**
	 * This method has to be called in the UI-thread. If it is not shure that 
	 * this method allways will be executed in the UI-thread, use
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
		int image;
		String header;
		if (messageList.containsErrorMsg()) {
			image = MessageDialog.ERROR;
			header = "Export aborted. ";
		}
		else {
			image = MessageDialog.WARNING;
			header = "Export successfull with warnings: ";
		}

		// TODO use ErrorDialog here and display messageList as MultiStatus
		MessageDialog dialog = new MessageDialog(shell, "Results of export", null, header + messageList.toString(), image, new String[] {"OK"}, 0);
		dialog.open();
	}
}