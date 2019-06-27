/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
            return new TextPageElement(getContext().getLabel(element), getContext());
        }
        if (objectProperty.getObject() instanceof IIpsElement) {
            IIpsElement element = (IIpsElement)objectProperty.getObject();
            return new TextPageElement(getContext().getLabel(element), getContext());
        }
        return new TextPageElement(objectProperty.getProperty(), getContext());
    }
}
