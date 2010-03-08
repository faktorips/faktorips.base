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
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.builder.JavaNamingConvention;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumContent;
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
 * Implementation of <tt>IEnumAttributeValue</tt>, see the corresponding interface for more details.
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
     * Creates a new <tt>IEnumAttributeValue</tt>.
     * 
     * @param parent The <tt>IEnumValue</tt> this <tt>IEnumAttributeValue</tt> belongs to.
     * @param id A unique ID for this <tt>IEnumAttributeValue</tt>.
     * 
     * @throws CoreException If an error occurs while initializing the object.
     */
    public EnumAttributeValue(IEnumValue parent, String id) throws CoreException {
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
    protected void initPropertiesFromXml(Element element, String id) {
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

    public IEnumAttribute findEnumAttribute(IIpsProject ipsProject) throws CoreException {
        ArgumentCheck.notNull(ipsProject);

        IEnumValue enumValue = getEnumValue();
        IEnumValueContainer valueContainer = enumValue.getEnumValueContainer();
        IEnumType enumType = valueContainer.findEnumType(ipsProject);
        if (enumType == null) {
            return null;
        }

        // Check number of EnumAttributeValues matching number of EnumAttributes.
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
    private boolean isLiteralNameEnumAttributeValue(IEnumAttribute enumAttribute) {
        return enumAttribute instanceof IEnumLiteralNameAttribute;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        String oldValue = this.value;
        this.value = value;

        /*
         * Update unique identifier validation cache if this EnumAttributeValue refers to a unique
         * EnumAttribute.
         */
        IEnumValue enumValue = getEnumValue();
        EnumValueContainer enumValueContainerImpl = (EnumValueContainer)enumValue.getEnumValueContainer();
        if (enumValueContainerImpl.isUniqueIdentifierValidationCacheInitialized()) {
            try {
                IEnumAttribute referencedEnumAttribute = findEnumAttribute(getIpsProject());
                IEnumType enumType = referencedEnumAttribute.getEnumType();
                int index = enumType.getIndexOfEnumAttribute(referencedEnumAttribute);
                if (enumValueContainerImpl.containsValidationCacheUniqueIdentifier(index)) {
                    enumValueContainerImpl.removeValidationCacheUniqueIdentifierEntry(index, oldValue, this);
                    enumValueContainerImpl.addValidationCacheUniqueIdentifierEntry(index, value, this);
                }
            } catch (CoreException e) {
                throw new RuntimeException(e);
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
        if (enumValueContainer instanceof IEnumType) {
            if (enumType.isAbstract() || !(enumType.isContainingValues())) {
                return;
            }
        }
        if (enumValueContainer instanceof IEnumContent) {
            IEnumContent enumContent = (IEnumContent)enumValueContainer;
            if (enumContent.isFixToModelRequired()) {
                return;
            }
        }

        // Value parsable?
        ValueDatatype valueDatatype = enumAttribute.findDatatype(ipsProject);
        if (valueDatatype != null) {
            if (!(valueDatatype.isParsable(value))) {
                String text = NLS.bind(Messages.EnumAttributeValue_ValueNotParsable, value, valueDatatype.getName());
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
            if (isUniqueIdentifierEnumAttributeValue(enumAttribute, enumType)) {
                validateUniqueIdentifierEnumAttributeValue(list, enumAttribute);
                if (list.getNoOfMessages() == 0) {
                    if (isLiteralNameEnumAttributeValue(enumAttribute)) {
                        validateLiteralNameEnumAttributeValue(list);
                    }
                }
            }
        }
    }

    /**
     * Validations necessary if this <tt>IEnumAttributeValue</tt> refers to an
     * <tt>IEnumAttribute</tt> that is used as literal name.
     */
    private void validateLiteralNameEnumAttributeValue(MessageList list) {
        // A literal name EnumAttributeValue must be java conform.
        String complianceLevel = getIpsProject().getJavaProject().getOption(JavaCore.COMPILER_COMPLIANCE, true);
        String sourceLevel = getIpsProject().getJavaProject().getOption(JavaCore.COMPILER_SOURCE, true);
        if (!(JavaConventions.validateIdentifier(value, sourceLevel, complianceLevel).isOK())) {
            String text = NLS.bind(Messages.EnumAttributeValue_LiteralNameValueNotJavaConform, value);
            Message validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_VALUE_LITERAL_NAME_NOT_JAVA_CONFORM, text,
                    Message.ERROR, this, PROPERTY_VALUE);
            list.add(validationMessage);
        }
    }

    /**
     * Validations necessary if this <tt>IEnumAttributeValue</tt> refers to a unique identifier
     * <tt>IEnumAttribute</tt>.
     */
    private void validateUniqueIdentifierEnumAttributeValue(MessageList list, IEnumAttribute enumAttribute) {
        String text;
        Message validationMessage;

        // A unique identifier EnumAttributeValue must not be empty.
        String uniqueIdentifierValue = getValue();
        boolean uniqueIdentifierValueMissing = (uniqueIdentifierValue == null) ? true
                : uniqueIdentifierValue.length() == 0
                        || uniqueIdentifierValue.equals(IpsPlugin.getDefault().getIpsPreferences()
                                .getNullPresentation());
        if (uniqueIdentifierValueMissing) {
            text = Messages.EnumAttributeValue_UniqueIdentifierValueEmpty;
            validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_VALUE_UNIQUE_IDENTIFIER_VALUE_EMPTY, text,
                    Message.ERROR, this, PROPERTY_VALUE);
            list.add(validationMessage);
            return;
        }

        // A unique identifier EnumAttributeValue must be truly unique.
        EnumValueContainer enumValueContainerImpl = (EnumValueContainer)getEnumValue().getEnumValueContainer();
        IEnumType enumType = enumAttribute.getEnumType();
        List<IEnumAttributeValue> cachedAttributeValues = enumValueContainerImpl
                .getValidationCacheListForUniqueIdentifier(enumType.getIndexOfEnumAttribute(enumAttribute), getValue());
        if (cachedAttributeValues != null) {
            if (cachedAttributeValues.size() > 1) {
                text = NLS.bind(Messages.EnumAttributeValue_UniqueIdentifierValueNotUnique, value);
                validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_VALUE_UNIQUE_IDENTIFIER_NOT_UNIQUE, text,
                        Message.ERROR, this, PROPERTY_VALUE);
                list.add(validationMessage);
            }
        }
    }

    /**
     * Returns whether this <tt>IEnumAttributeValue</tt> refers to a unique identifier
     * <tt>IEnumAttribute</tt>.
     */
    private boolean isUniqueIdentifierEnumAttributeValue(IEnumAttribute enumAttribute, IEnumType enumType) {
        EnumValueContainer enumValueContainerImpl = (EnumValueContainer)getEnumValue().getEnumValueContainer();
        return enumValueContainerImpl.containsValidationCacheUniqueIdentifier(enumType
                .getIndexOfEnumAttribute(enumAttribute));
    }

    public IEnumValue getEnumValue() {
        return (IEnumValue)getParent();
    }

    public void setValueAsLiteralName(String value) {
        if (value == null) {
            setValue(null);
            return;
        }

        String val = JavaNamingConvention.ECLIPSE_STANDARD.getConstantClassVarName(value);
        val = val.replaceAll("[Ää]", "AE");
        val = val.replaceAll("[Öö]", "OE");
        val = val.replaceAll("[Üü]", "UE");
        val = val.replaceAll("[ß]", "SS");
        val = val.replaceAll("[^A-Za-z0-9]", "_");
        setValue(val);
    }

}
