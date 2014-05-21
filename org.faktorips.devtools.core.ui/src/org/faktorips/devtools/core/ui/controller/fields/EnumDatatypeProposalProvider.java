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
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.UIDatatypeFormatter;
import org.faktorips.devtools.core.ui.internal.ContentProposal;

public class EnumDatatypeProposalProvider extends AbstractProposalProvider {

    public EnumDatatypeProposalProvider(IConfigElement configElement, UIDatatypeFormatter uiDatatypeFormatter) {
        super(configElement, uiDatatypeFormatter);
    }

    @Override
    public IContentProposal[] getProposals(String contents, int position) {
        if (isEnumDatatypeAllowed()) {
            List<IContentProposal> result = new ArrayList<IContentProposal>();
            result = createContentProposals(contents);
            return result.toArray(new IContentProposal[result.size()]);
        }
        return new IContentProposal[0];
    }

    private boolean isEnumDatatypeAllowed() {
        return getDatatype().isEnum();
    }

    private List<IContentProposal> createContentProposals(String input) {
        List<IContentProposal> result = new ArrayList<IContentProposal>();
        List<String> allowedValuesAsList = getAllowedValuesAsList();
        for (String value : allowedValuesAsList) {
            String content = getFormatValue(value);
            if (value.toLowerCase().startsWith(input.toLowerCase())) {
                ContentProposal contentProposal = new ContentProposal(content, content, null, input);
                result.add(contentProposal);
            }
        }
        return result;
    }

    private List<String> getAllowedValuesAsList() {
        IValueSet allowedValueSet = getAllowedValueSet();
        return Arrays.asList(((EnumDatatype)getDatatype()).getAllValueIds(allowedValueSet.isContainingNull()));
    }

}
