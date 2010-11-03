/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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
import org.faktorips.devtools.core.builder.JavaNamingConvention;
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

    /**
     * Sets the actual value, transformed to a valid literal name.
     * <p>
     * This could be a programming language dependent implementation in the future but for now all
     * letters will be transformed to upper case letters and all spaces will be transformed to
     * underscores.
     */
    @Override
    public void setValue(String value) {
        if (value == null) {
            super.setValue(null);
            return;
        }

        String val = JavaNamingConvention.ECLIPSE_STANDARD.getConstantClassVarName(value);
        val = val.replaceAll("[Ää]", "AE"); //$NON-NLS-1$ //$NON-NLS-2$
        val = val.replaceAll("[Öö]", "OE"); //$NON-NLS-1$ //$NON-NLS-2$
        val = val.replaceAll("[Üü]", "UE"); //$NON-NLS-1$ //$NON-NLS-2$
        val = val.replaceAll("[ß]", "SS"); //$NON-NLS-1$ //$NON-NLS-2$
        val = val.replaceAll("[^A-Za-z0-9]", "_"); //$NON-NLS-1$ //$NON-NLS-2$
        super.setValue(val);
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);

        if (getValue() != null) {
            char[] charArray = getValue().toCharArray();
            if (charArray.length > 0 && Character.isDigit(charArray[0])) {
                String text = NLS.bind(Messages.EnumLiteralNameAttributeValue_ValueIsNumber, getValue());
                Message msg = new Message(MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_VALUE_IS_NUMBER, text, Message.ERROR,
                        this, PROPERTY_VALUE);
                list.add(msg);
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
