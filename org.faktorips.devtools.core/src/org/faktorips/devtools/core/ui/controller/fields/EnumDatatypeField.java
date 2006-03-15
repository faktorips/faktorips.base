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

/**
 * An implementation of AbstractEnumDatatypeBasedField that displays the values
 * of an EnumDatatype. If the EnumDatatype supports value names these will be
 * displayed instead of the value ids.
 * 
 * @author kuntz
 * 
 */
public class EnumDatatypeField extends AbstractEnumDatatypeBasedField {

	public EnumDatatypeField(Combo combo, EnumDatatype datatype) {
		super(combo, datatype);
		reInitInternal();
	}

	/**
	 * {@inheritDoc}
	 */
	protected final void reInitInternal() {

		String[] ids = getEnumDatatype().getAllValueIds();
		String[] names = null;
		if (getEnumDatatype().isSupportingNames()) {

			ArrayList items = new ArrayList(ids.length);
			for (int i = 0; i < ids.length; i++) {
				items.add(getEnumDatatype().getValueName(ids[i]));
			}
			names = (String[]) items.toArray(new String[items.size()]);
		}
		initialize(ids, names);
	}
	
	private EnumDatatype getEnumDatatype() {
		return (EnumDatatype)getDatatype();
	}
}
