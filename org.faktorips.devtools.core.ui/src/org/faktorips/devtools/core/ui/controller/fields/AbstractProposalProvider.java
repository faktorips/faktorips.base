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

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.ui.inputformat.IInputFormat;
import org.faktorips.devtools.core.ui.internal.ContentProposal;

/**
 * Base class for {@link ConfigElementProposalProvider} and {@link EnumerationProposalProvider}. It
 * implements {@link IContentProposalProvider}.
 */
public abstract class AbstractProposalProvider implements IContentProposalProvider {

    private final IInputFormat<String> inputFormat;
    private IValueSetOwner valueSetOwner;
    private ValueDatatype valueDatatype;
    private int proposalAcceptanceStyle;

    public AbstractProposalProvider(IValueSetOwner owner, ValueDatatype valueDatatype,
            IInputFormat<String> inputFormat, int proposalAcceptanceStyle) {
        this.valueSetOwner = owner;
        this.valueDatatype = valueDatatype;
        this.inputFormat = inputFormat;
        this.proposalAcceptanceStyle = proposalAcceptanceStyle;
    }

    public ValueDatatype getValueDatatype() {
        return valueDatatype;
    }

    public IValueSetOwner getValueSetOwner() {
        return valueSetOwner;
    }

    public IInputFormat<String> getInputFormat() {
        return inputFormat;
    }

    public int getProposalAcceptanceStyle() {
        return proposalAcceptanceStyle;
    }

    protected String getFormatValue(String value) {
        return inputFormat.format(value);
    }

    @Override
    public IContentProposal[] getProposals(String contents, int position) {
        List<IContentProposal> result = new ArrayList<IContentProposal>();
        String prefix = createPrefix(contents, position);
        result = createContentProposals(prefix);
        return result.toArray(new IContentProposal[result.size()]);
    }

    protected String createPrefix(String contents, int position) {
        return StringUtils.left(contents, position);
    }

    private List<IContentProposal> createContentProposals(String prefix) {
        List<IContentProposal> result = new ArrayList<IContentProposal>();
        List<String> allowedValuesAsList = getAllowedValuesAsList();
        for (String valueInModel : allowedValuesAsList) {
            String formattedValue = getFormatValue(valueInModel);
            if (isApplicable(prefix, valueInModel, formattedValue)) {
                final String newContentPart = getContentForAcceptanceStyle(getProposalAcceptanceStyle(), prefix,
                        formattedValue);
                ContentProposal contentProposal = new ContentProposal(newContentPart, formattedValue, null, prefix);
                result.add(contentProposal);
            }
        }
        return result;
    }

    private String getContentForAcceptanceStyle(int acceptanceStyle, String prefix, String formattedValue) {
        if (acceptanceStyle == ContentProposalAdapter.PROPOSAL_REPLACE) {
            return formattedValue;
        }
        if (acceptanceStyle == ContentProposalAdapter.PROPOSAL_INSERT) {
            return formattedValue.substring(prefix.length());
        }
        return formattedValue;
    }

    /**
     * Specifies if the proposal provider is applicable for the given prefix, valueInModel and the
     * formattedValues allowed in the model. The comparison of formattedValue and prefix is not case
     * sensitive.
     * 
     * @param prefix specifies the scope of the considered user input.
     * @param valueInModel is an allowed value declared in the model.
     * @param formattedValue formatted string representation of valueInModel.
     * @return <code>true</code> if the proposal provider can be used for formatted values.
     */
    protected boolean isApplicable(String prefix, String valueInModel, String formattedValue) {
        return formattedValue.toLowerCase().startsWith(prefix.toLowerCase());
    }

    /**
     * Returns all allowed values declared in the model of a specific {@link IValueSet} or
     * {@link EnumDatatype}.
     * 
     * @return List<String> containing all allowed values.
     */
    protected abstract List<String> getAllowedValuesAsList();

}
