/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.ipsobject;

import java.util.Locale;

import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.ILabel;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of {@link ILabel}.
 * 
 * @author Alexander Weickmann
 */
public class Label extends AtomicIpsObjectPart implements ILabel {

    private Locale locale;

    private String value;

    private String pluralValue;

    /**
     * @param ipsObjectPartContainer The parent {@link IIpsObjectPartContainer} this is a label for.
     * @param id A unique ID for this label.
     */
    public Label(IIpsObjectPartContainer ipsObjectPartContainer, String id) {
        super(ipsObjectPartContainer, id);
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public String getValue() {
        return value;
    }

    public String getPluralValue() {
        return pluralValue;
    }

    @Override
    public void setLocale(Locale locale) {
        ArgumentCheck.notNull(locale);
        this.locale = locale;
    }

    @Override
    public void setPluralValue(String pluralValue) {
        this.pluralValue = pluralValue;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void initFromXml(Element element, String id) {
        locale = new Locale(element.getAttribute("locale")); //$NON-NLS-1$
        value = element.getAttribute("value"); //$NON-NLS-1$
        pluralValue = element.getAttribute("pluralValue"); //$NON-NLS-1$

        super.initFromXml(element, id);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);

        element.setAttribute(PROPERTY_LOCALE, locale.getLanguage());
        element.setAttribute(PROPERTY_VALUE, value);
        element.setAttribute(PROPERTY_PLURAL_VALUE, pluralValue);
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG_NAME);
    }

    @Override
    public boolean isDescriptionChangable() {
        return false;
    }

}
