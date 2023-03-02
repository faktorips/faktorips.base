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

import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.model.enums.IEnumLiteralNameAttributeValue;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.enums.IEnumValueContainer;
import org.faktorips.devtools.model.internal.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPart;
import org.faktorips.devtools.model.internal.value.InternationalStringValue;
import org.faktorips.devtools.model.internal.value.StringValue;
import org.faktorips.devtools.model.internal.value.ValueUtil;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.value.IValue;
import org.faktorips.devtools.model.value.ValueFactory;
import org.faktorips.devtools.model.value.ValueType;
import org.faktorips.devtools.model.value.ValueTypeMismatch;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.runtime.internal.InternationalStringXmlReaderWriter;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.values.LocalizedString;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Implementation of <code>IEnumAttributeValue</code>, see the corresponding interface for more
 * details.
 * 
 * @see org.faktorips.devtools.model.enums.IEnumAttributeValue
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumAttributeValue extends AtomicIpsObjectPart implements IEnumAttributeValue {

    private static final String IS_NULL = "isNull"; //$NON-NLS-1$

    /** The actual value that is being stored by this object. */
    private IValue<?> value;

    private final PropertyChangeListener propertyChangeListener;

    /**
     * Creates a new <code>IEnumAttributeValue</code>.
     * 
     * @param parent The <code>IEnumValue</code> this <code>IEnumAttributeValue</code> belongs to.
     * @param id A unique ID for this <code>IEnumAttributeValue</code>.
     * 
     * @throws IpsException If an error occurs while initializing the object.
     */
    public EnumAttributeValue(EnumValue parent, String id) {
        super(parent, id);
        propertyChangeListener = evt -> valueChanged(null, evt.getNewValue());
        setValueInternal(ValueFactory.createStringValue(null));
    }

    @Override
    public String getCaption(Locale locale) {
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
        element.removeAttribute(IpsObjectPart.PROPERTY_ID);
        if (getValue() != null) {
            Node valueNode = getValue().toXml(element.getOwnerDocument());
            element.appendChild(valueNode);
            /*
             * Default locale is needed only for runtime. As the XML is used for the designtime and
             * the runtime, defaultLocale is always generated (and ignored during design time).
             * 
             * TODO FIPS-4776 Generate default locale only for runtime XML
             */
            if (getValueType() == ValueType.INTERNATIONAL_STRING) {
                Locale defaultLocale = getIpsProject().getReadOnlyProperties().getDefaultLanguage().getLocale();
                InternationalStringXmlReaderWriter.setDefaultLocaleInXml(valueNode, defaultLocale);
            }
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
     * Returns the <code>IEnumAttribute</code> this <code>IEnumAttributeValue</code> is a value for.
     * t
     * 
     * @param enumType The <code>IEnumType</code> this <code>IEnumAttributeValue</code> is referring
     *            to.
     */
    private IEnumAttribute getEnumAttribute(IEnumType enumType) {
        IEnumValue enumValue = getEnumValue();

        // Check number of EnumAttributeValues matching number of EnumAttributes.
        int attributeValueIndex = enumValue.getIndexOfEnumAttributeValue(this);
        boolean includeLiteralName = getEnumValue().isEnumTypeValue();

        int enumAttributesCount = enumType.getEnumAttributesCountIncludeSupertypeCopies(includeLiteralName);
        if (!(enumAttributesCount == enumValue.getEnumAttributeValuesCount()) || attributeValueIndex == -1
                || enumAttributesCount < attributeValueIndex + 1) {
            return null;
        }

        List<IEnumAttribute> enumAttributes = enumType.getEnumAttributesIncludeSupertypeCopies(includeLiteralName);
        return enumAttributes.get(attributeValueIndex);
    }

    @Override
    public boolean isEnumLiteralNameAttributeValue() {
        return this instanceof IEnumLiteralNameAttributeValue;
    }

    private void setValueInternal(IValue<?> value) {
        if (this.value != null) {
            this.value.removePropertyChangeListener(propertyChangeListener);
        }
        this.value = value;
        if (this.value != null) {
            this.value.addPropertyChangeListener(propertyChangeListener);
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
        return value;
    }

    @Override
    public void setValue(IValue<?> newValue) {
        IValue<?> oldValue = getValue();
        setValueInternal(newValue);
        valueChanged(oldValue, newValue);
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) {
        IEnumAttribute enumAttribute = findEnumAttribute(ipsProject);
        if (enumAttribute == null) {
            return;
        }

        IEnumType enumType = enumAttribute.getEnumType();
        if (getEnumValue().isEnumTypeValue()) {
            if (enumType.isAbstract()) {
                return;
            }
        }

        validateMultilingual(list, enumAttribute);

        // DataType Value parsable?
        ValueDatatype datatype = enumAttribute.findDatatype(ipsProject);
        if (getValue() != null) {
            if (getEnumValue().isEnumTypeValue()) {
                datatype = enumAttribute.findDatatypeIgnoreEnumContents(ipsProject);
            } else {
                datatype = enumAttribute.findDatatype(ipsProject);
            }
            getValue().validate(datatype, enumAttribute.getDatatype(), getParent().getIpsProject(), list,
                    new ObjectProperty(this, PROPERTY_VALUE));

            // Unique identifier and literal name validations.
            if (isUniqueIdentifierEnumAttributeValue(enumAttribute)) {
                validateUniqueIdentifierEnumAttributeValue(list);
            }
        }

        IdentifierBoundaryValidator validator = new IdentifierBoundaryValidator(this, enumType, datatype, ipsProject);
        list.add(validator.validateIfPossible());
    }

    private void validateMultilingual(MessageList list, IEnumAttribute enumAttribute) {
        String defaultProjectLanguage = getIpsProject().getReadOnlyProperties().getDefaultLanguage().getLocale()
                .getLanguage();
        ValueTypeMismatch typeMismatch = checkValueTypeMismatch(enumAttribute);
        if (ValueTypeMismatch.STRING_TO_INTERNATIONAL_STRING.equals(typeMismatch)) {
            list.add(new Message(MSGCODE_INVALID_VALUE_TYPE,
                    MessageFormat.format(Messages.EnumAttributeValue_MultiLingual, enumAttribute.getName(),
                            defaultProjectLanguage),
                    Message.ERROR, new ObjectProperty(this, PROPERTY_VALUE)));
        } else if (ValueTypeMismatch.INTERNATIONAL_STRING_TO_STRING.equals(typeMismatch)) {
            list.add(new Message(
                    MSGCODE_INVALID_VALUE_TYPE, MessageFormat.format(Messages.EnumAttributeValue_NotMultiLingual,
                            enumAttribute.getName(), defaultProjectLanguage),
                    Message.ERROR, new ObjectProperty(this, PROPERTY_VALUE)));
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
        String stringValue = getStringValue() != null ? getStringValue() : IpsStringUtils.EMPTY;
        internationalStringValue.getContent().add(new LocalizedString(
                getIpsProject().getReadOnlyProperties().getDefaultLanguage().getLocale(), stringValue));
        setValueInternal(internationalStringValue);
    }

    private void convertInternationalStringToString() {
        String content = getValue().getDefaultLocalizedContent(getIpsProject());
        setValueInternal(new StringValue(content));
    }

    /**
     * Validations necessary if this <code>IEnumAttributeValue</code> refers to a unique identifier
     * <code>IEnumAttribute</code>.
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
            text = MessageFormat.format(Messages.EnumAttributeValue_UniqueIdentifierValueNotUnique, invalidString);
            validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_VALUE_UNIQUE_IDENTIFIER_NOT_UNIQUE, text,
                    Message.ERROR, this, PROPERTY_VALUE);
            list.add(validationMessage);
        }
    }

    /**
     * Returns whether this <code>IEnumAttributeValue</code> refers to a unique identifier
     * <code>IEnumAttribute</code>.
     */
    private boolean isUniqueIdentifierEnumAttributeValue(IEnumAttribute enumAttribute) {
        return enumAttribute.findIsUnique(getIpsProject());
    }

    @Override
    public EnumValue getEnumValue() {
        return (EnumValue)getParent();
    }

    @Override
    public boolean isNullValue() {
        boolean isNull = true;
        if (getValue() != null) {
            isNull = getValue().getContent() == null;
        }
        return isNull;
    }

    /**
     * Checks against the identifier boundary defined in the {@link IEnumType enum type}. Depending
     * on the type of value container, enum-type or enum-content, a given id must be less than or
     * greater than (or equal to) the identifier boundary.
     * <p>
     * Does nothing if no boundary is defined (empty or null).
     * <p>
     * Concept Discussion: https://wiki.faktorzehn.de/display/FaktorIPSdevelWiki/Validierung
     * 
     * @see IEnumType#getIdentifierBoundary()
     */
    public static class IdentifierBoundaryValidator {

        private final IEnumAttributeValue attributeValue;
        private final IIpsProject ipsProject;
        private final IEnumType enumType;
        private final ValueDatatype datatype;

        public IdentifierBoundaryValidator(IEnumAttributeValue attributeValue, IEnumType enumType,
                ValueDatatype datatype, IIpsProject ipsProject) {
            this.attributeValue = attributeValue;
            this.enumType = enumType;
            this.datatype = datatype;
            Assert.isNotNull(ipsProject);
            this.ipsProject = ipsProject;
        }

        /**
         * Validates if {@link #canValidate()} returns <code>true</code>. Does nothing otherwise.
         * 
         * @return the message list containing the validation messages. Contains no messages if no
         *             problems were detected.
         */
        public MessageList validateIfPossible() {
            MessageList messageList = new MessageList();
            if (canValidate()) {
                validateAndAppendMessages(messageList);
            }
            return messageList;
        }

        /**
         * @return <code>true</code> if this validator has been given sufficient information to be
         *             able to validate and if at the same time the meta-model is error free enough.
         *             Returns <code>false</code> if the meta-model has inconsistencies that prevent
         *             this validation or not all required information has been given.
         */
        public boolean canValidate() {
            return isValidateNecessary() && isIdentifierValue() && isIDValueParsable() && isBoundaryValueParsable();
        }

        private boolean isValidateNecessary() {
            if (enumType != null) {
                return ((EnumType)getEnumType())
                        .isValidateIdentifierBoundaryOnDatatypeNecessary(enumType.getIdentifierBoundary());
            }
            return false;
        }

        private boolean isIdentifierValue() {
            return getAttributeValue() != null && getEnumType() != null
                    && getAttributeValue().findEnumAttribute(getIpsProject()) == getIdentifierAttribute();
        }

        private boolean isIDValueParsable() {
            return hasDatatype() && getDatatype().isParsable(getIdAsString());
        }

        private boolean hasDatatype() {
            return getDatatype() != null;
        }

        private boolean isBoundaryValueParsable() {
            return isBoundaryValueDefined() && getDatatype().isParsable(getIdentifierBoundary());
        }

        private boolean isBoundaryValueDefined() {
            if (getEnumType() == null) {
                return false;
            } else {
                return !IpsStringUtils.isEmpty(getIdentifierBoundary());
            }
        }

        protected void validateAndAppendMessages(MessageList messageList) {
            if (!isIdentitifierValid()) {
                String message = MessageFormat.format(getRawMessageForTypeOrContent(), getIdAsString(),
                        getIdentifierBoundary());
                messageList.add(new Message(MSGCODE_ENUM_ATTRIBUTE_ID_DISALLOWED_BY_IDENTIFIER_BOUNDARY, message,
                        Message.ERROR, new ObjectProperty(getAttributeValue(), PROPERTY_VALUE)));
            }
        }

        private String getRawMessageForTypeOrContent() {
            if (isValueOfEnumType()) {
                return Messages.EnumAttributeValue_Msg_IdNotAllowedByIdentifierBoundary_valueOfEnumType;
            } else {
                return Messages.EnumAttributeValue_Msg_IdNotAllowedByIdentifierBoundary_valueOfEnumContent;
            }
        }

        private boolean isValueOfEnumType() {
            return isIdNameSpaceLessThanBoundary();
        }

        protected boolean isIdentitifierValid() {
            String candidateID = getIdAsString();
            String identifierBoundary = getIdentifierBoundary();

            int resultOfCompare = getDatatype().compare(candidateID, identifierBoundary);
            return allowsIdentifier(resultOfCompare);
        }

        private String getIdAsString() {
            return getAttributeValue().getStringValue();
        }

        private String getIdentifierBoundary() {
            return getEnumType().getIdentifierBoundary();
        }

        private boolean allowsIdentifier(int resultOfCompare) {
            return resultOfCompare < 0 == isValueOfEnumType();
        }

        private boolean isIdNameSpaceLessThanBoundary() {
            return getAttributeValue().getEnumValue().getEnumValueContainer().isIdentifierNamespaceBelowBoundary();
        }

        private IEnumAttribute getIdentifierAttribute() {
            return getEnumType().findIdentiferAttribute(getIpsProject());
        }

        private IEnumAttributeValue getAttributeValue() {
            return attributeValue;
        }

        private IIpsProject getIpsProject() {
            return ipsProject;
        }

        private IEnumType getEnumType() {
            return enumType;
        }

        private ValueDatatype getDatatype() {
            return datatype;
        }

    }
}
