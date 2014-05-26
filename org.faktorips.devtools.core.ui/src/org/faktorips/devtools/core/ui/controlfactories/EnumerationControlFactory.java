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
import org.eclipse.swt.widgets.Text;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.controller.fields.EnumerationFieldPainter;
import org.faktorips.devtools.core.ui.controller.fields.EnumerationProposalAdapter;
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
    protected void adaptEnumValueSetProposal(Text textControl, IValueSet valueSet, ValueDatatype datatype) {
        super.adaptEnumValueSetProposal(textControl, valueSet, datatype);
        if (valueSet == null) {
            EnumerationFieldPainter.addPainterTo(textControl, datatype, null);
            IInputFormat<String> inputFormat = IpsUIPlugin.getDefault().getInputFormat(datatype, null);
            EnumerationProposalAdapter.createAndActivateOnAnyKey(textControl, datatype, null, inputFormat);
        }
    }

    @Override
    public int getDefaultAlignment() {
        return SWT.LEFT;
    }

}
