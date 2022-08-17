/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls.chooser;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.controls.valuesets.IValueSetEditControl;
import org.faktorips.devtools.core.ui.controls.valuesets.ValueSetEditControlFactory;
import org.faktorips.devtools.model.valueset.IEnumValueSet;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.ValueSetType;

/**
 * Adapts the {@link EnumValueSubsetChooserModel} to an {@link IValueSetEditControl} to make the
 * {@link SubsetChooserViewer} compatible with {@link ValueSetEditControlFactory
 * ValueSetEditControlFactory's} newControl() method.
 * 
 * @author Stefan Widmaier
 */
public class SubsetChooserEditControl implements IValueSetEditControl {

    private final Composite composite;
    private final EnumValueSubsetChooserModel model;

    public SubsetChooserEditControl(Composite composite, EnumValueSubsetChooserModel model) {
        this.composite = composite;
        this.model = model;
    }

    @Override
    public ValueSetType getValueSetType() {
        return getValueSet().getValueSetType();
    }

    @Override
    public void setValueSet(IValueSet newSet, ValueDatatype valueDatatype) {
        model.setResultingEnumValueSet((IEnumValueSet)newSet);
    }

    @Override
    public IValueSet getValueSet() {
        return model.getResultingEnumValueSet();
    }

    @Override
    public boolean canEdit(IValueSet valueSet, ValueDatatype valueDatatype) {
        if (valueSet == null) {
            return false;
        }
        return valueSet.isEnum();
    }

    @Override
    public Composite getComposite() {
        return composite;
    }

}
