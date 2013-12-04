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

package org.faktorips.devtools.core.internal.model.enums;

import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.internal.model.value.InternationalStringValue;
import org.faktorips.devtools.core.internal.model.value.StringValue;
import org.faktorips.devtools.core.internal.model.value.ValueUtil;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.value.IValue;
import org.faktorips.devtools.core.model.value.ValueFactory;
import org.faktorips.devtools.core.model.value.ValueType;
import org.faktorips.devtools.core.model.value.ValueTypeMismatch;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.faktorips.values.LocalizedString;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of <tt>IEnumAttributeValue</tt>, see the corresponding interface for more details.
 * 
 * @see org.faktorips.devtools.core.model.enums.IEnumAttributeValue
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumAttributeValue extends AtomicIpsObjectPart implements IEnumAttributeValue {

    private static final String IS_NULL = "isNull"; //$NON-NLS-1$

    /** The actual value that is being stored by this object. */
    private IValue<?> value;

    private final Observer valueObserver;

    /**
     * Creates a new <tt>IEnumAttributeValue</tt>.
     * 
     * @param parent The <tt>IEnumValue</tt> this <tt>IEnumAttributeValue</tt> belongs to.
     * @param id A unique ID for this <tt>IEnumAttributeValue</tt>.
     * 
     * @throws CoreException If an error occurs while initializing the object.
     */
    public EnumAttributeValue(IEnumValue parent, String id) throws CoreException {
        super(parent, id);
        valueObserver = new Observer() {

            @Override
            public void update(Observable arg0, Object newValue) {
                valueChanged(null, newValue);
            }
        };
        setValueInternal(ValueFactory.createStringValue(null));
    }

    @Override
    public String getCaption(Locale locale) throws CoreException {
        IEnumAttribute foundEnumAttribute = findEnumAttribute(getIpsProject());
        if (foundEnumAttribute != null) {
            return foundEnumAttribute.getLabelValue(locale);
        } else {
            return super.getCaption(locale);
        }
    }

    @Override
    public String getLastResortCaption() {
        IEnumAttribute foundEnumAttribute = findEnumAttribute(getIpsProject());
        if (foundEnumAttribute != null) {
            return StringUtils.capitalize(foundEnumAttribute.getName());
        } else {
            return super.getLastResortCaption();
        }
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(IS_NULL, Boolean.toString(isNullValue()));
        if (getValue() != null) {
            element.appendChild(getValue().toXml(element.getOwnerDocument()));
        }
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        setValueInternal(ValueFactory.createValue(element));
    }

    @Override
    public IEnumAttribute findEnumAttribute(IIpsProject ipsProject) {
        ArgumentCheck.notNull(ipsProject);

        IEnumValue enumValue = getEnumValue();
        IEnumValueContainer valueContainer = enumValue.getEnumValueContainer();
        IEnumType enumType = valueContainer.findEnumType(ipsProject);
        if (enumType == null) {
            return null;
        }

        return getEnumAttribute(enumType);
    }

    /**
     * Returns the <tt>IEnumAttribute</tt> this <tt>IEnumAttributeValue</tt> is a value for.
     * 
     * @param enumType The <tt>IEnumType</tt> this <tt>IEnumAttributeValue</tt> is referring to.
     */
    private IEnumAttribute getEnumAttribute(IEnumType enumType) {
        IEnumValue enumValue = getEnumValue();

        // Check number of EnumAttributeValues matching number of EnumAttributes.
        int attributeValueIndex = enumValue.getIndexOfEnumAttributeValue(this);
        int enumAttributesCount = enumType.getEnumAttributesCountIncludeSupertypeCopies(true);
        if (!(enumAttributesCount == enumValue.getEnumAttributeValuesCount()) || attributeValueIndex == -1
                || enumAttributesCount < attributeValueIndex + 1) {
            return null;
        }

        List<IEnumAttribute> enumAttributes = enumType.getEnumAttributesIncludeSupertypeCopies(true);
        return enumAttributes.get(attributeValueIndex);
    }

    @Override
    public boolean isEnumLiteralNameAttributeValue() {
        return this instanceof IEnumLiteralNameAttributeValue;
    }

    private void setValueInternal(IValue<?> value) {
        if (this.value != null) {
            this.value.deleteObserver(valueObserver);
        }
        this.value = value;
        if (this.value != null) {
            this.value.addObserver(valueObserver);
        }
    }

    @Override
    public String getStringValue() {
        if (getValue() != null) {
            return getValue().getContentAsString();
        }
        return null;
    }

    @Override
    public ValueType getValueType() {
        return ValueType.getValueType(getValue());
    }

    @Override
    public IValue<?> getValue() {
        return this.value;
    }

    @Override
    public void setValue(IValue<?> newValue) {
        IValue<?> oldValue = getValue();
        setValueInternal(newValue);
        valueChanged(oldValue, newValue);
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        IEnumAttribute enumAttribute = findEnumAttribute(ipsProject);
        if (enumAttribute == null) {
            return;
        }

        IEnumValueContainer enumValueContainer = getEnumValue().getEnumValueContainer();
        IEnumType enumType = enumAttribute.getEnumType();
        if (enumValueContainer instanceof IEnumType) {
            if (enumType.isCapableOfContainingValues() || !enumType.containsValues()) {
                return;
            }
        }

        validateMultilingual(list, enumAttribute);

        // DataType Value parsable?
        if (getValue() != null) {
            getValue().validate(enumAttribute.findDatatype(ipsProject), getParent().getIpsProject(), list,
                    new ObjectProperty(this, PROPERTY_VALUE));

            // Unique identifier and literal name validations.
            if (isUniqueIdentifierEnumAttributeValue(enumAttribute)) {
                validateUniqueIdentifierEnumAttributeValue(list);
            }
        }
    }

    private void validateMultilingual(MessageList list, IEnumAttribute enumAttribute) {
        String defaultProjectLanguage = getIpsProject().getReadOnlyProperties().getDefaultLanguage().getLocale()
                .getLanguage();
        ValueTypeMismatch typeMismatch = checkValueTypeMismatch(enumAttribute);
        if (ValueTypeMismatch.STRING_TO_INTERNATIONAL_STRING.equals(typeMismatch)) {
            list.add(new Message(MSGCODE_INVALID_VALUE_TYPE, NLS.bind(Messages.EnumAttributeValue_MultiLingual,
                    enumAttribute.getName(), defaultProjectLanguage), Message.ERROR, new ObjectProperty(this,
                    PROPERTY_VALUE)));
        } else if (ValueTypeMismatch.INTERNATIONAL_STRING_TO_STRING.equals(typeMismatch)) {
            list.add(new Message(MSGCODE_INVALID_VALUE_TYPE, NLS.bind(Messages.EnumAttributeValue_NotMultiLingual,
                    enumAttribute.getName(), defaultProjectLanguage), Message.ERROR, new ObjectProperty(this,
                    PROPERTY_VALUE)));
        }
    }

    @Override
    public ValueTypeMismatch checkValueTypeMismatch(IEnumAttribute enumAttribute) {
        return ValueTypeMismatch.getMismatch(getValue(), enumAttribute.isMultilingual());
    }

    @Override
    public void fixValueType(boolean multilingual) {
        ValueTypeMismatch typeMismatch = ValueTypeMismatch.getMismatch(getValue(), multilingual);
        if (ValueTypeMismatch.STRING_TO_INTERNATIONAL_STRING.equals(typeMismatch)) {
            convertStringToInternationalString();
        } else if (ValueTypeMismatch.INTERNATIONAL_STRING_TO_STRING.equals(typeMismatch)) {
            convertInternationalStringToString();
        }
    }

    private void convertStringToInternationalString() {
        InternationalStringValue internationalStringValue = new InternationalStringValue();
        if (getStringValue() != null) {
            internationalStringValue.getContent().add(
                    new LocalizedString(getIpsProject().getReadOnlyProperties().getDefaultLanguage().getLocale(),
                            getStringValue()));
        }
        setValueInternal(internationalStringValue);
    }

    private void convertInternationalStringToString() {
        String content = getValue().getDefaultLocalizedContent(getIpsProject());
        setValueInternal(new StringValue(content));
    }

    /**
     * Validations necessary if this <tt>IEnumAttributeValue</tt> refers to a unique identifier
     * <tt>IEnumAttribute</tt>.
     */
    private void validateUniqueIdentifierEnumAttributeValue(MessageList list) {
        String text;
        Message validationMessage;

        // A unique identifier EnumAttributeValue must not be empty.
        IValue<?> uniqueIdentifierValue = getValue();
        ValueUtil valueUtil = ValueUtil.createUtil(uniqueIdentifierValue);
        boolean uniqueIdentifierValueMissing = valueUtil.isPartlyEmpty(getIpsProject());
        if (uniqueIdentifierValueMissing) {
            text = Messages.EnumAttributeValue_UniqueIdentifierValueEmpty;
            validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_VALUE_UNIQUE_IDENTIFIER_VALUE_EMPTY, text,
                    Message.ERROR, this, PROPERTY_VALUE);
            list.add(validationMessage);
            return;
        }

        // A unique identifier EnumAttributeValue must be truly unique.
        EnumValueContainer enumValueContainerImpl = (EnumValueContainer)getEnumValue().getEnumValueContainer();
        List<String> uniqueIdentifierViolations = enumValueContainerImpl.getUniqueIdentifierViolations(this);
        for (String invalidString : uniqueIdentifierViolations) {
            text = NLS.bind(Messages.EnumAttributeValue_UniqueIdentifierValueNotUnique, invalidString);
            validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_VALUE_UNIQUE_IDENTIFIER_NOT_UNIQUE, text,
                    Message.ERROR, this, PROPERTY_VALUE);
            list.add(validationMessage);
        }
    }

    /**
     * Returns whether this <tt>IEnumAttributeValue</tt> refers to a unique identifier
     * <tt>IEnumAttribute</tt>.
     */
    private boolean isUniqueIdentifierEnumAttributeValue(IEnumAttribute enumAttribute) {
        try {
            return enumAttribute.findIsUnique(getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    @Override
    public IEnumValue getEnumValue() {
        return (IEnumValue)getParent();
    }

    @Override
    public boolean isNullValue() {
        boolean isNull = true;
        if (getValue() != null) {
            isNull = getValue().getContent() == null;
        }
        return isNull;
    }
}
