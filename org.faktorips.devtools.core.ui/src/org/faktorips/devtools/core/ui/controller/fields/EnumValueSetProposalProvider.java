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
import java.util.List;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.ui.UIDatatypeFormatter;
import org.faktorips.devtools.core.ui.internal.ContentProposal;

/**
 * This is an implementation of the {@link AbstractEnumerationProposalProvider}. It provides valid
 * proposals for {@link EnumValueSet}s.
 */
public class EnumValueSetProposalProvider extends AbstractEnumerationProposalProvider {

    private IValueSetOwner owner;

    public EnumValueSetProposalProvider(IValueSetOwner owner, ValueDatatype valueDatatype,
            UIDatatypeFormatter uiDatatypeFormatter) {
        super(valueDatatype, uiDatatypeFormatter);
        this.owner = owner;
    }

    @Override
    public IContentProposal[] getProposals(String contents, int position) {
        if (isEnumValueSet()) {
            List<IContentProposal> result = new ArrayList<IContentProposal>();
            result = createContentProposals(contents);
            return result.toArray(new IContentProposal[result.size()]);
        }
        return new IContentProposal[0];
    }

    private boolean isEnumValueSet() {
        return getValueSet().isEnum();
    }

    private IValueSet getValueSet() {
        return owner.getValueSet();
    }

    private List<IContentProposal> createContentProposals(String input) {
        List<IContentProposal> result = new ArrayList<IContentProposal>();
        EnumValueSet enumValueSet = (EnumValueSet)getValueSet();
        List<String> valuesAsList = enumValueSet.getValuesAsList();
        for (String value : valuesAsList) {
            String formatedValue = getFormatValue(value);
            if (formatedValue.toLowerCase().startsWith(input.toLowerCase())) {
                ContentProposal contentProposal = new ContentProposal(formatedValue, formatedValue, null, input);
                result.add(contentProposal);
            }
        }
        return result;
    }
}
