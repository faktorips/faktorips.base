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

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.ValueSet;
import org.faktorips.devtools.core.model.IEnumValueSet;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.EnumDatatypeField;
import org.faktorips.devtools.core.ui.controller.fields.EnumValueSetField;

/**
 * A control factory for the datytpes enumeration.
 * 
 * @author Joerg Ortmann
 */
public class EnumDatatypeControlFactory extends ValueDatatypeControlFactory {

	public EnumDatatypeControlFactory() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isFactoryFor(ValueDatatype datatype) {
		return datatype instanceof EnumDatatype;
	}

	/**
	 * {@inheritDoc}
	 */
	public EditField createEditField(UIToolkit toolkit, Composite parent,
			ValueDatatype datatype, ValueSet valueSet) {

		Combo combo = toolkit.createCombo(parent);
		if (valueSet instanceof IEnumValueSet) {
			return new EnumValueSetField(combo, (IEnumValueSet)valueSet, datatype);
		} else {
			return new EnumDatatypeField(combo, (EnumDatatype)datatype);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Control createControl(UIToolkit toolkit, Composite parent,
			ValueDatatype datatype, ValueSet valueSet) {

		return createEditField(toolkit, parent, datatype, valueSet).getControl();
	}

    /**
     * {@inheritDoc}
     */
    public CellEditor createCellEditor(Composite parent, ValueSet valueSet, int columnIndex) {
        if (valueSet instanceof IEnumValueSet) {
            return new ComboBoxCellEditor(parent, ((IEnumValueSet)valueSet).getValues());
        }
        throw new RuntimeException("Not supported values set " + valueSet.getClass().getName());
    }
}
