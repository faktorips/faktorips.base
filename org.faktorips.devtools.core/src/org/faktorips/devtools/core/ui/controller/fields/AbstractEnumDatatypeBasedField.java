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
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.util.ArgumentCheck;

/**
 * An abstract base class for fields dealing with enums. It
 * expects a Combo as GUI Control. Subclasses are reponsible for filling the
 * Combo. Therefor the protected initialize method can be utilized within the
 * implementation. If the provided Datatype is an enum datatype and supports names for the
 * enumeration values the names are displayed in the combo but the getValue()
 * and setValue() methods still expect the ids that identify the values. This
 * implementation doesn't adjust to changes of the values it represents.
 * Therefor the reinit() method has to be explicitly called.
 * 
 * @author Peter Erzberger
 */
public abstract class AbstractEnumDatatypeBasedField extends ComboField {

	private ValueDatatype datatype;

	private String[] ids;
	
	private String invalidValue;

	public AbstractEnumDatatypeBasedField(Combo combo, ValueDatatype datatype) {
		super(combo);
		ArgumentCheck.notNull(datatype);
		this.datatype = datatype;
	}

	/**
	 * Refills the combo box and tries to keep the current value if it is still
	 * in the range of possible values. If not, the first value will be selected.
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
	 * <code>Datatype</code> changes its values dynamically this edit
	 * field can adjust to the value changes by means of this method. The
	 * <code>initialized(String[], String[])</code> method is supposed to be
	 * used to set the values of the combo within implementations of
	 * <code>reInitInternal()</code>.
	 */
	protected abstract void reInitInternal();

	/**
	 * Initializes the combo either with the ids of the enumeration values or
	 * with the value names if the Datatype if it is an enum datatype and 
	 * supports names. The ids are kept
	 * during the life time of this EditField or until the reInit() method is
	 * called. They are used to by the <code>getValue()</code> method.
	 * Implementations of the <code>reInitInteral()</code> method need to call
	 * this method to initialize this edit field correctly.
	 */
	protected final void initialize(String[] ids, String[] names) {

		this.ids = ids;
		
		for (int i = 0; i < this.ids.length; i++) {
			this.ids[i] = (String)super.prepareObjectForSet(this.ids[i]);
		}
		
		String[] myNames = new String[names.length];
		for (int i = 0; i < names.length; i++) {
			myNames[i] = (String)super.prepareObjectForSet(names[i]);
		}

		if (myNames != null && myNames.length > 0) {
			setItems(myNames);
			return;
		}
		setItems(ids);
	}
	
	private void setItems(String[] items) {
		getCombo().setItems(items);
		if (invalidValue != null) {
			getCombo().add((String)super.prepareObjectForSet(getValueName(invalidValue)));
		}
	}

	/**
	 * Returns the ValueDatatype of this edit field.
	 */
	public ValueDatatype getDatatype() {
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
		
		if (getCombo().getSelectionIndex() >= ids.length) {
			// we have the invalid value selected...
			return invalidValue;
		}
		
		return super.prepareObjectForGet(ids[getCombo().getSelectionIndex()]);
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

		super.setValue(getValueName((String) newValue));
	}
	
	protected String getValueName(String id) {
		String noNullId = (String)super.prepareObjectForSet(id);
		if (datatype instanceof EnumDatatype && ((EnumDatatype)datatype).isSupportingNames()) {
			return ((EnumDatatype)datatype).getValueName(noNullId) + " (" + noNullId + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			return noNullId;
		}
	}

	/**
	 * Set the given value as aditional value which must not be contained in the underlying value set. 
	 * The given value is added to the values contained in the combo-box.
	 * 
	 * @param value The value to add (which means the id of the value).
	 */
	public void setInvalidValue(String value) {
		invalidValue = value;
	}
}
