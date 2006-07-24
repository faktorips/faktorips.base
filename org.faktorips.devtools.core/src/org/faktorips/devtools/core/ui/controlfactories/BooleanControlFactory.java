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

package org.faktorips.devtools.core.ui.controlfactories;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.ValueSet;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.ComboField;
import org.faktorips.util.ArgumentCheck;

/**
 * A control factory for the datytpes boolean and primitve boolean.
 * 
 * @author Joerg Ortmann
 */
public class BooleanControlFactory extends ValueDatatypeControlFactory {

	public BooleanControlFactory() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isFactoryFor(ValueDatatype datatype) {
		return Datatype.BOOLEAN.equals(datatype) || Datatype.PRIMITIVE_BOOLEAN.equals(datatype);
	}

	/**
	 * {@inheritDoc}
	 */
	public EditField createEditField(UIToolkit toolkit, Composite parent, ValueDatatype datatype, ValueSet valueSet) {
		return new BooleanComboField((Combo)createControl(toolkit, parent, datatype, valueSet), "Yes", "No");

	}

	/**
	 * {@inheritDoc}
	 */
	public Control createControl(UIToolkit toolkit, Composite parent, ValueDatatype datatype, ValueSet valueSet) {
		return toolkit.createComboForBoolean(parent, !datatype.isPrimitive(), "Yes", "No");
	}

	/**
	 * Inner class of the enhanced combo field control.
	 * 
	 * @author Joerg Ortmann
	 */
	class BooleanComboField extends ComboField {
		
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
		public Object getValue() {
			String s = (String)super.getValue();
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
		public void setValue(Object newValue) {
			if (newValue==null) {
				super.setValue(newValue);
				return;
			}
			boolean bool = Boolean.valueOf((String)newValue).booleanValue();
			if (bool) {
				super.setValue(trueRepresentation);
			} else {
				super.setValue(falseRepresentation);
			}
		}	
	}
}
