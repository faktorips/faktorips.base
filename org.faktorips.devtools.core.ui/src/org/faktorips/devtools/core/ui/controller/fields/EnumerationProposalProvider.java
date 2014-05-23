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
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.faktorips.datatype.EnumDatatype;
/**
 * An <code>IContentProposalProvider</code> for EnumerationFields.
 */
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.ui.inputformat.IInputFormat;
import org.faktorips.devtools.core.ui.internal.ContentProposal;

/**
 * An implementation of {@link IContentProposalProvider} for {@link EnumerationField}s. It provides
 * proposals for {@link EnumDatatype}s or {@link EnumValueSet}s.
 */
public class EnumerationProposalProvider implements IContentProposalProvider {

    private final IInputFormat<String> inputFormat;
    private ValueDatatype valueDatatype;
    private IValueSetOwner owner;

    public EnumerationProposalProvider(ValueDatatype valueDatatype, IValueSetOwner owner,
            IInputFormat<String> inputFormat) {
        this.valueDatatype = valueDatatype;
        this.owner = owner;
        this.inputFormat = inputFormat;
    }

    public ValueDatatype getValueDatatype() {
        return valueDatatype;
    }

    public String getFormatValue(String value) {
        return inputFormat.format(value);
    }

    @Override
    public IContentProposal[] getProposals(String contents, int position) {
        List<IContentProposal> result = new ArrayList<IContentProposal>();
        result = createContentProposals(contents);
        return result.toArray(new IContentProposal[result.size()]);
    }

    private List<IContentProposal> createContentProposals(String input) {
        List<IContentProposal> result = new ArrayList<IContentProposal>();
        List<String> allowedValuesAsList = getValuesAsList();
        for (String value : allowedValuesAsList) {
            String content = getFormatValue(value);
            if (content.toLowerCase().startsWith(input.toLowerCase())) {
                final String newContentPart = content.substring(input.length());
                ContentProposal contentProposal = new ContentProposal(newContentPart, content, null, input);
                result.add(contentProposal);
            }
        }
        return result;
    }

    private List<String> getValuesAsList() {
        IValueSource valueSource = new EnumValueSetValueSource(owner);
        if (!valueSource.hasValues() && valueDatatype.isEnum()) {
            valueSource = new EnumDatatypeValueSource((EnumDatatype)valueDatatype);
        }
        return valueSource.getValues();
    }
}
