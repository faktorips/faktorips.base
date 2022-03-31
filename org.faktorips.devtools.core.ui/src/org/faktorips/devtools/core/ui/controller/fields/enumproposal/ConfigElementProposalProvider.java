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

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.controller.fields.ConfiguredValueSetField;
import org.faktorips.devtools.core.ui.controller.fields.IValueSource;
import org.faktorips.devtools.core.ui.inputformat.IInputFormat;
import org.faktorips.devtools.model.internal.valueset.EnumValueSet;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IConfigElement;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.valueset.IEnumValueSet;
import org.faktorips.devtools.model.valueset.IValueSetOwner;
import org.faktorips.devtools.model.valueset.ValueSetType;

/**
 * An implementation of {@link AbstractProposalProvider} for {@link ConfiguredValueSetField}s. It
 * provides proposals for all {@link IValueSetOwner}s allowing enum {@link ValueSetType}.
 */
public class ConfigElementProposalProvider extends AbstractProposalProvider {

    public ConfigElementProposalProvider(IConfiguredValueSet configuredValueSet, ValueDatatype valueDatatype,
            IInputFormat<String> inputFormat) {
        super(configuredValueSet, valueDatatype, inputFormat);
    }

    @Override
    public IConfiguredValueSet getValueSetOwner() {
        return (IConfiguredValueSet)super.getValueSetOwner();
    }

    protected IConfigElement getConfigElement() {
        return getValueSetOwner();
    }

    @Override
    protected IValueSource createValueSource(IValueSetOwner valueSetOwner, ValueDatatype datatype) {
        // use the policy attribute's enum value set as a base value set
        return new EnumValueSource(getPolicyAttribute(), datatype);
    }

    private IValueSetOwner getPolicyAttribute() {
        return getConfigElement().findPcTypeAttribute(getIpsProject());
    }

    @Override
    public IContentProposal[] getProposals(String contents, int position) {
        if (isEnumValueSetAllowed()) {
            return super.getProposals(contents, position);
        }
        return new IContentProposal[0];
    }

    @Override
    protected String getPrefixFor(String contents, int position) {
        String prefix = super.getPrefixFor(contents, position);
        return getLastValue(prefix);
    }

    @Override
    protected boolean isApplicable(String valueInModel, String formattedValue) {
        return !isAlreadyContained(valueInModel) && super.isApplicable(valueInModel, formattedValue);
    }

    private boolean isEnumValueSetAllowed() {
        return getValueSetOwner().getAllowedValueSetTypes(getIpsProject()).contains(ValueSetType.ENUM);
    }

    private boolean isAlreadyContained(String value) {
        if (isCurrentEnumValueSet()) {
            IEnumValueSet currentValueSet = getCurrentValueSet();
            return currentValueSet.containsValue(value, getIpsProject());
        } else {
            return false;
        }
    }

    private IIpsProject getIpsProject() {
        return getConfigElement().getIpsProject();
    }

    private boolean isCurrentEnumValueSet() {
        return getValueSetOwner().getValueSet().isEnum();
    }

    private IEnumValueSet getCurrentValueSet() {
        return (IEnumValueSet)getValueSetOwner().getValueSet();
    }

    private String getLastValue(String s) {
        if (StringUtils.isEmpty(s)) {
            return StringUtils.EMPTY;
        }
        int i = s.length() - 1;
        while (i >= 0) {
            char c = s.charAt(i);
            if (isSeparatorChar(c)) {
                break;
            }
            i--;
        }
        // removes whitespace chars from beginning of the input string
        return StringUtils.stripStart(s.substring(i + 1), null);
    }

    private boolean isSeparatorChar(char c) {
        return EnumValueSet.ENUM_VALUESET_SEPARATOR.equals(String.valueOf(c));
    }
}
