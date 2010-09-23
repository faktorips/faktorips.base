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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.ILabel;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
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
        value = ""; //$NON-NLS-1$
        pluralValue = ""; //$NON-NLS-1$
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
        Locale oldValue = this.locale;
        this.locale = locale;
        valueChanged(oldValue, locale);
    }

    public void setLocaleWithoutChangeEvent(Locale locale) {
        this.locale = locale;
    }

    @Override
    public void setPluralValue(String pluralValue) {
        if (pluralValue == null) {
            pluralValue = ""; //$NON-NLS-1$
        }
        String oldValue = this.pluralValue;
        this.pluralValue = pluralValue;
        valueChanged(oldValue, pluralValue);
    }

    @Override
    public void setValue(String value) {
        if (value == null) {
            value = ""; //$NON-NLS-1$
        }
        String oldValue = this.value;
        this.value = value;
        valueChanged(oldValue, value);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        String localeCode = element.getAttribute(PROPERTY_LOCALE);
        locale = localeCode.equals("") ? null : new Locale(localeCode); //$NON-NLS-1$
        value = element.getAttribute(PROPERTY_VALUE);
        pluralValue = element.getAttribute(PROPERTY_PLURAL_VALUE);

        super.initPropertiesFromXml(element, id);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);

        element.setAttribute(PROPERTY_LOCALE, (locale == null) ? "" : locale.getLanguage()); //$NON-NLS-1$
        element.setAttribute(PROPERTY_VALUE, value);
        element.setAttribute(PROPERTY_PLURAL_VALUE, pluralValue);
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        if (locale == null) {
            validateLocaleMissing(list);
        } else {
            validateLocaleSupported(list);
        }
    }

    private void validateLocaleMissing(MessageList list) {
        String text = Messages.Label_msgLocaleMissing;
        Message msg = new Message(ILabel.MSGCODE_LOCALE_MISSING, text, Message.ERROR, this, ILabel.PROPERTY_LOCALE);
        list.add(msg);
    }

    private void validateLocaleSupported(MessageList list) {
        // Only the project of the label itself must support the language
        IIpsProject ipsProject = getIpsProject();

        boolean localeSupported = ipsProject.getProperties().isSupportedLanguage(locale);
        if (!(localeSupported)) {
            String text = NLS.bind(Messages.Label_msgLocaleNotSupportedByProject, locale.getLanguage());
            Message msg = new Message(ILabel.MSGCODE_LOCALE_NOT_SUPPORTED_BY_IPS_PROJECT, text, Message.WARNING, this,
                    ILabel.PROPERTY_LOCALE);
            list.add(msg);
        }
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG_NAME);
    }

}
