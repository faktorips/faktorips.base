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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumLiteralNameAttributeValue;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.enums.IEnumValueContainer;
import org.faktorips.devtools.model.internal.ipsobject.BaseIpsObjectPart;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPart;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPartCollection;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.value.IValue;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of <code>IEnumValue</code>, see the corresponding interface for more details.
 * 
 * @see org.faktorips.devtools.model.enums.IEnumValue
 * 
 * @since 2.3
 */
public class EnumValue extends BaseIpsObjectPart implements IEnumValue {

    /**
     * Collection containing all <code>IEnumAttributeValue</code>s that belong to this
     * <code>IEnumValue</code>.
     */
    private IpsObjectPartCollection<IEnumAttributeValue> enumAttributeValues;

    /**
     * Creates a new <code>IEnumValue</code>.
     * 
     * @param parent The <code>IEnumValueContainer</code> this <code>IEnumValue</code> belongs to.
     * @param id A unique ID for this <code>IEnumValue</code>.
     */
    public EnumValue(EnumValueContainer parent, String id) {
        super(parent, id);
        enumAttributeValues = new IpsObjectPartCollection<>(this, EnumAttributeValue.class,
                IEnumAttributeValue.class, IEnumAttributeValue.XML_TAG);
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG);
    }

    @Override
    public List<IEnumAttributeValue> getEnumAttributeValues() {
        // I'm not returning the backing list due to mutability concerns.
        List<IEnumAttributeValue> attributeValuesList = new ArrayList<>();
        IIpsObjectPart[] parts = enumAttributeValues.getParts();
        for (IIpsObjectPart currentObjectPart : parts) {
            attributeValuesList.add((IEnumAttributeValue)currentObjectPart);
        }
        return attributeValuesList;
    }

    @Override
    public IEnumAttributeValue newEnumAttributeValue() {
        return createNewEnumAttributeValue(EnumAttributeValue.class);
    }

    @Override
    public IEnumLiteralNameAttributeValue newEnumLiteralNameAttributeValue() {
        return createNewEnumAttributeValue(EnumLiteralNameAttributeValue.class);
    }

    private <T extends IEnumAttributeValue> T createNewEnumAttributeValue(Class<T> attributeValueClass) {
        T attributeValue = newPart(attributeValueClass);
        fixEnumAttributeValueAfterConstructing(attributeValue);
        return attributeValue;
    }

    private void fixEnumAttributeValueAfterConstructing(IEnumAttributeValue enumAttributeValue) {
        IEnumType enumType = findEnumType();
        if (enumType != null) {
            List<IEnumAttribute> enumAttributes = enumType.getEnumAttributesIncludeSupertypeCopies(isEnumTypeValue());
            int index = getEnumAttributeValuesCount() - 1;
            if (enumAttributes.size() > index) {
                IEnumAttribute enumAttribute = enumAttributes.get(index);
                enumAttributeValue.fixValueType(enumAttribute.isMultilingual());
            }
        }
    }

    private IEnumType findEnumType() {
        return getEnumValueContainer().findEnumType(getIpsProject());
    }

    protected boolean isEnumTypeValue() {
        return getEnumValueContainer() instanceof IEnumType;
    }

    @Override
    protected IIpsObjectPart newPartThis(Element xmlTag, String id) {
        if (xmlTag.getTagName().equals(IEnumLiteralNameAttributeValue.XML_TAG)) {
            return newPart(EnumLiteralNameAttributeValue.class);
        }
        return super.newPartThis(xmlTag, id);
    }

    @Override
    public int moveEnumAttributeValue(IEnumAttributeValue enumAttributeValue, boolean up) {
        ArgumentCheck.notNull(enumAttributeValue);
        int index = getIndexOfEnumAttributeValue(enumAttributeValue);
        // Return if element is already the first / last one.
        if (up) {
            if (index == 0) {
                return index;
            }
        } else {
            if (index == getEnumAttributeValuesCount() - 1) {
                return index;
            }
        }

        // Perform the moving.
        int[] newIndex = enumAttributeValues.moveParts(new int[] { index }, up);

        return newIndex[0];
    }

    @Override
    public void swapEnumAttributeValue(int firstIndex, int secondIndex) {
        if (firstIndex == secondIndex || firstIndex < 0 || secondIndex < 0) {
            throw new IllegalArgumentException();
        }
        int positionOfFirstAttribute = firstIndex;
        int positionOfSecondAttribute = secondIndex;
        // firstIndex is always the higher index. If not swap both indexes
        if (firstIndex < secondIndex) {
            positionOfFirstAttribute = secondIndex;
            positionOfSecondAttribute = firstIndex;
        }
        IEnumAttributeValue firstValue = getEnumAttributeValues().get(positionOfFirstAttribute);
        IEnumAttributeValue secondValue = getEnumAttributeValues().get(positionOfSecondAttribute);
        int currentIndex = positionOfFirstAttribute;
        // move first EnumAttributValue to the desired position
        while (currentIndex != positionOfSecondAttribute) {
            currentIndex = moveEnumAttributeValue(firstValue, true);
        }
        // move second EnumAttributValue to the desired position
        currentIndex = positionOfSecondAttribute + 1;
        while (currentIndex != positionOfFirstAttribute) {
            currentIndex = moveEnumAttributeValue(secondValue, false);
        }
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) {
        super.validateThis(list, ipsProject);

        IEnumValueContainer enumValueContainer = getEnumValueContainer();
        IEnumType enumType = enumValueContainer.findEnumType(ipsProject);
        if (enumType == null) {
            return;
        }

        /*
         * Number of EnumAttributeValues must match number of EnumAttributes of the
         * EnumValueContainer.
         */
        int numberNeeded = 0;
        if (isEnumTypeValue()) {
            IEnumType containerType = (IEnumType)enumValueContainer;
            numberNeeded = containerType.getEnumAttributesCountIncludeSupertypeCopies(true);
        } else {
            IEnumContent contentType = (IEnumContent)enumValueContainer;
            numberNeeded = contentType.getEnumAttributeReferencesCount();
        }
        if (numberNeeded != getEnumAttributeValuesCount()) {
            String text = Messages.EnumValue_NumberAttributeValuesDoesNotCorrespondToNumberAttributes;
            Message validationMessage = new Message(
                    MSGCODE_ENUM_VALUE_NUMBER_ATTRIBUTE_VALUES_DOES_NOT_CORRESPOND_TO_NUMBER_ATTRIBUTES, text,
                    Message.ERROR, this);
            list.add(validationMessage);
        }
    }

    @Override
    public int getEnumAttributeValuesCount() {
        return enumAttributeValues.size();
    }

    @Override
    public IEnumValueContainer getEnumValueContainer() {
        return (IEnumValueContainer)getParent();
    }

    @Override
    public IEnumAttributeValue getEnumAttributeValue(IEnumAttribute enumAttribute) {
        if (enumAttribute == null) {
            return null;
        }
        int attributeIndex = enumAttribute.getEnumType().getIndexOfEnumAttribute(enumAttribute, isEnumTypeValue());
        if (attributeIndex < 0 || enumAttributeValues.size() - 1 < attributeIndex) {
            return null;
        }
        return enumAttributeValues.getPart(attributeIndex);
    }

    @Override
    public void setEnumAttributeValue(IEnumAttribute enumAttribute, IValue<?> value) {
        ArgumentCheck.notNull(enumAttribute);
        getEnumAttributeValue(enumAttribute).setValue(value);
    }

    @Override
    public void setEnumAttributeValue(String enumAttributeName, IValue<?> value) {
        ArgumentCheck.notNull(enumAttributeName);
        IEnumType enumType = findEnumType();
        IEnumAttribute enumAttribute = enumType.getEnumAttributeIncludeSupertypeCopies(enumAttributeName);
        if (enumAttribute == null) {
            throw new NoSuchElementException();
        }
        setEnumAttributeValue(enumAttribute, value);
    }

    @Override
    public void setEnumAttributeValue(int enumAttributeIndex, IValue<?> value) {
        if (!(enumAttributeIndex > -1 && enumAttributeIndex < enumAttributeValues.size())) {
            throw new IndexOutOfBoundsException();
        }
        enumAttributeValues.getBackingList().get(enumAttributeIndex).setValue(value);
    }

    @Override
    public List<IEnumAttributeValue> findUniqueEnumAttributeValues(List<IEnumAttribute> uniqueEnumAttributes,
            IIpsProject ipsProject) {

        ArgumentCheck.notNull(new Object[] { uniqueEnumAttributes, ipsProject });
        List<IEnumAttributeValue> uniqueAttributeValues = new ArrayList<>(
                uniqueEnumAttributes.size());
        for (IEnumAttribute currentUniqueAttribute : uniqueEnumAttributes) {
            uniqueAttributeValues.add(getEnumAttributeValue(currentUniqueAttribute));
        }
        return uniqueAttributeValues;
    }

    @Override
    public int getIndexOfEnumAttributeValue(IEnumAttributeValue enumAttributeValue) {
        ArgumentCheck.notNull(enumAttributeValue);
        return enumAttributeValues.indexOf(enumAttributeValue);
    }

    @Override
    public IEnumLiteralNameAttributeValue getEnumLiteralNameAttributeValue() {
        if (!isEnumTypeValue()) {
            return null;
        }
        for (IEnumAttributeValue enumAttributeValue : enumAttributeValues) {
            if (enumAttributeValue.isEnumLiteralNameAttributeValue()) {
                return (IEnumLiteralNameAttributeValue)enumAttributeValue;
            }
        }
        return null;
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.removeAttribute(IpsObjectPart.PROPERTY_ID);
    }

}
