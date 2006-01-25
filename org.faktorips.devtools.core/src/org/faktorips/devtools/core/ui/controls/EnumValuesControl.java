package org.faktorips.devtools.core.ui.controls;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.productcmpt.PolicyAttributeEditDialog;

/**
 * Control to edit the values of an enum. A textfeld followed by a button is provided.
 * If the button is clicked, an special Editor for editing the enum values with support is opened.
 * 
 * @author Thorsten Guenther
 */
public class EnumValuesControl extends TextButtonControl {

	IConfigElement configElement;
	Shell shell;
	
	public EnumValuesControl(Composite parent, UIToolkit toolkit, IConfigElement configElement, Shell shell) {
		super(parent, toolkit, "...", true, 15);
		this.configElement = configElement;
		this.shell = shell;
	}
	
	protected void buttonClicked() {
		new PolicyAttributeEditDialog(configElement, shell).open();
	}

}
