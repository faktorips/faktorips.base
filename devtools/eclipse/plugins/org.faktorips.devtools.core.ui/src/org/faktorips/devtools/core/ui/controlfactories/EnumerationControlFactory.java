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

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.FormattingComboField;
import org.faktorips.devtools.core.ui.controller.fields.enumproposal.EnumerationProposalProvider;
import org.faktorips.devtools.core.ui.inputformat.IInputFormat;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.runtime.internal.IpsStringUtils;

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
    protected EditField<String> createEditFieldForTable(UIToolkit toolkit,
            Composite parent,
            ValueDatatype datatype,
            IValueSet valueSet,
            IIpsProject ipsProject) {
        IInputFormat<String> inputFormat = getInputFormat(datatype, valueSet, ipsProject);
        String[] proposalsAsString = getProposals(datatype, inputFormat);

        Combo combo = new Combo(parent, SWT.NONE);
        combo.setItems(proposalsAsString);

        return new FormattingComboField<>(combo, inputFormat);
    }

    private String[] getProposals(ValueDatatype datatype, IInputFormat<String> inputFormat) {
        EnumerationProposalProvider enumerationProposalProvider = new EnumerationProposalProvider(datatype, null,
                inputFormat);
        IContentProposal[] proposals = enumerationProposalProvider.getProposals(IpsStringUtils.EMPTY, 0);
        String[] proposalsAsString = new String[proposals.length];
        for (int i = 0; i < proposals.length; i++) {
            proposalsAsString[i] = proposals[i].getLabel();
        }
        return proposalsAsString;
    }

    @Override
    public int getDefaultAlignment() {
        return SWT.LEFT;
    }

}
