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
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.FormattingTextField;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.valueset.IValueSet;

/**
 * A default factory that creates a combo box for none-abstract enum value sets and a simple text
 * control in all other cases.
 * 
 * @author Joerg Ortmann
 */
public class DefaultControlFactory extends ValueDatatypeControlFactory {

    public DefaultControlFactory() {
        super();
    }

    @Override
    public boolean isFactoryFor(ValueDatatype datatype) {
        return true;
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
        return SWT.LEFT;
    }

    @Override
    protected EditField<String> createEditFieldForTable(UIToolkit toolkit,
            Composite parent,
            ValueDatatype datatype,
            IValueSet valueSet,
            IIpsProject ipsProject) {
        Text text = toolkit.createTextAppendStyle(parent, getDefaultAlignment());
        return new FormattingTextField<>(text, getInputFormat(datatype, valueSet, ipsProject));
    }

}
