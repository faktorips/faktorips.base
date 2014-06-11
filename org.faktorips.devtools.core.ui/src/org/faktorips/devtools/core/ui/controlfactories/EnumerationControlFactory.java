/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controlfactories;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controller.fields.enumproposal.EnumerationProposalAdapter;
import org.faktorips.devtools.core.ui.inputformat.IInputFormat;

/**
 * A control factory for the {@link IEnumType} which implements the {@link EnumDatatype} interface.
 * 
 */
public class EnumerationControlFactory extends DefaultControlFactory {

    public EnumerationControlFactory() {
        super();
    }

    @Override
    public boolean isFactoryFor(ValueDatatype datatype) {
        return datatype instanceof EnumDatatype;
    }

    @Override
    protected void adaptEnumValueProposal(UIToolkit toolkit,
            Text textControl,
            IValueSet valueSet,
            ValueDatatype datatype) {
        super.adaptEnumValueProposal(toolkit, textControl, valueSet, datatype);
        if (requiresEnumValueProposal(valueSet)) {
            // always create a proposal provider at this point as this is an enum datatype
            IInputFormat<String> inputFormat = getInputFormat(datatype, valueSet);
            Button button = createArrowDownButton(toolkit, textControl.getParent());
            EnumerationProposalAdapter.createAndActivateOnAnyKey(textControl, button, datatype, null, inputFormat);
        }
    }

    /**
     * This method returns <code>true</code> if:
     * <ul>
     * <li>
     * the value set is <code>null</code> or</li>
     * <li>
     * the value set is not an enum</li>
     * </ul>
     * For enum value sets, content propsal will be added by the super implementation:
     * {@link ValueDatatypeControlFactory#adaptEnumValueProposal(UIToolkit, Text, IValueSet, ValueDatatype)}
     * .
     */
    private boolean requiresEnumValueProposal(IValueSet valueSet) {
        return valueSet == null || !valueSet.isEnum();
    }

    @Override
    public int getDefaultAlignment() {
        return SWT.LEFT;
    }

}
