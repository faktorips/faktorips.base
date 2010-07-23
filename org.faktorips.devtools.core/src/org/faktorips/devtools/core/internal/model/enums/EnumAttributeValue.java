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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.builder.JavaNamingConvention;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttributeValue;
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
            element.setAttribute("isNull", "true"); //$NON-NLS-1$ //$NON-NLS-2$
            Text textNode = XmlUtil.getTextNode(element);
            if (textNode != null) {
                textNode.setTextContent(""); //$NON-NLS-1$
            }
            return;
        } else {
            element.setAttribute("isNull", "false"); //$NON-NLS-1$ //$NON-NLS-2$
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
        Boolean isNull = Boolean.valueOf(element.getAttribute("isNull")); //$NON-NLS-1$
        if (isNull.booleanValue()) {
            value = null;
            return;
        }
        Text textNode = XmlUtil.getTextNode(element);
        if (textNode != null) {
            value = textNode.getTextContent();
        } else {
            value = ""; //$NON-NLS-1$
        }
    }

    @Override
    public IEnumAttribute findEnumAttribute(IIpsProject ipsProject) throws CoreException {
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
        if (!(enumAttributesCount == enumValue.getEnumAttributeValuesCount())
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

    @Override
    public String getValue() {
        return value;
    }

    @Override
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
            }
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

    @Override
    public IEnumValue getEnumValue() {
        return (IEnumValue)getParent();
    }

    @Override
    public void setValueAsLiteralName(String value) {
        if (value == null) {
            setValue(null);
            return;
        }

        String val = JavaNamingConvention.ECLIPSE_STANDARD.getConstantClassVarName(value);
        val = val.replaceAll("[Ää]", "AE"); //$NON-NLS-1$ //$NON-NLS-2$
        val = val.replaceAll("[Öö]", "OE"); //$NON-NLS-1$ //$NON-NLS-2$
        val = val.replaceAll("[Üü]", "UE"); //$NON-NLS-1$ //$NON-NLS-2$
        val = val.replaceAll("[ß]", "SS"); //$NON-NLS-1$ //$NON-NLS-2$
        val = val.replaceAll("[^A-Za-z0-9]", "_"); //$NON-NLS-1$ //$NON-NLS-2$
        setValue(val);
    }

}
