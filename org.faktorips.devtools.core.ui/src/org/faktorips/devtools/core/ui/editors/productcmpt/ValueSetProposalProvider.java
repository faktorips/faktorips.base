/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.ui.dialogs.SearchPattern;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.ui.UIDatatypeFormatter;
import org.faktorips.devtools.core.ui.inputFormat.ValueSetFormat;
import org.faktorips.devtools.core.ui.internal.ContentProposal;

public class ValueSetProposalProvider implements IContentProposalProvider {

    private final IConfigElement configElement;

    private final UIDatatypeFormatter uiDatatypeFormatter;

    private SearchPattern searchPattern = new SearchPattern();

    public ValueSetProposalProvider(IConfigElement propertyValue, UIDatatypeFormatter uiDatatypeFormatter) {
        configElement = propertyValue;
        this.uiDatatypeFormatter = uiDatatypeFormatter;
    }

    @Override
    public IContentProposal[] getProposals(String contents, int position) {
        if (isEnumValueSetAllowed()) {
            String prefix = StringUtils.left(contents, position);
            String lastValue = getLastValue(prefix);
            boolean needSeparator = needSeparator(prefix, lastValue);

            searchPattern.setPattern(lastValue);
            List<IContentProposal> result = new ArrayList<IContentProposal>();
            List<String> allowedValuesAsList = getAllowedValuesAsList();
            for (String value : allowedValuesAsList) {
                String content = getFormatValue(value);
                if (!isAlreadyContained(value) && searchPattern.matches(content)) {
                    ContentProposal contentProposal = new ContentProposal(addSeparatorIfNecessary(content,
                            needSeparator), content, null, lastValue);
                    result.add(contentProposal);
                }
            }
            return result.toArray(new IContentProposal[result.size()]);
        }
        return new IContentProposal[0];
    }

    private boolean isEnumValueSetAllowed() {
        try {
            return configElement.getAllowedValueSetTypes(getIpsProject()).contains(ValueSetType.ENUM);
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

    private ValueDatatype getDatatype() {
        try {
            return configElement.findValueDatatype(getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private IIpsProject getIpsProject() {
        return configElement.getIpsProject();
    }

    private List<String> getAllowedValuesAsList() {
        final IValueSet allowedValueSet = getAllowedValueSet();
        if (allowedValueSet.canBeUsedAsSupersetForAnotherEnumValueSet()) {
            return ((IEnumValueSet)allowedValueSet).getValuesAsList();
        } else if (getDatatype().isEnum()) {
            return Arrays.asList(((EnumDatatype)getDatatype()).getAllValueIds(allowedValueSet.isContainingNull()));
        }
        return new ArrayList<String>();
    }

    private IValueSet getAllowedValueSet() {
        try {
            return configElement.findPcTypeAttribute(getIpsProject()).getValueSet();
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private boolean isCurrentEnumValueSet() {
        return configElement.getValueSet().isEnum();
    }

    private IEnumValueSet getCurrentValueSet() {
        return (IEnumValueSet)configElement.getValueSet();
    }

    private String getFormatValue(String value) {
        return uiDatatypeFormatter.formatValue(getDatatype(), value);
    }

    private String addSeparatorIfNecessary(String value, boolean needSeparator) {
        if (needSeparator) {
            return ValueSetFormat.VALUESET_SEPARATOR + " " + value; //$NON-NLS-1$
        }
        return value;
    }

    private boolean needSeparator(String s, String identifier) {
        if (StringUtils.isEmpty(s) || s.equals(identifier)) {
            return false;
        }
        int pos = s.indexOf(identifier);
        if (pos == 0) {
            return !endsWithSeparator(s);
        } else if (pos > 0) {
            return !endsWithSeparator(s.substring(0, pos));
        }
        return true;
    }

    private boolean endsWithSeparator(String s) {
        return s.trim().endsWith(ValueSetFormat.VALUESET_SEPARATOR);
    }

    private String getLastValue(String s) {
        if (StringUtils.isEmpty(s)) {
            return StringUtils.EMPTY;
        }
        int i = s.length() - 1;
        while (i >= 0) {
            char c = s.charAt(i);
            if (!isLegalChar(c)) {
                break;
            }
            i--;
        }
        return s.substring(i + 1).trim();
    }

    private boolean isLegalChar(char c) {
        return !ValueSetFormat.VALUESET_SEPARATOR.equals(String.valueOf(c));
    }
}
