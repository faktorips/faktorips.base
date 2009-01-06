/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
		String[] ids = getEnumDatatype().getAllValueIds(true);
		ArrayList items = new ArrayList(ids.length);
		for (int i = 0; i < ids.length; i++) { 
			items.add(super.getDisplayTextForValue(ids[i]));
		}
        String[] textToShow = (String[]) items.toArray(new String[items.size()]);
		initialize(ids, textToShow);
	}
	
	private EnumDatatype getEnumDatatype() {
		return (EnumDatatype)getDatatype();
	}
}
