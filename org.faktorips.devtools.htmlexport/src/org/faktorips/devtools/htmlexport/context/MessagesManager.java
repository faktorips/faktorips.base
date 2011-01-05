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

package org.faktorips.devtools.htmlexport.context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.htmlexport.HtmlExportPlugin;

public class MessagesManager {

    private final static String MESSAGES_FOLDER = "org/faktorips/devtools/htmlexport/context/messages/"; //$NON-NLS-1$
    private final Properties messages = new Properties();
    private final DocumentationContext context;

    public MessagesManager(DocumentationContext context) {
        this.context = context;
        initialize(context.getDescriptionLocale());
    }

    private void initialize(Locale locale) {
        initializeStandardMessages();

        if (equalsPlatformLocale(locale)) {
            return;
        }

        initializeLocalizedMessages(locale);
    }

    private void initializeStandardMessages() {
        initializeLocalizedMessages(Locale.UK);
    }

    private boolean equalsPlatformLocale(Locale locale) {
        return locale.getLanguage().equals(getPlatformLanguage());
    }

    public String getPlatformLanguage() {
        String nl = Platform.getNL();
        if (nl.length() > 2) {
            nl = nl.substring(0, 2);
        }
        return nl;
    }

    private void initializeLocalizedMessages(Locale locale) {
        String fileName = getMessagesFileName(locale);

        String resourceName = MESSAGES_FOLDER + fileName;
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
        context.addStatus(new IpsStatus(IStatus.WARNING, "Message with Id " + messageId + " not found.")); //$NON-NLS-1$ //$NON-NLS-2$
        return messageId;
    }
}
