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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.internal.model.ipsobject.BaseIpsObjectPart;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartCollection;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of <tt>IEnumValue</tt>, see the corresponding interface for more details.
 * 
 * @see org.faktorips.devtools.core.model.enums.IEnumValue
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumValue extends BaseIpsObjectPart implements IEnumValue {

    /** Collection containing all enum attribute values that belong to this enum value object. */
    private IpsObjectPartCollection<IEnumAttributeValue> enumAttributeValues;

    /**
     * Creates a new <tt>EnumValue</tt>.
     * 
     * @param parent The enum value container this enum value belongs to.
     * @param id A unique id for this enum value.
     */
    public EnumValue(IEnumValueContainer parent, int id) {
        super(parent, id);
        descriptionChangable = false;
        enumAttributeValues = new IpsObjectPartCollection<IEnumAttributeValue>(this, EnumAttributeValue.class,
                IEnumAttributeValue.class, IEnumAttributeValue.XML_TAG);
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG);
    }

    public Image getImage() {
        return null;
    }

    public List<IEnumAttributeValue> getEnumAttributeValues() {
        // I'm not returning the backing list due to mutability concerns.
        List<IEnumAttributeValue> attributeValuesList = new ArrayList<IEnumAttributeValue>();
        IIpsObjectPart[] parts = enumAttributeValues.getParts();
        for (IIpsObjectPart currentObjectPart : parts) {
            attributeValuesList.add((IEnumAttributeValue)currentObjectPart);
        }
        return attributeValuesList;
    }

    public IEnumAttributeValue newEnumAttributeValue() throws CoreException {
        return (IEnumAttributeValue)newPart(EnumAttributeValue.class);
    }

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
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
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
        if (enumValueContainer instanceof IEnumType) {
            IEnumType containerType = (IEnumType)enumValueContainer;
            numberNeeded = containerType.getEnumAttributesCountIncludeSupertypeCopies(true);
        } else {
            IEnumContent contentType = (IEnumContent)enumValueContainer;
            numberNeeded = contentType.getReferencedEnumAttributesCount();
        }
        if (numberNeeded != getEnumAttributeValuesCount()) {
            String text = Messages.EnumValue_NumberAttributeValuesDoesNotCorrespondToNumberAttributes;
            Message validationMessage = new Message(
                    MSGCODE_ENUM_VALUE_NUMBER_ATTRIBUTE_VALUES_DOES_NOT_CORRESPOND_TO_NUMBER_ATTRIBUTES, text,
                    Message.ERROR, this);
            list.add(validationMessage);
        }
    }

    public int getEnumAttributeValuesCount() {
        return enumAttributeValues.size();
    }

    public IEnumValueContainer getEnumValueContainer() {
        return (IEnumValueContainer)getParent();
    }

    public IEnumAttributeValue findEnumAttributeValue(IIpsProject ipsProject, IEnumAttribute enumAttribute)
            throws CoreException {

        ArgumentCheck.notNull(ipsProject);
        if (enumAttribute == null) {
            return null;
        }

        for (IEnumAttributeValue currentEnumAttributeValue : enumAttributeValues.getBackingList()) {
            IEnumAttribute currentReferencedEnumAttribute = currentEnumAttributeValue.findEnumAttribute(ipsProject);
            if (currentReferencedEnumAttribute != null) {
                if (currentReferencedEnumAttribute.equals(enumAttribute)) {
                    return currentEnumAttributeValue;
                }
            }
        }

        return null;
    }

    public void setEnumAttributeValue(IEnumAttribute enumAttribute, String value) throws CoreException {
        ArgumentCheck.notNull(enumAttribute);
        findEnumAttributeValue(getIpsProject(), enumAttribute).setValue(value);
    }

    public void setEnumAttributeValue(String enumAttributeName, String value) throws CoreException {
        ArgumentCheck.notNull(enumAttributeName);
        IEnumType enumType = getEnumValueContainer().findEnumType(getIpsProject());
        IEnumAttribute enumAttribute = enumType.getEnumAttributeIncludeSupertypeCopies(enumAttributeName);
        if (enumAttribute == null) {
            throw new NoSuchElementException();
        }
        setEnumAttributeValue(enumAttribute, value);
    }

    public void setEnumAttributeValue(int enumAttributeIndex, String value) {
        if (!(enumAttributeIndex > -1 && enumAttributeIndex < enumAttributeValues.size())) {
            throw new IndexOutOfBoundsException();
        }
        enumAttributeValues.getBackingList().get(enumAttributeIndex).setValue(value);
    }

    public List<IEnumAttributeValue> findUniqueEnumAttributeValues(List<IEnumAttribute> uniqueEnumAttributes,
            IIpsProject ipsProject) throws CoreException {

        ArgumentCheck.notNull(new Object[] { uniqueEnumAttributes, ipsProject });
        List<IEnumAttributeValue> uniqueAttributeValues = new ArrayList<IEnumAttributeValue>(uniqueEnumAttributes
                .size());
        IEnumType enumType = getEnumValueContainer().findEnumType(ipsProject);
        for (IEnumAttribute currentUniqueAttribute : uniqueEnumAttributes) {
            int attributeIndex = enumType.getIndexOfEnumAttribute(currentUniqueAttribute);
            uniqueAttributeValues.add(enumAttributeValues.getPart(attributeIndex));
        }
        return uniqueAttributeValues;
    }

    public int getIndexOfEnumAttributeValue(IEnumAttributeValue enumAttributeValue) {
        ArgumentCheck.notNull(enumAttributeValue);
        int index = enumAttributeValues.indexOf(enumAttributeValue);
        if (index >= 0) {
            return index;
        }
        throw new NoSuchElementException();
    }

    @Override
    public void delete() {
        // Remove unique identifier entries of this EnumValue from the validation cache.
        IIpsProject ipsProject = getIpsProject();
        EnumValueContainer enumValueContainerImpl = (EnumValueContainer)getEnumValueContainer();
        if (enumValueContainerImpl.isUniqueIdentifierValidationCacheInitialized()) {
            try {
                IEnumType referencedEnumType = getEnumValueContainer().findEnumType(ipsProject);
                if (referencedEnumType != null) {
                    List<IEnumAttribute> uniqueEnumAttributes = referencedEnumType.findUniqueEnumAttributes(
                            getEnumValueContainer() instanceof IEnumType, ipsProject);
                    for (IEnumAttributeValue currentEnumAttributeValue : findUniqueEnumAttributeValues(
                            uniqueEnumAttributes, ipsProject)) {
                        enumValueContainerImpl.removeValidationCacheUniqueIdentifierEntry(
                                getIndexOfEnumAttributeValue(currentEnumAttributeValue), currentEnumAttributeValue
                                        .getValue(), currentEnumAttributeValue);
                    }
                }
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }

        super.delete();
    }

}
