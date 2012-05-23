/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.controls.chooser;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.ui.controls.valuesets.IValueSetEditControl;
import org.faktorips.devtools.core.ui.controls.valuesets.ValueSetEditControlFactory;

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
