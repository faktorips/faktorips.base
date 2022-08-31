/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields.enumproposal;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.ui.dialogs.SearchPattern;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.controller.fields.IValueSource;
import org.faktorips.devtools.core.ui.controls.contentproposal.AbstractPrefixContentProposalProvider;
import org.faktorips.devtools.core.ui.inputformat.IInputFormat;
import org.faktorips.devtools.core.ui.internal.ContentProposal;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.IValueSetOwner;

/**
 * Base class for {@link ConfigElementProposalProvider} and {@link EnumerationProposalProvider}. It
 * implements {@link IContentProposalProvider}.
 */
public abstract class AbstractProposalProvider extends AbstractPrefixContentProposalProvider {

    private final IInputFormat<String> inputFormat;
    private final IValueSetOwner valueSetOwner;
    private final ValueDatatype valueDatatype;
    private final IValueSource valueSource;
    private final SearchPattern searchPattern = new SearchPattern(SearchPattern.RULE_BLANK_MATCH
            | SearchPattern.RULE_CAMELCASE_MATCH | SearchPattern.RULE_PATTERN_MATCH | SearchPattern.RULE_PREFIX_MATCH);

    public AbstractProposalProvider(IValueSetOwner owner, ValueDatatype valueDatatype,
            IInputFormat<String> inputFormat) {
        valueSetOwner = owner;
        this.valueDatatype = valueDatatype;
        this.inputFormat = inputFormat;
        valueSource = createValueSource(valueSetOwner, valueDatatype);
    }

    /**
     * Creates the value source for this proposal provider. For each value in the list returned by
     * the {@link IValueSource}, a content proposal is created.
     * 
     * @param valueSetOwner the value set owner
     * @param datatype the attribute's datatype
     */
    protected abstract IValueSource createValueSource(IValueSetOwner valueSetOwner, ValueDatatype datatype);

    public ValueDatatype getValueDatatype() {
        return valueDatatype;
    }

    public IValueSetOwner getValueSetOwner() {
        return valueSetOwner;
    }

    public IInputFormat<String> getInputFormat() {
        return inputFormat;
    }

    protected String format(String value) {
        return inputFormat.format(value);
    }

    @Override
    public IContentProposal[] getProposals(String prefix) {
        List<IContentProposal> result = createContentProposals(prefix);
        return result.toArray(new IContentProposal[result.size()]);
    }

    /**
     * This implementation uses the {@link SearchPattern} to matches the prefix (pattern) against
     * the value.
     * <p>
     * To get perfect match from {@link SearchPattern} we have to special cases:
     * <ol>
     * <li>The prefix is trimmed before it is used as pattern. This is necessary because the
     * {@link SearchPattern} would only try perfect match if the pattern ends with a blank.</li>
     * <li>The value is capitalized before we try to match. This is necessary to be able to search
     * entries by camel-case-pattern also if they start with a lower case.</li>
     * </ol>
     */
    private List<IContentProposal> createContentProposals(String prefix) {
        searchPattern.setPattern(prefix.trim());
        List<IContentProposal> result = new ArrayList<>();
        for (String valueInModel : getAllowedValuesAsList()) {
            String formattedValue = format(valueInModel);
            if (isApplicable(valueInModel, formattedValue)) {
                ContentProposal contentProposal = createProposal(prefix, formattedValue);
                result.add(contentProposal);
            }
        }
        return result;
    }

    private ContentProposal createProposal(String prefix, String formattedValue) {
        final String textToInsert = getContentForAcceptanceStyle(formattedValue);
        return new ContentProposal(textToInsert, formattedValue, null, prefix);
    }

    private String getContentForAcceptanceStyle(String formattedValue) {
        return formattedValue;
    }

    /**
     * Specifies if the proposal provider is applicable for the given prefix, valueInModel and the
     * formattedValues allowed in the model. The comparison of formattedValue and prefix is not case
     * sensitive.
     * 
     * @param valueInModel is an allowed value declared in the model.
     * @param formattedValue formatted string representation of valueInModel.
     * 
     * @return <code>true</code> if the proposal provider can be used for formatted values.
     */
    protected boolean isApplicable(String valueInModel, String formattedValue) {
        return match(formattedValue);
    }

    private boolean match(String value) {
        return searchPattern.matches(StringUtils.capitalize(value));
    }

    /**
     * Returns all allowed values declared in the model of a specific {@link IValueSet} or
     * {@link EnumDatatype}.
     * 
     * @return List&lt;String&gt; containing all allowed values.
     */
    private List<String> getAllowedValuesAsList() {
        return valueSource.getValues();
    }

}
