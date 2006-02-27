package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.IpsPartUIController;
import org.faktorips.devtools.core.ui.controls.TextButtonControl;

/**
 * Control to edit the values of an enum. A textfeld followed by a button is provided.
 * If the button is clicked, an special Editor for editing the enum values with support is opened.
 * 
 * @author Thorsten Guenther
 */
public class EnumValueSetControl extends TextButtonControl {

	IConfigElement configElement;
	Shell shell;
	IpsPartUIController controller; 
	
	public EnumValueSetControl(Composite parent, UIToolkit toolkit, IConfigElement configElement, Shell shell, IpsPartUIController controller) {
		super(parent, toolkit, "...", true, 15); //$NON-NLS-1$
		this.configElement = configElement;
		this.shell = shell;
		this.controller = controller;
		
		// lock input, values are not editable.
		super.getTextControl().setEnabled(false);
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void buttonClicked() {
		DefaultsAndRangesEditDialog dialog = new DefaultsAndRangesEditDialog(configElement, shell);
		if (dialog.open() == Dialog.OK) {
			super.getTextControl().setText(configElement.getValueSet().toString());
		}
		controller.updateUI();
		
	}

}
