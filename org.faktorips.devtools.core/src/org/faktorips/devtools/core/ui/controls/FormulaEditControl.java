/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.productcmpt.FormulaEditDialog;

/**
 * Control to edit the value of an formula. A textfeld followed by a button is provided.
 * If the button is clicked, an special Editor for editing the formula with support is opened.
 * 
 * @author Thorsten Guenther
 */
public class FormulaEditControl extends TextButtonControl {

	IConfigElement configElement;
	Shell shell;
	
	public FormulaEditControl(Composite parent, UIToolkit toolkit, IConfigElement configElement, Shell shell) {
		super(parent, toolkit, "...", true, 15); //$NON-NLS-1$
		this.configElement = configElement;
		this.shell = shell;
	}
	
	protected void buttonClicked() {
		try {
			new FormulaEditDialog(configElement, shell, !super.getTextControl().isEnabled()).open();
		} catch (CoreException e) {
			IpsPlugin.logAndShowErrorDialog(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setEnabled(boolean enabled) {
		super.getTextControl().setEnabled(enabled);
	}
	
}
