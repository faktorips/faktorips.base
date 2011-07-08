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

package org.faktorips.devtools.htmlexport.context.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;

import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.util.LocalizedStringsSet;

/**
 * This class manages the Messages for the HtmlExport. This is necessary, because the {@link Locale}
 * of the HtmlExport may not be the {@link Locale} of the workspace. Because of this the eclipse
 * mechanism for localized messages is not useful here.
 * 
 * @author dicker
 */
public final class MessagesManager {

    private final LocalizedStringsSet localizedStringsSet;
    private final DocumentationContext context;
    private final List<String> notFoundMessages;

    public MessagesManager(DocumentationContext context) {
        this.context = context;
        this.notFoundMessages = new ArrayList<String>();
        localizedStringsSet = new LocalizedStringsSet(this.getClass());
    }

    /**
     * 
     * returns a message for the given messageId
     */
    public String getMessage(String messageId) {
        try {
            return localizedStringsSet.getString(messageId, context.getDescriptionLocale());
        } catch (MissingResourceException e) {
            context.addStatus(new IpsStatus(IStatus.INFO, "Message with Id " + messageId + " not found.")); //$NON-NLS-1$ //$NON-NLS-2$
            notFoundMessages.add(messageId);
        }
        return messageId;
    }

    /**
     * returns a message for the given object
     */
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
