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

package org.faktorips.devtools.core.ui.actions;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;

/**
 * Action that allows to modify the working date (which is a part of the ips preferences)
 * with a simple click to a button in the toolbar. The modification made by the user using
 * this action has the same effect as if the user changes the working date on the
 * ips preferences page.
 * 
 * @author Thorsten Guenther
 */
public class ChangeWorkingDateAction implements IWorkbenchWindowActionDelegate {

	private Shell shell;
	
	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
		// nothing to do
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(IWorkbenchWindow window) {
		shell = window.getShell();
	}

	/**
	 * {@inheritDoc}
	 */
	public void run(IAction action) {
		InputDialog dialog = new InputDialog(shell, "Change WorkingDate", "Please enter the new working date as YYYY-MM-DD.", IpsPlugin.getDefault().getIpsPreferences().getFormattedWorkingDate(), new Validator());

		if (dialog.open() == InputDialog.OK) {
			try {
				DateFormat format = IpsPlugin.getDefault().getIpsPreferences().getValidFromFormat();
				Date newDate = format.parse(dialog.getValue());
				IpsPreferences.setWorkingDate(new GregorianCalendar(1900 + newDate.getYear(), newDate.getMonth(), newDate.getDate()));
			} catch (ParseException e) {
				// should not happen if validator works correct.
				IpsPlugin.log(e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		// nothing to do
	}

	/**
	 * Validates the input to be a valid date.
	 * 
	 * @author Thorsten Guenther
	 */
	private class Validator implements IInputValidator {
		DateFormat format;
		
		public Validator() {
			format = IpsPlugin.getDefault().getIpsPreferences().getValidFromFormat();
		}
		/**
		 * {@inheritDoc}
		 */
		public String isValid(String newText) {
			try {
				format.parse(newText);
			} catch (ParseException e) {
				String pattern;
				if (format instanceof SimpleDateFormat) {
					pattern = ((SimpleDateFormat)format).toLocalizedPattern();
				}
				else {
					pattern = "to be parsed as date.";
				}
				String msg = NLS.bind("The value is not in the form {0}. ", pattern);
				return msg;
			}

			return null;
		}
		
	}
}
