/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.enums;

import java.text.MessageFormat;

import org.faktorips.devtools.model.enums.IEnumLiteralNameAttributeValue;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of <code>IEnumLiteralNameAttributeValue</code>, see the corresponding interface
 * for more details.
 * 
 * @see IEnumLiteralNameAttributeValue
 * 
 * @author Alexander Weickmann
 * 
 * @since 3.0
 */
public class EnumLiteralNameAttributeValue extends EnumAttributeValue implements IEnumLiteralNameAttributeValue {

    public EnumLiteralNameAttributeValue(EnumValue parent, String id) {
        super(parent, id);
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) {
        super.validateThis(list, ipsProject);

        if (isNullValue()) {
            return;
        }

        char[] characters = getValue().getContentAsString().toCharArray();
        for (int i = 0; i < characters.length; i++) {
            boolean validCharacter = i == 0 ? Character.isJavaIdentifierStart(characters[i])
                    : Character
                            .isJavaIdentifierPart(characters[i]);
            if (!validCharacter) {
                String text = MessageFormat.format(
                        Messages.EnumLiteralNameAttributeValue_ValueIsNotAValidJavaIdentifier,
                        getValue());
                Message msg = new Message(MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_VALUE_IS_NO_VALID_JAVA_IDENTIFIER, text,
                        Message.ERROR, this, PROPERTY_VALUE);
                list.add(msg);
                break;
            }
        }
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(IEnumLiteralNameAttributeValue.XML_TAG);
    }

    @Override
    public String getName() {
        return getStringValue();
    }

}
