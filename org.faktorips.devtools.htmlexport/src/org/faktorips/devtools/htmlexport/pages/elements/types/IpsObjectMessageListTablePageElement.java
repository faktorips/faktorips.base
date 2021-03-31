/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.types;

import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IDescription;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;

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
        if (objectProperty.getObject() instanceof IDescription) {
            IIpsElement element = ((IDescription)objectProperty.getObject()).getParent();
            return new TextPageElement(getContext().getLabel(element), getContext());
        }
        if (objectProperty.getObject() instanceof IIpsElement) {
            IIpsElement element = (IIpsElement)objectProperty.getObject();
            return new TextPageElement(getContext().getLabel(element), getContext());
        }
        return new TextPageElement(objectProperty.getProperty(), getContext());
    }
}
