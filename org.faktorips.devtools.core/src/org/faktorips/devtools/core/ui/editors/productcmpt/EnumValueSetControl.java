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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.ui.IDataChangeableReadWriteAccess;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.IpsPartUIController;
import org.faktorips.devtools.core.ui.controls.TextButtonControl;
import org.faktorips.util.memento.Memento;

/**
 * Control to edit the values of an enum. A textfeld followed by a button is provided.
 * If the button is clicked, an special Editor for editing the enum values with support is opened.
 * 
 * @author Thorsten Guenther
 */
public class EnumValueSetControl extends TextButtonControl implements IDataChangeableReadWriteAccess {

    /**
	 * The config element which is based on the enum value set to modify.
	 */
	private IConfigElement configElement;
	
	/**
	 * The shell to details dialog within.
	 */
	private Shell shell;
	
	/**
	 * The controller to notify if detail modifiation has finished.
	 */
	private IpsPartUIController controller; 
	
	/**
	 * The state of the config element before opening the detail edit dialog. Used to handle the
	 * cancel button properly
	 */
	private Memento state;
	
	/**
	 * The state of the ips source file before opening the detail edit dialog. Used to handle the
	 * cancel button properly
	 */
	private boolean dirty;
	
    private boolean dataChangeable;

	
	/**
	 * Creates a new control to edit an enum value.
	 * 
	 * @param parent The parent composite to add this control to.
	 * @param toolkit The toolkit to use for the creation of the controls.
	 * @param configElement The config element which is based on the enum value set to modify.
	 * @param shell The shell to open the details edit dialog within.
	 * @param controller The controller to notify uppon changes to update the ui.
	 */
	public EnumValueSetControl(Composite parent, UIToolkit toolkit, IConfigElement configElement, Shell shell, IpsPartUIController controller) {
		super(parent, toolkit, "...", true, 15); //$NON-NLS-1$
		this.configElement = configElement;
		this.shell = shell;
		this.controller = controller;
        getTextControl().setEditable(false);
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void buttonClicked() {
		preserveState();
		DefaultsAndRangesEditDialog dialog = new DefaultsAndRangesEditDialog(configElement, shell, !dataChangeable);
        dialog.setDataChangeable(isDataChangeable());
		if (dialog.open() == Dialog.OK) {
			super.getTextControl().setText(configElement.getValueSet().toShortString());
			controller.updateUI();
		} 
		else {
			resetState();
		}		
	}
	
	/**
	 * Stores the current state of the config element and the underlying sourcefile for 
	 * later recovery.
	 */
	private void preserveState() {
		dirty = configElement.getIpsObject().getIpsSrcFile().isDirty();
		state = configElement.newMemento();
	}
	
	/**
	 * Recovers an old state preserved by calling preserveState. If no state was
	 * preserved before calling this method, nothing is done.
	 */
	private void resetState() {
		if (state == null) {
			// no state was preserved, so dont do anything.
			return;
		}
		
		configElement.setState(state);
		if (!dirty) {
			configElement.getIpsObject().getIpsSrcFile().markAsClean();
		}
	}
	
    /**
     * {@inheritDoc}
     */
    public void setDataChangeable(boolean changeable) {
        this.dataChangeable = changeable;
        setButtonEnabled(changeable);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDataChangeable() {
        return dataChangeable;
    }
}
