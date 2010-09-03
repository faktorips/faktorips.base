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
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of {@link IDescription}.
 * 
 * @author Alexander Weickmann
 */
public class Description extends AtomicIpsObjectPart implements IDescription {

    private Locale locale;

    private String text = ""; //$NON-NLS-1$

    /**
     * @param ipsObjectPartContainer The parent {@link IIpsObjectPartContainer} this is a
     *            description for.
     * @param id A unique ID for this description.
     */
    public Description(IIpsObjectPartContainer ipsObjectPartContainer, String id) {
        super(ipsObjectPartContainer, id);
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public String getText() {
        return text;
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
    public void setText(String text) {
        String oldValue = this.text;
        if (text == null) {
            this.text = ""; //$NON-NLS-1$
        } else {
            this.text = text;
        }
        valueChanged(oldValue, this.text);
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        // TODO AW: Out commented for now, it needs to be decided if this will be necessary
        // Right now it conflicts with the integration test projects that cannot be migrated yet
        // validateLocale(list, ipsProject);
    }

    private void validateLocale(MessageList list, IIpsProject ipsProject) {
        if (locale == null) {
            String text = Messages.Description_msgLocaleMissing;
            Message msg = new Message(IDescription.MSGCODE_LOCALE_MISSING, text, Message.ERROR, this,
                    IDescription.PROPERTY_LOCALE);
            list.add(msg);
            return;
        }

        boolean localeSupported = ipsProject.getProperties().isSupportedLanguage(locale);
        if (!(localeSupported)) {
            String text = NLS.bind(Messages.Description_msgLocaleNotSupportedByProject, locale.getLanguage());
            Message msg = new Message(IDescription.MSGCODE_LOCALE_NOT_SUPPORTED_BY_IPS_PROJECT, text, Message.ERROR,
                    this, IDescription.PROPERTY_LOCALE);
            list.add(msg);
        }
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        String localeCode = element.getAttribute(PROPERTY_LOCALE);
        locale = localeCode.equals("") ? null : new Locale(localeCode); //$NON-NLS-1$
        text = element.getTextContent();

        super.initPropertiesFromXml(element, id);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);

        element.setAttribute(PROPERTY_LOCALE, (locale == null) ? "" : locale.getLanguage()); //$NON-NLS-1$
        element.setTextContent(text);
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG_NAME);
    }

}
