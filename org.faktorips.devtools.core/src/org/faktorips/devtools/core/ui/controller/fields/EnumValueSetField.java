package org.faktorips.devtools.core.ui.controller.fields;

import java.util.ArrayList;

import org.eclipse.swt.widgets.Combo;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.model.EnumValueSet;
import org.faktorips.devtools.core.model.ValueSet;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.util.ArgumentCheck;

/**
 * An implementation of AbstractEnumDatatypeBasedFields that displays the values
 * of an EnumValueSets. If the EnumDatatype the EnumValueSet bases on supports
 * value names these are displayed instead of the value ids.
 * 
 * @author Peter Erzberger
 */
public class EnumValueSetField extends AbstractEnumDatatypeBasedField {

	private IConfigElement element;
	private IAttribute attribute;

	/**
	 * Creates a new EnumValueSetField.
	 * 
	 * @param combo the control of this EditField
	 * @param element the config element defining (in combination with the attribute) the value set to display.
	 * @param attribute the attribute defining (in combination with the config element) the value set to display
	 * @param datatype the datatype the value set bases on (used for name-id-mapping). Can be null, then no names
	 * are used, only the ids given in the value set. 
	 */
	public EnumValueSetField(Combo combo, IConfigElement element, IAttribute attribute,
			EnumDatatype datatype) {
		super(combo, datatype);
		ArgumentCheck.notNull(element, this);
		
		this.element = element;
		this.attribute = attribute;
		
		reInitInternal();
	}

	protected final void reInitInternal() {
		ValueSet valueSet = element.getValueSet();
		if (attribute != null && valueSet.isAllValues()) {
			valueSet = attribute.getValueSet();
		}

		EnumValueSet enumValueSet = (EnumValueSet)valueSet;

		String[] ids = enumValueSet.getValues();

		ArrayList names = new ArrayList();
		if (getEnumDatatype().isSupportingNames()) {
			for (int i = 0; i < ids.length; i++) {
				names.add(getEnumDatatype().getValueName(enumValueSet.getValue(i)));
			}
		}
	
		initialize(ids, (String[]) names.toArray(new String[names.size()]));
	}
}
