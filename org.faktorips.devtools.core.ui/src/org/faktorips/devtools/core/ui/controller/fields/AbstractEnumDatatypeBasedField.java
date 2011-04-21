/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.swt.widgets.Combo;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
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
 * @see IpsPreferences#getEnumTypeDisplay()
 * 
 * @author Peter Erzberger
 */
public abstract class AbstractEnumDatatypeBasedField extends StringValueComboField {

    private ValueDatatype datatype;

    private List<String> ids;

    private String invalidValue;

    public AbstractEnumDatatypeBasedField(Combo combo, ValueDatatype datatype) {
        super(combo);
        ArgumentCheck.notNull(datatype);
        this.datatype = datatype;
    }

    protected abstract List<String> getDatatypeValueIds();

    /**
     * Refills the combo box and tries to keep the current value if it is still in the range of
     * possible values. If not, the first value will be selected.
     */
    public final void reInit() {
        boolean prevValidSelection = getCombo().getSelectionIndex() != -1;
        String currentValue = getValue();
        reInitInternal();
        if (prevValidSelection) {
            try {
                setValue(currentValue, false);
                return;
            } catch (Exception e) {
                // TODO catch Exception needs to be documented properly or specialized
                // ignore exception, select first element instead if available
            }
            if (ids != null && ids.size() > 0) {
                setValue(ids.get(0));
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
    protected final void reInitInternal() {
        ids = getDatatypeValueIds();
        if (ids == null) {
            ids = new ArrayList<String>();
        }
        ArrayList<String> items = new ArrayList<String>(ids.size());
        for (String id : ids) {
            String text = getDisplayTextForValue(id);
            if (text == null) {
                throw new RuntimeException(
                        "Inconsistant state during initialization of this edit field. The method getDisplayTextForValue(String) returned null for the provided id=" //$NON-NLS-1$
                                + id);
            }
            items.add(text);
        }
        setItems(items.toArray(new String[items.size()]));

    }

    private void setItems(String[] items) {
        getCombo().setItems(items);
        if (invalidValue != null) {
            // there is an invalid value in the list, add this value to the items if the invalid
            // value is currently not in the list
            String valueToAdd = getDisplayTextForValue(invalidValue);
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
    @Override
    public String parseContent() {
        int selectedIndex = getCombo().getSelectionIndex();
        if (selectedIndex == -1) {
            return null;
        }

        if (selectedIndex >= ids.size()) {
            // we have the invalid value selected...
            return invalidValue;
        }

        return ids.get(selectedIndex);
    }

    /**
     * Sets (select) the value that is to display in the Control of this EditField. If the value are
     * not contained in the EnumValueSet then the value will be added and selected otherwise only
     * selected.
     */
    @Override
    public void setValue(String newValue) {
        if (datatype.isParsable(newValue)) {
            setText(getDisplayTextForValue(newValue));
        }

        /*
         * check if the given value was set - if not so, we try to set an invalid value. But because
         * this is a field for a combo, an invalid value can only be set using this method if it
         * valid before (for example the value-set changed). So we can add the value as invalid
         * value.
         */
        if (!ObjectUtils.equals(getValue(), newValue)) {
            setInvalidValue(newValue);
            // because this is an invalid value (not in enum value set, we
            // must reinit the item in the drop down, only so we can select the invalid value
            reInitInternal();
            setText(getDisplayTextForValue(newValue));
        }
    }

    /**
     * Returns the text to be displayed in the combo for the given value id. The ips property @see
     * {@link IpsPreferences#ENUM_TYPE_DISPLAY} specifies the format.
     */
    public String getDisplayTextForValue(String id) {
        return IpsUIPlugin.getDefault().getDatatypeFormatter().formatValue(datatype, id);
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
    public String getInvalidValue() {
        return invalidValue;
    }

}
