/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.runtime.Assert;
import org.faktorips.devtools.core.model.IInternationalString;
import org.faktorips.devtools.core.model.ILocalizedString;
import org.faktorips.devtools.core.model.XmlSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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

    private final Map<Locale, ILocalizedString> localizedStringMap = new LinkedHashMap<Locale, ILocalizedString>();

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
    public ILocalizedString get(Locale locale) {
        return localizedStringMap.get(locale);
    }

    @Override
    public void add(ILocalizedString localizedString) {
        Assert.isNotNull(localizedString);
        ILocalizedString oldText = localizedStringMap.put(localizedString.getLocale(), localizedString);
        if (!localizedString.equals(oldText)) {
            setChanged();
        }
        notifyObservers(localizedString);
    }

    /**
     * Returning all values of this {@link InternationalString} ordered by insertion.
     * 
     * {@inheritDoc}
     */
    @Override
    public Collection<ILocalizedString> values() {
        return localizedStringMap.values();
    }

    @Override
    public void initFromXml(Element element) {
        if (!element.getNodeName().equals(XML_TAG)) {
            return;
        }
        localizedStringMap.clear();
        element.getChildNodes();
        NodeList nl = element.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node item = nl.item(i);
            if (item.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element partEl = (Element)item;
            if (partEl.getNodeName().equals(XML_ELEMENT_LOCALIZED_STRING)) {
                String localeString = partEl.getAttribute(XML_ATTR_LOCALE);
                Locale locale = new Locale(localeString);
                String value = partEl.getAttribute(XML_ATTR_TEXT);
                LocalizedString localizedString = new LocalizedString(locale, value);
                localizedStringMap.put(localizedString.getLocale(), localizedString);
            }
        }
    }

    @Override
    public Element toXml(Document doc) {
        Element element = doc.createElement(XML_TAG);
        for (ILocalizedString localizedString : localizedStringMap.values()) {
            Element partElement = doc.createElement(XML_ELEMENT_LOCALIZED_STRING);
            Locale locale = localizedString.getLocale();
            partElement.setAttribute(XML_ATTR_LOCALE, locale.toString());
            partElement.setAttribute(XML_ATTR_TEXT, localizedString.getValue());
            element.appendChild(partElement);
        }
        return element;
    }

}
