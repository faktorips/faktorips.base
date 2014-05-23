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

import org.eclipse.jface.fieldassist.IContentProposal;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.ui.UIDatatypeFormatter;
import org.faktorips.devtools.core.ui.internal.ContentProposal;

/**
 * This is an implementation of the {@link AbstractEnumerationProposalProvider}. It provides valid
 * proposals for {@link EnumDatatype}s.
 */
public class EnumDatatypeProposalProvider extends AbstractEnumerationProposalProvider {

    public EnumDatatypeProposalProvider(EnumDatatype enumDatatype, UIDatatypeFormatter uiDatatypeFormatter) {
        super(enumDatatype, uiDatatypeFormatter);
    }

    @Override
    public IContentProposal[] getProposals(String contents, int position) {
        List<IContentProposal> result = new ArrayList<IContentProposal>();
        result = createContentProposals(contents);
        return result.toArray(new IContentProposal[result.size()]);
    }

    private List<IContentProposal> createContentProposals(String input) {
        List<IContentProposal> result = new ArrayList<IContentProposal>();
        List<String> allowedValuesAsList = getAllowedValuesAsList();
        for (String value : allowedValuesAsList) {
            String content = getFormatValue(value);
            if (content.toLowerCase().startsWith(input.toLowerCase())) {
                ContentProposal contentProposal = new ContentProposal(content, content, null, input);
                result.add(contentProposal);
            }
        }
        return result;
    }

    @Override
    public EnumDatatype getValueDatatype() {
        return (EnumDatatype)super.getValueDatatype();
    }

    private List<String> getAllowedValuesAsList() {
        return Arrays.asList(getValueDatatype().getAllValueIds(true));
    }
}
