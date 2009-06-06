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

        // Initialize the value with the proper default value
        IIpsProject ipsProject = getIpsProject();
        IEnumAttribute referencedEnumAttribute = findEnumAttribute(ipsProject);
        if (referencedEnumAttribute != null) {
            ValueDatatype datatype = referencedEnumAttribute.findDatatype(ipsProject);
            if (datatype != null) {
                value = datatype.getDefaultValue();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG);
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    public Image getImage() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public IEnumAttribute findEnumAttribute(IIpsProject ipsProject) throws CoreException {
        ArgumentCheck.notNull(ipsProject);

        IEnumValueContainer valueContainer = (IEnumValueContainer)parent.getParent();
        IEnumValue enumValue = (IEnumValue)parent;

        // Calculate index
        int index;
        List<IEnumAttributeValue> enumAttributeValuesList = enumValue.getEnumAttributeValues();
        for (index = 0; index < enumAttributeValuesList.size(); index++) {
            IEnumAttributeValue currentEnumAttributeValue = enumAttributeValuesList.get(index);
            if (currentEnumAttributeValue == this) {
                break;
            }
        }

        IEnumType enumType = valueContainer.findEnumType(ipsProject);
        if (enumType == null) {
            return null;
        }

        if (!(enumType.getEnumAttributesCount(true) == enumValue.getEnumAttributeValuesCount())) {
            return null;
        }

        return enumType.getEnumAttributesIncludeSupertypeCopies().get(index);
    }

    /**
     * {@inheritDoc}
     */
    public String getValue() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    public void setValue(String value) {
        String oldValue = this.value;
        this.value = value;
        valueChanged(oldValue, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);

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

        // Unique identifier and literal name validations
        if (isUniqueIdentifierEnumAttributeValue()) {
            validateUniqueIdentifierEnumAttributeValue(list, ipsProject);
            if (list.getNoOfMessages() == 0) {
                if (isLiteralNameEnumAttributeValue()) {
                    validateLiteralNameEnumAttributeValue(list, ipsProject);
                }
            }
        }

    }

    /**
     * Validations neccessary if this enum attribute value refers to an enum attribute that is used
     * as literal name.
     */
    private void validateLiteralNameEnumAttributeValue(MessageList list, IIpsProject ipsProject) throws CoreException {
        IEnumAttribute enumAttribute = findEnumAttribute(ipsProject);
        String text;
        Message validationMessage;

        // The identifier enum attribute value must be java conform
        if (!(JavaConventions.validateIdentifier(value, "1.5", "1.5").isOK())) {
            text = NLS.bind(Messages.EnumAttributeValue_LiteralNameValueNotJavaConform, enumAttribute.getName());
            validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_VALUE_LITERAL_NAME_NOT_JAVA_CONFORM, text,
                    Message.ERROR, this);
            list.add(validationMessage);
        }
    }

    /**
     * Validations neccessary if this enum attribute value refers to a unique identifier enum
     * attribute.
     */
    private void validateUniqueIdentifierEnumAttributeValue(MessageList list, IIpsProject ipsProject)
            throws CoreException {

        IEnumAttribute enumAttribute = findEnumAttribute(ipsProject);
        String text;
        Message validationMessage;

        // The unique identifier enum attribute value must not be empty
        String uniqueIdentifierValue = getValue();
        boolean uniqueIdentifierValueMissing = (uniqueIdentifierValue == null) ? true : uniqueIdentifierValue
                .equals("")
                || uniqueIdentifierValue.equals("<null>");
        if (uniqueIdentifierValueMissing) {
            text = NLS.bind(Messages.EnumAttributeValue_UniqueIdentifierValueEmpty, enumAttribute.getName());
            validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_VALUE_UNIQUE_IDENTIFIER_VALUE_EMPTY, text,
                    Message.ERROR, this);
            list.add(validationMessage);
        }

        if (!uniqueIdentifierValueMissing) {
            // The unique identifier enum attribute value must be unique
            String value = getValue();
            IEnumValueContainer enumValueContainer = getEnumValue().getEnumValueContainer();
            for (IEnumValue currentEnumValue : enumValueContainer.getEnumValues()) {
                if (currentEnumValue == getEnumValue()) {
                    continue;
                }

                IEnumAttributeValue otherEnumAttributeValue = currentEnumValue.findEnumAttributeValue(ipsProject,
                        enumAttribute);
                if (otherEnumAttributeValue != null) {
                    String otherValue = otherEnumAttributeValue.getValue();
                    boolean otherValueMissing = (otherValue == null) ? true : uniqueIdentifierValue.equals("")
                            || uniqueIdentifierValue.equals("<null>");
                    if (!otherValueMissing) {
                        if (otherValue.equals(value)) {
                            text = NLS.bind(Messages.EnumAttributeValue_UniqueIdentifierValueNotUnique, enumAttribute
                                    .getName());
                            validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_VALUE_UNIQUE_IDENTIFIER_NOT_UNIQUE,
                                    text, Message.ERROR, this);
                            list.add(validationMessage);
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns whether this enum attribute value refers to an enum attribute that is used as literal
     * name.
     */
    private boolean isLiteralNameEnumAttributeValue() throws CoreException {
        IIpsProject ipsProject = getIpsProject();
        IEnumAttribute referencedEnumAttribute = findEnumAttribute(ipsProject);
        if (referencedEnumAttribute == null) {
            throw new NullPointerException();
        }

        Boolean literalName = referencedEnumAttribute.findIsLiteralName(ipsProject);
        if (literalName == null) {
            return false;
        }

        return literalName;
    }

    /** Returns whether this enum attribute value refers to a unique identifier enum attribute. */
    private boolean isUniqueIdentifierEnumAttributeValue() throws CoreException {
        IIpsProject ipsProject = getIpsProject();
        IEnumAttribute referencedEnumAttribute = findEnumAttribute(ipsProject);
        if (referencedEnumAttribute == null) {
            throw new NullPointerException();
        }

        Boolean uniqueIdentifier = referencedEnumAttribute.findIsUniqueIdentifier(ipsProject);
        if (uniqueIdentifier == null) {
            return false;
        }

        return uniqueIdentifier;
    }

    /**
     * {@inheritDoc}
     */
    public IEnumValue getEnumValue() {
        return (IEnumValue)getParent();
    }

}
