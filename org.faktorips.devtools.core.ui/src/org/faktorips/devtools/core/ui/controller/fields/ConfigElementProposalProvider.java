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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.ui.inputformat.IInputFormat;

/**
 * An implementation of {@link AbstractProposalProvider} for {@link ConfigElementField}s. It
 * provides proposals for all {@link IValueSetOwner}s allowing enum {@link ValueSetType}.
 */
public class ConfigElementProposalProvider extends AbstractProposalProvider {

    public ConfigElementProposalProvider(IConfigElement configElement, ValueDatatype valueDatatype,
            IInputFormat<String> inputFormat) {
        super(configElement, valueDatatype, inputFormat);
    }

    @Override
    public IConfigElement getValueSetOwner() {
        return (IConfigElement)super.getValueSetOwner();
    }

    @Override
    public IContentProposal[] getProposals(String contents, int position) {
        if (isEnumValueSetAllowed()) {
            return super.getProposals(contents, position);
        }
        return new IContentProposal[0];
    }

    @Override
    protected String createPrefix(String contents, int position) {
        String prefix = super.createPrefix(contents, position);
        return getLastValue(prefix);
    }

    @Override
    protected boolean isApplicable(String prefix, String valueInModel, String formattedValue) {
        return !isAlreadyContained(valueInModel) && super.isApplicable(prefix, valueInModel, formattedValue);
    }

    private boolean isEnumValueSetAllowed() {
        try {
            return getValueSetOwner().getAllowedValueSetTypes(getIpsProject()).contains(ValueSetType.ENUM);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private boolean isAlreadyContained(String value) {
        try {
            if (isCurrentEnumValueSet()) {
                IEnumValueSet currentValueSet = getCurrentValueSet();
                return currentValueSet.containsValue(value, getIpsProject());
            } else {
                return false;
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private IIpsProject getIpsProject() {
        return getValueSetOwner().getIpsProject();
    }

    @Override
    protected List<String> getAllowedValuesAsList() {
        IValueSet allowedValueSet = getAllowedValueSet();
        if (allowedValueSet.canBeUsedAsSupersetForAnotherEnumValueSet()) {
            return ((IEnumValueSet)allowedValueSet).getValuesAsList();
        } else if (getValueDatatype().isEnum()) {
            return new EnumDatatypeValueSource(getValueDatatype()).getValues();
        }
        return new ArrayList<String>();
    }

    private IValueSet getAllowedValueSet() {
        try {
            return getValueSetOwner().findPcTypeAttribute(getIpsProject()).getValueSet();
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
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
