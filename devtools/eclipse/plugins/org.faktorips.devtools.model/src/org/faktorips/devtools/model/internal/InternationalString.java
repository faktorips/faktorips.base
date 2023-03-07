/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.eclipse.core.runtime.Assert;
import org.faktorips.devtools.model.IInternationalString;
import org.faktorips.devtools.model.XmlSupport;
import org.faktorips.runtime.internal.InternationalStringXmlReaderWriter;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.util.collections.DistinctElementComparator;
import org.faktorips.values.LocalizedString;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A {@link InternationalString} can be used for string properties that can be translated in
 * different languages. It contains a set of {@link LocalizedString}. To get notified about changes
 * to any {@link LocalizedString} in this {@link InternationalString} you can register an
 * {@link PropertyChangeListener}.
 * <p>
 * The {@link InternationalString} implements the {@link XmlSupport}. To be able to use more than
 * one {@link InternationalString} property in one object use the
 * {@link InternationalStringXmlHelper}.
 * <p>
 * Note that this class does not support a default locale as none is needed at design time. However,
 * at runtime {@link org.faktorips.values.DefaultInternationalString} is initialized from the XML
 * written by this class and the {@code DefaultInternationalString} class needs a default locale.
 * {@code org.faktorips.devtools.stdbuilder.productcmpt.ProductCmptXMLBuilder} is used to add the
 * default locale to a product component's XML.
 * 
 * @author dirmeier
 */
public class InternationalString implements IInternationalString {

    private static final DistinctElementComparator<LocalizedString> COMPARATOR = DistinctElementComparator
            .createComparator(Comparator.comparing(LocalizedString::getValue));

    private final Map<Locale, LocalizedString> localizedStringMap = new LinkedHashMap<>();

    private PropertyChangeSupport changes = new PropertyChangeSupport(this);

    /**
     * The default constructor. Consider to register a {@link PropertyChangeListener} to get
     * notified about changes.
     */
    public InternationalString() {
        // default constructor
    }

    /**
     * Construct the object and register the given observer.
     * 
     * @param listener The {@link PropertyChangeListener} you want to register to get notified about
     *            changes
     */
    public InternationalString(PropertyChangeListener listener) {
        this();
        changes.addPropertyChangeListener(listener);
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
        changes.firePropertyChange("localizedString", oldText, localizedStringToSet); //$NON-NLS-1$
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
        return Objects.hash(localizedStringMap);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || !(obj instanceof InternationalString other)) {
            return false;
        }
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
        if ((otherLocalizedStringMapValues == null) || (values.size() != otherLocalizedStringMapValues.size())) {
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
        return new LocalizedString(locale, IpsStringUtils.EMPTY);
    }

    @Override
    public int compareTo(IInternationalString o) {
        return COMPARATOR.compare(values(), o.values());
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        changes.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        changes.removePropertyChangeListener(l);
    }

    @Override
    public boolean hasValueFor(Locale locale) {
        return localizedStringMap != null && localizedStringMap.containsKey(locale);
    }

}
