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

package org.faktorips.devtools.core.ui.controller.fields;

import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;

/**
 * Textfield to represent and edit nullable values.
 * 
 * @author Thorsten Guenther
 */
public class ValueTextField extends TextField {


	/**
	 * {@inheritDoc}
	 */
	public ValueTextField(Text text) {
		super(text);
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getValue() {
		String s = (String)super.getValue();
		if (s.equals(IpsPlugin.getDefault().getIpsPreferences().getNullPresentation())) {
			return null;
		}
		return s;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValue(Object newValue) {
		if (newValue==null) {
			super.setValue(IpsPlugin.getDefault().getIpsPreferences().getNullPresentation());
		} else {
			super.setValue(newValue);
		}
	}	
}
