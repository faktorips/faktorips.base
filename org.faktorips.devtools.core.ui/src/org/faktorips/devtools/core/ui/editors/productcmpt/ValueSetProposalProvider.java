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
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.UIDatatypeFormatter;
import org.faktorips.devtools.core.ui.internal.ContentProposal;

public class ValueSetProposalProvider implements IContentProposalProvider {

    private final IConfigElement configElement;

    private final UIDatatypeFormatter uiDatatypeFormatter;

    private SearchPattern searchPattern = new SearchPattern();

    public ValueSetProposalProvider(IConfigElement propertyValue, UIDatatypeFormatter uiDatatypeFormatter) {
        this.configElement = propertyValue;
        this.uiDatatypeFormatter = uiDatatypeFormatter;
    }

    @Override
    public IContentProposal[] getProposals(String contents, int position) {
        if (getValueSet() instanceof IEnumValueSet) {
            String prefix = StringUtils.left(contents, position);
            String identifier = getLastIdentifier(prefix);
            boolean needSeparator = needSeparator(prefix, identifier);

            List<String> splitList = getContentAsList(contents);

            searchPattern.setPattern(identifier);
            List<IContentProposal> result = new ArrayList<IContentProposal>();
            for (String value : getAllowedValuesAsList()) {
                String content = getFormatValue(value);
                if (!splitList.contains(content) && searchPattern.matches(content)) {
                    ContentProposal contentProposal = new ContentProposal(addSeparatorIfNecessary(content,
                            needSeparator), content, null, identifier);
                    result.add(contentProposal);
                }
            }
            return result.toArray(new IContentProposal[result.size()]);
        }
        return new IContentProposal[0];
    }

    private IValueSet getAllowedValues() {
        try {
            return this.configElement.findPcTypeAttribute(configElement.getIpsProject()).getValueSet();
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private ValueDatatype getDatatype() {
        try {
            return this.configElement.findValueDatatype(this.configElement.getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private List<String> getAllowedValuesAsList() {
        if (getAllowedValues().canBeUsedAsSupersetForAnotherEnumValueSet()) {
            return ((IEnumValueSet)getAllowedValues()).getValuesAsList();
        } else if (getDatatype().isEnum()) {
            return Arrays.asList(((EnumDatatype)getDatatype()).getAllValueIds(false));
        }
        return new ArrayList<String>();
    }

    private IValueSet getValueSet() {
        return this.configElement.getValueSet();
    }

    private EnumValueSet getEnumValueSet() {
        return (EnumValueSet)getValueSet();
    }

    private String getFormatValue(String value) {
        return uiDatatypeFormatter.formatValue(getEnumValueSet().getValueDatatype(), value);
    }

    private List<String> getContentAsList(String contents) {
        List<String> contentsList = new ArrayList<String>();
        String[] splitContent = contents.trim().split("\\" + UIDatatypeFormatter.VALUESET_SEPARATOR); //$NON-NLS-1$
        for (String content : splitContent) {
            contentsList.add(content.trim());
        }
        return contentsList;
    }

    private String addSeparatorIfNecessary(String value, boolean needSeparator) {
        if (needSeparator) {
            return UIDatatypeFormatter.VALUESET_SEPARATOR + " " + value; //$NON-NLS-1$
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
        return s.trim().endsWith(UIDatatypeFormatter.VALUESET_SEPARATOR);
    }

    /**
     * The characters that are checked within this method have to be in synch with the identifier
     * tokens defined in the ffl.jjt grammar
     */
    private String getLastIdentifier(String s) {
        if (StringUtils.isEmpty(s)) {
            return StringUtils.EMPTY;
        }
        int i = s.length() - 1;
        boolean isInQuotes = false;
        while (i >= 0) {
            char c = s.charAt(i);
            if (c == '"') {
                isInQuotes = !isInQuotes;
            } else if (!isLegalChar(c, isInQuotes)) {
                break;
            }
            i--;
        }
        return s.substring(i + 1);
    }

    private boolean isLegalChar(char c, boolean isInQuotes) {
        return Character.isLetterOrDigit(c) || (isInQuotes && c == ' ');
    }
}
