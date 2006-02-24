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
}
