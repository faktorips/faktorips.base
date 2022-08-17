/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controlfactories;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.CurrencySymbolPainter;
import org.faktorips.devtools.core.ui.controller.fields.FormattingTextField;
import org.faktorips.devtools.core.ui.inputformat.MoneyFormat;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.valueset.IValueSet;

public class MoneyControlFactory extends ValueDatatypeControlFactory {

    public MoneyControlFactory() {
        super();
    }

    @Override
    public boolean isFactoryFor(ValueDatatype datatype) {
        return Datatype.MONEY.equals(datatype);
    }

    @Override
    public EditField<String> createEditField(UIToolkit toolkit,
            Composite parent,
            ValueDatatype datatype,
            IValueSet valueSet,
            IIpsProject ipsProject) {
        Text control = createTextAndAdaptEnumProposal(toolkit, parent, datatype, valueSet, ipsProject);
        return setUpFieldForTextControl(datatype, valueSet, control, ipsProject);
    }

    @Override
    public int getDefaultAlignment() {
        return SWT.RIGHT;
    }

    @Override
    protected EditField<String> createEditFieldForTable(UIToolkit toolkit,
            Composite parent,
            ValueDatatype datatype,
            IValueSet valueSet,
            IIpsProject ipsProject) {
        Text control = toolkit.createTextAppendStyle(parent, getDefaultAlignment());
        return setUpFieldForTextControl(datatype, valueSet, control, ipsProject);
    }

    private EditField<String> setUpFieldForTextControl(ValueDatatype datatype,
            IValueSet valueSet,
            Text control,
            IIpsProject ipsProject) {
        MoneyFormat inputFormat = (MoneyFormat)getInputFormat(datatype, valueSet, ipsProject);
        control.addPaintListener(new CurrencySymbolPainter(inputFormat));
        return new FormattingTextField<>(control, inputFormat);
    }
}
