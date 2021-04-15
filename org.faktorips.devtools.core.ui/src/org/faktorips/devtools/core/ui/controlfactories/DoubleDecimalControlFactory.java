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
import org.faktorips.devtools.core.ui.controller.fields.FormattingTextField;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.valueset.IValueSet;

/**
 * A factory for edit fields/controls for the data type Double and Decimal.
 * 
 * @author Stefan Widmaier
 */
public class DoubleDecimalControlFactory extends ValueDatatypeControlFactory {

    public DoubleDecimalControlFactory() {
        super();
    }

    @Override
    public boolean isFactoryFor(ValueDatatype datatype) {
        return Datatype.DOUBLE.equals(datatype) || Datatype.DECIMAL.equals(datatype)
                || Datatype.BIG_DECIMAL.equals(datatype);
    }

    @Override
    public EditField<String> createEditField(UIToolkit toolkit,
            Composite parent,
            ValueDatatype datatype,
            IValueSet valueSet,
            IIpsProject ipsProject) {
        Text text = createTextAndAdaptEnumProposal(toolkit, parent, datatype, valueSet, ipsProject);
        return new FormattingTextField<>(text, getInputFormat(datatype, valueSet, ipsProject));

    }

    @Override
    public int getDefaultAlignment() {
        return SWT.RIGHT;
    }

}
