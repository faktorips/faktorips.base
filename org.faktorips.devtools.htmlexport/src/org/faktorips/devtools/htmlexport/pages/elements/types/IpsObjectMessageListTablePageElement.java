/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.types;

import org.faktorips.devtools.core.internal.model.ipsobject.Description;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;

/**
 * Represents a table with the {@link Message}s of a {@link MessageList} as rows and the attributes
 * of the {@link Message} as columns
 * 
 * @author dicker
 * 
 */
public class IpsObjectMessageListTablePageElement extends MessageListTablePageElement {
    /**
     * Creates a {@link IpsObjectMessageListTablePageElement} for the specified {@link MessageList}
     * 
     */
    public IpsObjectMessageListTablePageElement(MessageList messageList, DocumentationContext context) {
        super(messageList, context);
    }

    @Override
    protected IPageElement createInvalidObjectPropertiesItem(ObjectProperty objectProperty) {
        if (objectProperty.getObject() instanceof Description) {
            IIpsElement element = ((Description)objectProperty.getObject()).getParent();
            return new TextPageElement(context.getLabel(element));
        }
        if (objectProperty.getObject() instanceof IIpsElement) {
            IIpsElement element = (IIpsElement)objectProperty.getObject();
            return new TextPageElement(context.getLabel(element));
        }
        return new TextPageElement(objectProperty.getProperty());
    }
}
