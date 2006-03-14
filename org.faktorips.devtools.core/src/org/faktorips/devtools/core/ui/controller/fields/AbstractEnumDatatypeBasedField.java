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

import org.eclipse.swt.widgets.Combo;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.util.ArgumentCheck;

/**
 * An abstract base class for EnumDatatypeField and EnumValueSetField. It
 * expects a Combo as GUI Control. Subclasses are reponsible for filling the
 * Combo. Therefor the protected initialize method can be utilized within the
 * implementation. If the provided EnumDatatype supports names for the
 * enumeration values the names are displayed in the combo but the getValue()
 * and setValue() methods still expect the ids that identify the values. This
 * implementation doesn't adjust to changes of the values it represents.
 * Therefor the reinit() method has to be explicitly called.
 * 
 * @author Peter Erzberger
 */
public abstract class AbstractEnumDatatypeBasedField extends ComboField {

	private EnumDatatype datatype;

	private String[] ids;

	public AbstractEnumDatatypeBasedField(Combo combo, EnumDatatype datatype) {
		super(combo);
		ArgumentCheck.notNull(datatype);
		this.datatype = datatype;
	}

	/**
	 * Refills the combo box and tries to keep the current value if it is still
	 * in the range off possible values if not the first value will be selected.
	 */
	public final void reInit() {
		String currentValue = (String) getValue();
		reInitInternal();
		try {
			setValue(currentValue, false);
		} catch (Exception e) {
			if (ids != null && ids.length > 0) {
				setValue(ids[0]);
			}
		}
	}

	/**
	 * Implementations of this edit field should provide a reinitialization of
	 * the field within this method. In cases where the provided
	 * <code>EnumDatatype</code> changes its values dynamically this edit
	 * field can adjust to the value changes by means of this method. The
	 * <code>initialized(String[], String[])</code> method is supposed to be
	 * used to set the values of the combo within implementations of
	 * <code>reInitInternal()</code>.
	 */
	protected abstract void reInitInternal();

	/**
	 * Initializes the combo either with the ids of the enumeration values or
	 * with the value names if the EnumDatatype supports names. The ids are kept
	 * during the life time of this EditField or until the reInit() method is
	 * called. They are used to by the <code>getValue()</code> method.
	 * Implementations of the <code>reInitInteral()</code> method need to call
	 * this method to initialize this edit field correctly.
	 */
	protected final void initialize(String[] ids, String[] names) {

		this.ids = ids;
		if (names != null && names.length > 0) {
			getCombo().setItems(names);
			return;
		}
		getCombo().setItems(ids);
	}

	/**
	 * Returns the EnumDatatype of this edit field.
	 */
	public EnumDatatype getEnumDatatype() {
		return datatype;
	}

	/**
	 * Returns the value of the currently selected index (which is the id of the
	 * enumeration value). Returns null if no value is selected.
	 */
	public Object getValue() {

		if (getCombo().getSelectionIndex() == -1) {
			return null;
		}
		return ids[getCombo().getSelectionIndex()];
	}

	/**
	 * Sets the value that is to display in the Control of this EditField. It
	 * expectes the values that are contained in the EnumValueSet this EditField
	 * was initialized with. Other values are ignored.
	 */
	public void setValue(Object newValue) {

		if (!datatype.isParsable((String) newValue)) {
			return;
		}

		if (datatype.isSupportingNames()) {
			super.setValue(getValueName((String) newValue));
			return;
		}
		super.setValue(newValue);
	}
	
	protected String getValueName(String id) {
		return getEnumDatatype().getValueName(id) + " (" + id + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
