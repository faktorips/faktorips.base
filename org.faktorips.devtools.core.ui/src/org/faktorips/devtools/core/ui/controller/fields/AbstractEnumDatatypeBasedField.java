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

import java.util.Arrays;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.swt.widgets.Combo;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.util.ArgumentCheck;

/**
 * An abstract base class for fields dealing with enums. It expects a Combo as GUI Control.
 * Subclasses are reponsible for filling the Combo. Therefore the protected initialize method can be
 * utilized within the implementation. The combo's items are filled not directly with the ids of the
 * enum values but the IpsPreferences#formatValue(..) method is used to get the text to display. But
 * the getValue() and setValue() methods still expect the ids that identify the values. This
 * implementation doesn't adjust to changes of the values it represents. Therefore the reinit()
 * method has to be explicitly called.
 * 
 * @see IpsPreferences#formatValue(EnumDatatype, String)
 * @see IpsPreferences#getEnumTypeDisplay()
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
     * Refills the combo box and tries to keep the current value if it is still in the range of
     * possible values. If not, the first value will be selected.
     */
    public final void reInit() {
        boolean prevValidSelection = getCombo().getSelectionIndex() != -1;
        String currentValue = (String)getValue();
        reInitInternal();
        if (prevValidSelection) {
            try {
                setValue(currentValue, false);
                return;
            } catch (Exception e) {
                // ignore exception, select first element instead if available
            }
            if (ids != null && ids.length > 0) {
                setValue(ids[0]);
            }
        }
    }

    /**
     * Implementations of this edit field should provide a reinitialization of the field within this
     * method. In cases where the provided <code>Datatype</code> changes its values dynamically this
     * edit field can adjust to the value changes by means of this method. The
     * <code>initialized(String[], String[])</code> method is supposed to be used to set the values
     * of the combo within implementations of <code>reInitInternal()</code>.
     */
    protected abstract void reInitInternal();

    /**
     * Initializes the combo either with the ids of the enumeration values or with the value names
     * if the Datatype if it is an enum datatype and supports names. The ids are kept during the
     * life time of this EditField or until the reInit() method is called. They are used to by the
     * <code>getValue()</code> method. Implementations of the <code>reInitInteral()</code> method
     * need to call this method to initialize this edit field correctly.
     */
    protected final void initialize(String[] ids, String[] texts) {
        this.ids = ids;
        for (int i = 0; i < this.ids.length; i++) {
            this.ids[i] = (String)super.prepareObjectForSet(this.ids[i]);
        }
        if (texts == null || texts.length == 0) {
            setItems(ids);
            return;
        }
        String[] textsToShow = new String[texts.length];
        for (int i = 0; i < textsToShow.length; i++) {
            textsToShow[i] = (String)super.prepareObjectForSet(texts[i]);
        }
        setItems(textsToShow);
        return;
    }

    private void setItems(String[] items) {
        getCombo().setItems(items);
        if (invalidValue != null) {
            // there is an invalid value in the list, add this value to the items if the invalid
            // value is currently not in the list
            String valueToAdd = (String)super.prepareObjectForSet(getDisplayTextForValue(invalidValue));
            if (!Arrays.asList(getCombo().getItems()).contains(valueToAdd)) {
                getCombo().add(valueToAdd);
            }
        }
    }

    /**
     * Returns the ValueDatatype of this edit field.
     */
    public ValueDatatype getDatatype() {
        return datatype;
    }

    /**
     * Returns the value of the currently selected index (which is the id of the enumeration value).
     * Returns null if no value is selected.
     */
    public Object parseContent() {
        int selectedIndex = getCombo().getSelectionIndex();
        if (selectedIndex == -1) {
            return null;
        }

        if (selectedIndex >= ids.length) {
            // we have the invalid value selected...
            return invalidValue;
        }

        return super.prepareObjectForGet(ids[selectedIndex]);
    }

    /**
     * Sets (select) the value that is to display in the Control of this EditField. If the value are
     * not contained in the EnumValueSet then the value will be added and selected otherwise only
     * selected.
     */
    public void setValue(Object newValue) {
        boolean isParsable = false;
        newValue = (String)prepareObjectForSet(newValue);

        isParsable = datatype.isParsable((String)newValue);
        if (isParsable) {
            super.setValue(getDisplayTextForValue((String)newValue));
        }

        /*
         * check if the given value was set - if not so, we try to set an invalid value. But because
         * this is a field for a combo, an invalid value can only be set using this mehtod if it
         * valid before (for example the value-set changed). So we can add the value as invalid
         * value.
         */
        if (!ObjectUtils.equals(getValue(), newValue)) {
            setInvalidValue((String)newValue);
            // because this is an invalid value (not in enum value set, we
            // must reinit the item in the drop down, only so we can select the invalid value
            reInitInternal();
            super.setValue(newValue);
        }
    }

    /**
     * Returns the text to be displayed in the combo for the given value id. The ips property @see
     * {@link IpsPreferences#ENUM_TYPE_DISPLAY} specifies the format.
     */
    public String getDisplayTextForValue(String id) {
        return IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormatter().formatValue(datatype, id);
    }

    /**
     * Set the given value as aditional value which must not be contained in the underlying value
     * set. The given value is added to the values contained in the combo-box.
     * 
     * @param value The value to add (which means the id of the value).
     */
    public void setInvalidValue(String value) {
        invalidValue = value;
    }

    /**
     * For tests only!
     */
    protected String getInvalidValue() {
        return invalidValue;
    }
    
}
