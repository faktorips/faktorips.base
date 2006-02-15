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
