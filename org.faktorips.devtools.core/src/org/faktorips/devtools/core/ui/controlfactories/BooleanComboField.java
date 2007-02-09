package org.faktorips.devtools.core.ui.controlfactories;

import org.eclipse.swt.widgets.Combo;
import org.faktorips.devtools.core.ui.controller.fields.ComboField;
import org.faktorips.util.ArgumentCheck;

/**
 * Field to edit values of type boolean (=instances of the BooleanDatatype). E.g. this field can be used
 * to edit an attributes default value if the attributes datatype is boolean.
 * This is NOT a general purpose field for booleans that are part the faktor ips meta model.
 * 
 * @author Joerg Ortmann
 */
public class BooleanComboField extends ComboField {
	
	private String trueRepresentation;
	private String falseRepresentation;

	/**
	 * @param combo
	 */
	public BooleanComboField(Combo combo, String trueRepresentation, String falseRepresentation) {
		super(combo);
		ArgumentCheck.notNull(trueRepresentation);
		ArgumentCheck.notNull(falseRepresentation);
		this.trueRepresentation = trueRepresentation;
		this.falseRepresentation = falseRepresentation;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object parseContent() {
		String s = (String)super.getText();
		if (s==null) {
			return null;
		} else if (s.equals(trueRepresentation)) {
			return Boolean.TRUE.toString();
		} else if (s.equals(falseRepresentation)) {
				return Boolean.FALSE.toString();
		}
		throw new RuntimeException("Unknown value " + s); //$NON-NLS-1$
	}
    
	/**
	 * {@inheritDoc}
	 */
	public void setText(String newValue) {
		if (newValue==null) {
			super.setText(newValue);
			return;
		}
		if (newValue.equals(trueRepresentation)) {
			super.setText(trueRepresentation);
		} else {
			super.setText(falseRepresentation);
		}
    }
}