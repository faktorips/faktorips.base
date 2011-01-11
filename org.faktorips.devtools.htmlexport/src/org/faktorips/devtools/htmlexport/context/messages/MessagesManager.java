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

package org.faktorips.devtools.htmlexport.context.messages;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.htmlexport.HtmlExportPlugin;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;

public class MessagesManager {

    private final Properties messages = new Properties();
    private final DocumentationContext context;
    private final List<String> notFoundMessages;

    private final Class<?>[] importMessagesClasses = { IpsObjectType.class };

    public MessagesManager(DocumentationContext context) {
        this.context = context;
        this.notFoundMessages = new ArrayList<String>();
        initialize(context.getDescriptionLocale());
    }

    private void initialize(Locale locale) {
        initializeStandardMessages();

        if (isEnglish(locale)) {
            return;
        }

        initializeLocalizedMessages(locale);
    }

    private void initializeStandardMessages() {
        initializeLocalizedMessages(Locale.UK);
    }

    private boolean isEnglish(Locale locale) {
        String nl = locale.getLanguage();
        if (nl.length() > 2) {
            nl = nl.substring(0, 2);
        }
        return "en".equals(nl); //$NON-NLS-1$
    }

    private void initializeLocalizedMessages(Locale locale) {
        loadMessages(getClass(), locale);

        for (Class<?> clazz : importMessagesClasses) {
            loadMessages(clazz, locale);
        }

    }

    protected void loadMessages(Class<?> clazz, Locale locale) {
        String resourceName = clazz.getPackage().getName().replace('.', File.separatorChar) + File.separatorChar
                + getMessagesFileName(locale);

        final InputStream inputStream = HtmlExportPlugin.getDefault().getClass().getClassLoader()
                .getResourceAsStream(resourceName);

        if (inputStream == null) {
            context.addStatus(new IpsStatus(IStatus.WARNING, "Messages for Locale " + locale + " not found")); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        try {
            messages.load(inputStream);
        } catch (IOException e) {
            context.addStatus(new IpsStatus(IStatus.WARNING, "Messages for Locale " + locale + " couldn't be loaded")); //$NON-NLS-1$ //$NON-NLS-2$
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                // nothing to do
            }
        }
    }

    private String getMessagesFileName(Locale locale) {
        if (locale.getLanguage().equals(Locale.UK.getLanguage())) {
            return "messages.properties"; //$NON-NLS-1$
        }
        return "messages_" + locale.getLanguage() + ".properties"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public String getMessage(String messageId) {
        if (messages.containsKey(messageId)) {
            return messages.getProperty(messageId);
        }
        if (!notFoundMessages.contains(messageId)) {
            context.addStatus(new IpsStatus(IStatus.INFO, "Message with Id " + messageId + " not found.")); //$NON-NLS-1$ //$NON-NLS-2$
            notFoundMessages.add(messageId);
        }
        return messageId;
    }

    public String getMessage(Object object) {
        if (object instanceof IpsObjectType) {
            IpsObjectType objectType = (IpsObjectType)object;
            String messageId = "IpsObjectType_name" + objectType.getId(); //$NON-NLS-1$
            String message = getMessage(messageId);
            return messageId.equals(message) ? objectType.getDisplayName() : message;
        }
        return object.toString();
    }
}
