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

import java.util.ArrayList;

import org.eclipse.swt.widgets.Combo;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.model.IEnumValueSet;
import org.faktorips.util.ArgumentCheck;

/**
 * An implementation of AbstractEnumDatatypeBasedFields that displays the values
 * of an EnumValueSets. If the EnumDatatype the EnumValueSet bases on supports
 * value names these are displayed instead of the value ids.
 * 
 * @author Peter Erzberger
 */
public class EnumValueSetField extends AbstractEnumDatatypeBasedField {

	private IEnumValueSet valueSet;

	/**
	 * Creates a new EnumValueSetField.
	 * 
	 * @param combo
	 *            the control of this EditField
	 * @param valueSet
	 *            the value set which is displayed by this edit field
	 * @param datatype
	 *            the datatype the value set bases on
	 */
	public EnumValueSetField(Combo combo, IEnumValueSet valueSet,
			EnumDatatype datatype) {
		super(combo, datatype);
		ArgumentCheck.notNull(valueSet, this);
		this.valueSet = valueSet;
		reInitInternal();
	}

	protected final void reInitInternal() {
		String[] ids = valueSet.getValues();

		ArrayList names = new ArrayList(ids.length);
		for (int i = 0; i < ids.length; i++) {
			if (getEnumDatatype().isSupportingNames()) {
				names.add(getEnumDatatype().getValueName(valueSet.getValue(i)));
			}
		}
		initialize(ids, (String[]) names.toArray(new String[names.size()]));
	}
}
