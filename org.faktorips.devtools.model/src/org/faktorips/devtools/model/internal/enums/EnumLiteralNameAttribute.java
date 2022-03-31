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

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of <code>IEnumLiteralNameAttribute</code>, see the corresponding interface for
 * more details.
 * 
 * @see org.faktorips.devtools.model.enums.IEnumLiteralNameAttribute
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.4
 */
public class EnumLiteralNameAttribute extends EnumAttribute implements IEnumLiteralNameAttribute {

    /**
     * The name of the <code>IEnumAttribute</code> that is used as default value provider for
     * enumeration literals.
     */
    private String defaultValueProviderAttribute;

    /**
     * Creates a new <code>IEnumLiteralNameAttribute</code>.
     * 
     * @param parent The <code>IEnumType</code> this <code>IEnumLiteralNameAttribute</code> belongs
     *            to.
     * @param id A unique ID for this <code>IEnumLiteralNameAttribute</code>.
     */
    public EnumLiteralNameAttribute(IEnumType parent, String id) {
        super(parent, id);
        defaultValueProviderAttribute = ""; //$NON-NLS-1$
    }

    @Override
    public void setDefaultValueProviderAttribute(String defaultValueProviderAttributeName) {
        ArgumentCheck.notNull(defaultValueProviderAttributeName);

        String oldValue = defaultValueProviderAttribute;
        defaultValueProviderAttribute = defaultValueProviderAttributeName;
        valueChanged(oldValue, defaultValueProviderAttributeName);
    }

    @Override
    public String getDefaultValueProviderAttribute() {
        return defaultValueProviderAttribute;
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        defaultValueProviderAttribute = element.getAttribute(PROPERTY_DEFAULT_VALUE_PROVIDER_ATTRIBUTE);
        super.initPropertiesFromXml(element, id);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        if (IpsStringUtils.isNotBlank(defaultValueProviderAttribute)) {
            element.setAttribute(PROPERTY_DEFAULT_VALUE_PROVIDER_ATTRIBUTE, defaultValueProviderAttribute);
        }
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) {
        super.validateThis(list, ipsProject);
        validateIsNeeded(list);
        if (list.containsErrorMsg()) {
            return;
        }
        validateDefaultValueProviderAttribute(list);
    }

    /**
     * Validates whether this <code>IEnumLiteralNameAttribute</code> is needed by the
     * <code>IEnumType</code> it belongs to.
     */
    private void validateIsNeeded(MessageList list) {
        IEnumType enumType = getEnumType();
        if (enumType.isAbstract()) {
            String text = Messages.EnumLiteralNameAttribute_NotNeeded;
            Message msg = new Message(MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_NOT_NEEDED, text, Message.ERROR, this);
            list.add(msg);
        }
    }

    /** Validates the <code>defaultValueProviderAttribute</code> property. */
    private void validateDefaultValueProviderAttribute(MessageList list) {
        // Pass validation if no provider is specified.
        if (StringUtils.isEmpty(defaultValueProviderAttribute)) {
            return;
        }
        validateValueProviderAttributeExists(list);
        validateValueProviderAttributeHasStringDatatype(list);
    }

    private void validateValueProviderAttributeExists(MessageList list) {
        IEnumType enumType = getEnumType();
        if (isValueProviderAttributeMissing(enumType)) {
            String text = MessageFormat.format(
                    Messages.EnumLiteralNameAttribute_DefaultValueProviderAttributeDoesNotExist,
                    defaultValueProviderAttribute);
            Message msg = new Message(
                    MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_DEFAULT_VALUE_PROVIDER_ATTRIBUTE_DOES_NOT_EXIST, text,
                    Message.ERROR, this, PROPERTY_DEFAULT_VALUE_PROVIDER_ATTRIBUTE);
            list.add(msg);
        }
    }

    private boolean isValueProviderAttributeMissing(IEnumType enumType) {
        return !(enumType.containsEnumAttributeIncludeSupertypeCopies(defaultValueProviderAttribute));
    }

    private void validateValueProviderAttributeHasStringDatatype(MessageList list) {
        IEnumType enumType = getEnumType();
        if (isValueProviderAttributeMissing(enumType)) {
            return;
        }
        IEnumAttribute providerAttribute = enumType
                .getEnumAttributeIncludeSupertypeCopies(defaultValueProviderAttribute);
        Datatype datatype = providerAttribute.findDatatype(getIpsProject());
        if (datatype != null) {
            if (!(datatype.equals(Datatype.STRING))) {
                String text = MessageFormat.format(
                        Messages.EnumLiteralNameAttribute_DefaultValueProviderAttributeNotOfDatatypeString,
                        defaultValueProviderAttribute);
                Message msg = new Message(
                        MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_DEFAULT_VALUE_PROVIDER_ATTRIBUTE_NOT_OF_DATATYPE_STRING,
                        text, Message.ERROR, this, PROPERTY_DEFAULT_VALUE_PROVIDER_ATTRIBUTE);
                list.add(msg);
            }
        }
    }

    /**
     * Not supported by <code>IEnumLiteralNameAttribute</code>s.
     * 
     * @throws UnsupportedOperationException If the operation is called.
     */
    @Override
    public void setIdentifier(boolean usedAsIdInFaktorIpsUi) {
        throw new UnsupportedOperationException("The identifier property is not used by EnumLiteralNameAttributes."); //$NON-NLS-1$
    }

    /**
     * Not supported by <code>IEnumLiteralNameAttribute</code>s.
     * 
     * @throws UnsupportedOperationException If the operation is called.
     */
    @Override
    public void setInherited(boolean isInherited) {
        throw new UnsupportedOperationException("The inherited property is not used by EnumLiteralNameAttributes."); //$NON-NLS-1$
    }

    /**
     * Not supported by <code>IEnumLiteralNameAttribute</code>s.
     * 
     * @throws UnsupportedOperationException If the operation is called.
     */
    @Override
    public void setUsedAsNameInFaktorIpsUi(boolean usedAsNameInFaktorIpsUi) {
        throw new UnsupportedOperationException("The usedAsName property is not used by EnumLiteralNameAttributes."); //$NON-NLS-1$
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(IEnumLiteralNameAttribute.XML_TAG);
    }

}
