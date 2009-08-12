/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.enums;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * Implementation of <code>IEnumAttributeValue</code>, see the corresponding interface for more
 * details.
 * 
 * @see org.faktorips.devtools.core.model.enums.IEnumAttributeValue
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumAttributeValue extends AtomicIpsObjectPart implements IEnumAttributeValue {

    /** The actual value that is being stored by this object. */
    private String value;

    /**
     * Creates a new <code>EnumAttributeValue</code>.
     * 
     * @param parent The enum value this enum attribute value belongs to.
     * @param id A unique id for this enum attribute value.
     * 
     * @throws CoreException If an error occurs while initializing the object.
     */
    public EnumAttributeValue(IEnumValue parent, int id) throws CoreException {
        super(parent, id);
        descriptionChangable = false;
        value = null;
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        if (value == null) {
            element.setAttribute("isNull", "true");
            Text textNode = XmlUtil.getTextNode(element);
            if (textNode != null) {
                textNode.setTextContent("");
            }
            return;
        } else {
            element.setAttribute("isNull", "false");
        }
        if (XmlUtil.getTextNode(element) == null) {
            XmlUtil.addNewTextChild(element.getOwnerDocument(), element, value);
        } else {
            XmlUtil.getTextNode(element).setTextContent(value);
        }
    }

    @Override
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        Boolean isNull = Boolean.valueOf(element.getAttribute("isNull"));
        if (isNull.booleanValue()) {
            value = null;
            return;
        }
        Text textNode = XmlUtil.getTextNode(element);
        if (textNode != null) {
            value = textNode.getTextContent();
        } else {
            value = "";
        }
    }

    public Image getImage() {
        return null;
    }

    public IEnumAttribute findEnumAttribute(IIpsProject ipsProject) throws CoreException {
        ArgumentCheck.notNull(ipsProject);

        IEnumValue enumValue = getEnumValue();
        IEnumValueContainer valueContainer = enumValue.getEnumValueContainer();
        IEnumType enumType = valueContainer.findEnumType(ipsProject);
        if (enumType == null) {
            return null;
        }

        // Check number of EnumAttributeValues matching number of EnumAttributes
        int attributeValueIndex = enumValue.getIndexOfEnumAttributeValue(this);
        boolean includeLiteralNames = valueContainer instanceof IEnumType;
        int enumAttributesCount = enumType.getEnumAttributesCountIncludeSupertypeCopies(includeLiteralNames);
        if (!(enumAttributesCount == enumValue.getEnumAttributeValuesCount())
                || enumAttributesCount < attributeValueIndex + 1) {
            return null;
        }

        List<IEnumAttribute> enumAttributes = enumType.getEnumAttributesIncludeSupertypeCopies(includeLiteralNames);
        return enumAttributes.get(attributeValueIndex);
    }

    /**
     * Returns whether this <tt>EnumAttributeValue</tt> refers to an
     * <tt>EnumLiteralNameAttribute</tt>.
     */
    private boolean isLiteralNameEnumAttributeValue(IEnumAttribute enumAttribute) throws CoreException {
        return enumAttribute instanceof IEnumLiteralNameAttribute;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        String oldValue = this.value;
        this.value = value;

        /*
         * Update unique identifier validation cache if this enum attribute value refers to a unique
         * enum attribute.
         */
        IEnumValue enumValue = getEnumValue();
        EnumValueContainer enumValueContainerImpl = (EnumValueContainer)enumValue.getEnumValueContainer();
        if (enumValueContainerImpl.isUniqueIdentifierValidationCacheInitialized()) {
            int index = enumValue.getIndexOfEnumAttributeValue(this);
            if (enumValueContainerImpl.containsValidationCacheUniqueIdentifier(index)) {
                enumValueContainerImpl.removeValidationCacheUniqueIdentifierEntry(index, oldValue, this);
                enumValueContainerImpl.addValidationCacheUniqueIdentifierEntry(index, value, this);
            }
        }

        valueChanged(oldValue, value);
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        IEnumAttribute enumAttribute = findEnumAttribute(ipsProject);
        if (enumAttribute == null) {
            return;
        }

        IEnumValueContainer enumValueContainer = getEnumValue().getEnumValueContainer();
        IEnumType enumType = enumAttribute.getEnumType();
        if (enumType.isAbstract() || (!(enumType.isContainingValues()) && enumValueContainer instanceof IEnumType)) {
            return;
        }

        // Value parsable?
        ValueDatatype valueDatatype = enumAttribute.findDatatype(ipsProject);
        if (valueDatatype != null) {
            if (!(valueDatatype.isParsable(value))) {
                String text = NLS.bind(Messages.EnumAttributeValue_ValueNotParsable, enumAttribute.getName(),
                        valueDatatype.getName());
                Message validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_VALUE_NOT_PARSABLE, text, Message.ERROR,
                        this, PROPERTY_VALUE);
                list.add(validationMessage);
            }
        }

        // Unique identifier and literal name validations.
        EnumValueContainer enumValueContainerImpl = (EnumValueContainer)getEnumValue().getEnumValueContainer();
        boolean cacheInitialized = true;
        if (!(enumValueContainerImpl.isUniqueIdentifierValidationCacheInitialized())) {
            cacheInitialized = enumValueContainerImpl.initUniqueIdentifierValidationCache();
        }

        if (cacheInitialized) {
            if (isUniqueIdentifierEnumAttributeValue()) {
                validateUniqueIdentifierEnumAttributeValue(list, enumAttribute);
                if (list.getNoOfMessages() == 0) {
                    if (isLiteralNameEnumAttributeValue(enumAttribute)) {
                        validateLiteralNameEnumAttributeValue(list, enumAttribute);
                    }
                }
            }
        }
    }

    /**
     * Validations necessary if this enum attribute value refers to an enum attribute that is used
     * as literal name.
     */
    private void validateLiteralNameEnumAttributeValue(MessageList list, IEnumAttribute enumAttribute)
            throws CoreException {

        // The identifier enum attribute value must be java conform.
        if (!(JavaConventions.validateIdentifier(value, "1.5", "1.5").isOK())) {
            String text = NLS.bind(Messages.EnumAttributeValue_LiteralNameValueNotJavaConform, enumAttribute.getName());
            Message validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_VALUE_LITERAL_NAME_NOT_JAVA_CONFORM, text,
                    Message.ERROR, this);
            list.add(validationMessage);
        }
    }

    /**
     * Validations necessary if this enum attribute value refers to a unique identifier enum
     * attribute.
     */
    private void validateUniqueIdentifierEnumAttributeValue(MessageList list, IEnumAttribute enumAttribute)
            throws CoreException {

        String text;
        Message validationMessage;

        // The unique identifier enum attribute value must not be empty.
        String uniqueIdentifierValue = getValue();
        boolean uniqueIdentifierValueMissing = (uniqueIdentifierValue == null) ? true
                : uniqueIdentifierValue.length() == 0 || uniqueIdentifierValue.equals("<null>");
        if (uniqueIdentifierValueMissing) {
            text = NLS.bind(Messages.EnumAttributeValue_UniqueIdentifierValueEmpty, enumAttribute.getName());
            validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_VALUE_UNIQUE_IDENTIFIER_VALUE_EMPTY, text,
                    Message.ERROR, this);
            list.add(validationMessage);
            return;
        }

        // The unique identifier enum attribute value must be unique.
        EnumValueContainer enumValueContainerImpl = (EnumValueContainer)getEnumValue().getEnumValueContainer();
        List<IEnumAttributeValue> cachedAttributeValues = enumValueContainerImpl
                .getValidationCacheListForUniqueIdentifier(getEnumValue().getIndexOfEnumAttributeValue(this),
                        getValue());
        if (cachedAttributeValues != null) {
            if (cachedAttributeValues.size() > 1) {
                text = NLS.bind(Messages.EnumAttributeValue_UniqueIdentifierValueNotUnique, enumAttribute.getName());
                validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_VALUE_UNIQUE_IDENTIFIER_NOT_UNIQUE, text,
                        Message.ERROR, this);
                list.add(validationMessage);
            }
        }
    }

    /** Returns whether this enum attribute value refers to a unique identifier enum attribute. */
    private boolean isUniqueIdentifierEnumAttributeValue() throws CoreException {
        EnumValueContainer enumValueContainerImpl = (EnumValueContainer)getEnumValue().getEnumValueContainer();
        return enumValueContainerImpl.containsValidationCacheUniqueIdentifier(getEnumValue()
                .getIndexOfEnumAttributeValue(this));
    }

    public IEnumValue getEnumValue() {
        return (IEnumValue)getParent();
    }

}
