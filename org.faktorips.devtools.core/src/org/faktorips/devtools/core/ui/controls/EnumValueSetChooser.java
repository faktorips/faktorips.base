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

package org.faktorips.devtools.core.ui.controls;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.IEnumValueSet;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.IpsPartUIController;

/**
 * Control to select values from one enum value set to be added to another.
 * 
 * @author Thorsten Guenther
 */
public class EnumValueSetChooser extends ListChooser {

	private IEnumValueSet target;
	private IpsPartUIController uiController;
	
	/**
	 * @param parent The parent control
	 * @param toolkit The toolkit to make creation of UI easier.
	 * @param source The source-valueser
	 * @param target The target-valueset (the one to add the values to).
	 * @param uiController The controller to notify upon change
	 */
	public EnumValueSetChooser(Composite parent, UIToolkit toolkit,
			IEnumValueSet source, IEnumValueSet target,
			IpsPartUIController uiController) {
		super(parent, toolkit, target.getValuesNotContained(source), target.getValues());
		this.target = target;
		this.uiController = uiController;
	}

	/**
	 * {@inheritDoc}
	 */
	public void valuesAdded(String[] values) {
		for (int i = 0; i < values.length; i++) {
			target.addValue(values[i]);
		}
		uiController.updateUI();
	}

	/**
	 * {@inheritDoc}
	 */
	public void valuesRemoved(String[] values) {
		for (int i = 0; i < values.length; i++) {
			target.removeValue(values[i]);
		}
		uiController.updateUI();
	}

	/**
	 * {@inheritDoc}
	 */
	public void valueMoved(String value, int index, boolean up) {
		int newIndex;
		if (up) {
			newIndex = index - 1;
		} else {
			newIndex = index + 1;
		}
		String old = target.getValue(newIndex);
		target.setValue(newIndex, value);
		target.setValue(index, old);

	}	
}
