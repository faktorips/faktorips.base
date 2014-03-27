/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.widgets.Combo;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.ui.Messages;
import org.faktorips.util.ArgumentCheck;

/**
 * An implementation of AbstractEnumDatatypeBasedField that displays the values of an EnumValueSet.
 * If the EnumDatatype the EnumValueSet is based on, supports value names these are displayed
 * instead of the value ids.
 * 
 * @author Peter Erzberger
 */
public class EnumValueSetField extends AbstractEnumDatatypeBasedField {

    private IValueSetOwner valueSetOwner;

    /**
     * Creates a new EnumValueSetField.
     * 
     * @param combo the control of this EditField
     * @param valueSet the value set which is displayed by this edit field
     * @param datatype the datatype the value set bases on
     */
    public EnumValueSetField(Combo combo, IEnumValueSet valueSet, ValueDatatype datatype) {
        super(combo, datatype);
        ArgumentCheck.notNull(valueSet, this);
        this.valueSetOwner = valueSet.getValueSetOwner();
        reInitInternal();
    }

    @Override
    protected List<String> getDatatypeValueIds() {
        IValueSet newValueSet = valueSetOwner.getValueSet();
        return fillList(newValueSet);
    }

    private List<String> fillList(IValueSet newValueSet) {
        List<String> ids = new ArrayList<String>();
        if (newValueSet instanceof IEnumValueSet) {
            IEnumValueSet newEnumValueSet = (IEnumValueSet)newValueSet;
            ids.addAll(Arrays.asList(newEnumValueSet.getValues()));
        }
        if (!ids.contains(null) && isDefaultValueField()) {
            // For default values there is always the option of 'no default' value
            ids.add(null);
        }
        if (ids.isEmpty()) {
            ids.add(null);
        }
        return ids;
    }

    private boolean isDefaultValueField() {
        return valueSetOwner instanceof IConfigElement;
    }

    @Override
    public String getDisplayTextForValue(String id) {
        if (id == null && isDefaultValueField()) {
            return Messages.DefaultValueRepresentation_Combobox;
        } else {
            return super.getDisplayTextForValue(id);
        }
    }

}
