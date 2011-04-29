/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.enums;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.internal.model.enums.refactor.RenameEnumLiteralNameAttributeValueProcessor;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of <tt>IEnumLiteralNameAttributeValue</tt>, see the corresponding interface for
 * more details.
 * 
 * @see IEnumLiteralNameAttributeValue
 * 
 * @author Alexander Weickmann
 * 
 * @since 3.0
 */
public class EnumLiteralNameAttributeValue extends EnumAttributeValue implements IEnumLiteralNameAttributeValue {

    public EnumLiteralNameAttributeValue(IEnumValue parent, String id) throws CoreException {
        super(parent, id);
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);

        if (getValue() == null) {
            return;
        }

        char[] characters = getValue().toCharArray();
        for (int i = 0; i < characters.length; i++) {
            boolean validCharacter = i == 0 ? Character.isJavaIdentifierStart(characters[i]) : Character
                    .isJavaIdentifierPart(characters[i]);
            if (!validCharacter) {
                String text = NLS.bind(Messages.EnumLiteralNameAttributeValue_ValueIsNotAValidJavaIdentifier,
                        getValue());
                Message msg = new Message(MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_VALUE_IS_NO_VALID_JAVA_IDENTIFIER, text,
                        Message.ERROR, this, PROPERTY_VALUE);
                list.add(msg);
                break;
            }
        }
    }

    @Override
    public ProcessorBasedRefactoring getRenameRefactoring() {
        return new ProcessorBasedRefactoring(new RenameEnumLiteralNameAttributeValueProcessor(this));
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(IEnumLiteralNameAttributeValue.XML_TAG);
    }

    @Override
    public String getName() {
        return getValue();
    }

}
