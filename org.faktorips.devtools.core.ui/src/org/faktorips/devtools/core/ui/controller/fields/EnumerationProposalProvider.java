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

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.ui.inputformat.IInputFormat;
import org.faktorips.devtools.core.ui.internal.ContentProposal;

/**
 * An implementation of {@link AbstractProposalProvider}. It provides proposals for
 * {@link EnumDatatype}s or {@link EnumValueSet}s.
 */
public class EnumerationProposalProvider extends AbstractProposalProvider {

    public EnumerationProposalProvider(ValueDatatype valueDatatype, IValueSetOwner owner,
            IInputFormat<String> inputFormat) {
        super(owner, valueDatatype, inputFormat);
    }

    @Override
    protected List<String> getAllowedValuesAsList() {
        IValueSource valueSource = new EnumValueSetValueSource(getValueSetOwner());
        if (!valueSource.isApplicable() && getValueDatatype().isEnum()) {
            return new EnumDatatypeValueSource(getValueDatatype()).getValues();
        }
        if (valueSource.isApplicable()) {
            return valueSource.getValues();
        }
        return new ArrayList<String>();
    }

    @Override
    public IContentProposal[] getProposals(String contents, int position) {
        IContentProposal[] proposals = super.getProposals(contents, position);
        ArrayList<IContentProposal> proposalsList = new ArrayList<IContentProposal>(Arrays.asList(proposals));
        if (isControlForDefaultValue()) {
            if (contents.equals(getFormatValue(null))) {
                IContentProposal[] allProposals = super.getProposals(StringUtils.EMPTY, 0);
                proposalsList = new ArrayList<IContentProposal>(Arrays.asList(allProposals));
            }
            if (!containsNull(proposalsList)) {
                addNullProposal(proposalsList);
            }
        }
        return proposalsList.toArray(new IContentProposal[proposalsList.size()]);
    }

    private boolean isControlForDefaultValue() {
        return getValueSetOwner() instanceof IConfigElement;
    }

    private boolean containsNull(List<IContentProposal> proposalsList) {
        ContentProposal nullProposal = createNullProposal();
        for (IContentProposal contentProposal : proposalsList) {
            if (contentProposal.getLabel().equalsIgnoreCase(nullProposal.getLabel())) {
                return true;
            }
        }
        return false;
    }

    private ContentProposal createNullProposal() {
        String formattedNullValue = getFormatValue(null);
        ContentProposal nullProposal = new ContentProposal(formattedNullValue, formattedNullValue, null,
                formattedNullValue);
        return nullProposal;
    }

    private void addNullProposal(List<IContentProposal> proposalsList) {
        ContentProposal nullProposal = createNullProposal();
        proposalsList.add(nullProposal);
    }
}
