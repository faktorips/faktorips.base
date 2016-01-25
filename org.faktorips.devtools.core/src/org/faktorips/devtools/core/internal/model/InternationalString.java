/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.faktorips.devtools.core.model.IInternationalString;
import org.faktorips.devtools.core.model.XmlSupport;
import org.faktorips.runtime.internal.InternationalStringXmlReaderWriter;
import org.faktorips.util.collections.DistinctElementComparator;
import org.faktorips.values.LocalizedString;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A {@link InternationalString} could be used for string properties that could be translated in
 * different languages. The {@link InternationalString} consists of a set of {@link LocalizedString}
 * . To get notyfied about changes to any {@link LocalizedString} in this
 * {@link InternationalString} you could register as an {@link Observer}.
 * <p>
 * The {@link InternationalString} implements the {@link XmlSupport}. To be able to use more than
 * one {@link InternationalString} property in one object use the
 * {@link InternationalStringXmlHelper}.
 * 
 * @author dirmeier
 */
public class InternationalString extends Observable implements IInternationalString {

    public static final String XML_TAG = "InternationalString"; //$NON-NLS-1$

    public static final String XML_ELEMENT_LOCALIZED_STRING = "LocalizedString"; //$NON-NLS-1$

    public static final String XML_ATTR_LOCALE = "locale"; //$NON-NLS-1$

    public static final String XML_ATTR_TEXT = "text"; //$NON-NLS-1$

    private static final DistinctElementComparator<LocalizedString> COMPARATOR = DistinctElementComparator
            .createComparator(new Comparator<LocalizedString>() {

                @Override
                public int compare(LocalizedString o1, LocalizedString o2) {
                    return ObjectUtils.compare(o1.getValue(), o2.getValue());
                }
            });

    private final Map<Locale, LocalizedString> localizedStringMap = new LinkedHashMap<Locale, LocalizedString>();

    /**
     * The default constructor. Consider to register a {@link Observer} to get notified for changes
     */
    public InternationalString() {
        // default constructor
    }

    /**
     * Construct the object and register the given observer.
     * 
     * @param observer The observer you want to register to get notyfied for changes
     */
    public InternationalString(Observer observer) {
        this();
        addObserver(observer);
    }

    @Override
    public LocalizedString get(Locale locale) {
        LocalizedString localizedString = localizedStringMap.get(locale);
        if (localizedString == null) {
            return emptyLocalizedString(locale);
        } else {
            return localizedString;
        }
    }

    @Override
    public void add(LocalizedString localizedString) {
        Assert.isNotNull(localizedString);
        LocalizedString localizedStringToSet = localizedString;
        if (localizedString.getValue() == null) {
            localizedStringToSet = emptyLocalizedString(localizedString.getLocale());
        }
        LocalizedString oldText = localizedStringMap.put(localizedStringToSet.getLocale(), localizedStringToSet);
        if (!localizedStringToSet.equals(oldText)) {
            setChanged();
        }
        notifyObservers(localizedStringToSet);
    }

    /**
     * Returning all values of this {@link InternationalString} ordered by insertion.
     * 
     * {@inheritDoc}
     */
    @Override
    public Collection<LocalizedString> values() {
        return localizedStringMap.values();
    }

    @Override
    public void initFromXml(Element element) {
        Collection<LocalizedString> localizedStrings = InternationalStringXmlReaderWriter.fromXml(element);

        localizedStringMap.clear();
        for (LocalizedString localizedString : localizedStrings) {
            localizedStringMap.put(localizedString.getLocale(), localizedString);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Only localizedStrings with an value will be saved to xml
     */
    @Override
    public Element toXml(Document doc) {
        return InternationalStringXmlReaderWriter.toXml(doc, localizedStringMap.values());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((localizedStringMap == null) ? 0 : localizedStringMap.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof InternationalString)) {
            return false;
        }
        InternationalString other = (InternationalString)obj;
        if (localizedStringMap == null) {
            if (other.localizedStringMap != null) {
                return false;
            }
        } else {
            return equalLocalizedMapValues(other.values());
        }
        return true;
    }

    private boolean equalLocalizedMapValues(Collection<LocalizedString> otherLocalizedStringMapValues) {
        Collection<LocalizedString> values = values();
        if (otherLocalizedStringMapValues == null) {
            return false;
        }
        if (values.size() != otherLocalizedStringMapValues.size()) {
            return false;
        }
        for (LocalizedString localizedString : values) {
            if (!(otherLocalizedStringMapValues.contains(localizedString))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("InternationalString ["); //$NON-NLS-1$
        if (localizedStringMap != null) {
            for (LocalizedString localizedString : values()) {
                builder.append(localizedString.toString());
                builder.append(" "); //$NON-NLS-1$
            }
        }
        builder.append("]"); //$NON-NLS-1$
        return builder.toString();
    }

    private LocalizedString emptyLocalizedString(Locale locale) {
        return new LocalizedString(locale, StringUtils.EMPTY);
    }

    @Override
    public int compareTo(IInternationalString o) {
        return COMPARATOR.compare(values(), o.values());
    }

}
