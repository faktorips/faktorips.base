/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.w3c.dom.Element;

public class AttributeValueContainer {

    private List<IAttributeValue> attributeValues;

    private final IPropertyValueContainer container;

    public AttributeValueContainer(IPropertyValueContainer container) {
        this.container = container;
        this.attributeValues = new ArrayList<IAttributeValue>(0);
    }

    /**
     * 
     * @param property the product component property a {@link IPropertyValue} is requested for
     * @return the {@link IPropertyValue} for the indicated property. Returns <code>null</code> if
     *         the given property is <code>null</code> or if no property could be found.
     */
    public IPropertyValue getPropertyValue(IProductCmptProperty property) {
        if (property == null) {
            return null;
        }
        return getPropertyValue(property.getPropertyName());
    }

    /**
     * @param propertyName the name of the requested {@link IPropertyValue}
     * @return the {@link IPropertyValue} with the given name. Returns <code>null</code> if the
     *         given Property name is <code>null</code> or if no property with the indicated name
     *         could be found.
     */
    public IPropertyValue getPropertyValue(String propertyName) {
        if (propertyName == null) {
            return null;
        }
        List<IPropertyValue> allValues = getAllPropertyValues();
        for (IPropertyValue value : allValues) {
            if (propertyName.equals(value.getPropertyName())) {
                return value;
            }
        }
        return null;
    }

    /**
     * Returns all {@link IPropertyValue}s of the given type this value container contains.
     * 
     * @param type the property type of the requested property value
     * @return a list of property values of the indicated type, or an empty list if none exist.
     */
    public List<IPropertyValue> getPropertyValues(ProductCmptPropertyType type) {
        if (type == ProductCmptPropertyType.VALUE) {
            return new ArrayList<IPropertyValue>(attributeValues);
        }
        return new ArrayList<IPropertyValue>();
    }

    /**
     * @param property the {@link IProductCmptProperty} the {@link IPropertyValue} is created for
     * @return the newly created {@link IPropertyValue} or <code>null</code> if at least one of the
     *         arguments is null.
     */
    public IPropertyValue newPropertyValue(IProductCmptProperty property, String partId) {
        if (property == null || container == null) {
            return null;
        }
        IPropertyValue value = property.getProdDefPropertyType().createPropertyValue(container, partId,
                property.getPropertyName());
        registerPropertyValue(value);
        return value;
    }

    private void registerPropertyValue(IPropertyValue value) {
        attributeValues.add((IAttributeValue)value);
    }

    private List<IPropertyValue> getAllPropertyValues() {
        List<IPropertyValue> allValues = new ArrayList<IPropertyValue>();
        allValues.addAll(attributeValues);
        return allValues;
    }

    /**
     * @return a defensive copy of this container's list of attribute values
     */
    public List<IAttributeValue> getAttributeValues() {
        return new ArrayList<IAttributeValue>(attributeValues);
    }

    public IAttributeValue getAttributeValue(String attribute) {
        if (attribute == null) {
            return null;
        }
        IPropertyValue value = getPropertyValue(attribute);
        if (value != null && value.getPropertyType() == ProductCmptPropertyType.VALUE) {
            return (IAttributeValue)value;
        } else {
            return null;
        }
    }

    public IAttributeValue newAttributeValue(String partId) {
        return newAttributeValue(partId, null);
    }

    public IAttributeValue newAttributeValue(String partId, IProductCmptTypeAttribute attribute) {
        IAttributeValue newValue = newAttributeValueInternal(partId, attribute,
                attribute == null ? "" : attribute.getDefaultValue()); //$NON-NLS-1$
        objectHasChanged();
        return newValue;
    }

    public IAttributeValue newAttributeValue(String partId, IProductCmptTypeAttribute attribute, String value) {
        IAttributeValue newValue = newAttributeValueInternal(partId, attribute, value);
        objectHasChanged();
        return newValue;
    }

    /**
     * Creates a new attribute value without updating the src file.
     */
    public AttributeValue newAttributeValueInternal(String id) {
        AttributeValue av = new AttributeValue(container, id);
        attributeValues.add(av);
        return av;
    }

    /**
     * Creates a new attribute value without updating the src file.
     */
    private AttributeValue newAttributeValueInternal(String id, IProductCmptTypeAttribute attr, String value) {
        AttributeValue av = new AttributeValue(container, id, attr == null ? "" : attr.getName(), value); //$NON-NLS-1$
        attributeValues.add(av);
        return av;
    }

    public void addAttributeValue(IAttributeValue part) {
        attributeValues.add(part);
    }

    public void removeAttributeValue(IAttributeValue part) {
        attributeValues.remove(part);
    }

    public void clear() {
        attributeValues.clear();
    }

    public IIpsObjectPart newPartThis(Element xmlTag, String id) {
        String xmlTagName = xmlTag.getNodeName();
        if (xmlTagName.equals(AttributeValue.TAG_NAME)) {
            return newAttributeValueInternal(id);
        }
        return null;
    }

    public IIpsObjectPart newPartThis(Class<? extends IIpsObjectPart> partType, String partId) {
        if (partType.equals(IAttributeValue.class)) {
            return newAttributeValue(partId);
        }
        return null;
    }

    public boolean addPartThis(IIpsObjectPart part) {
        if (part instanceof IAttributeValue) {
            addAttributeValue((IAttributeValue)part);
            return true;
        }
        return false;
    }

    public boolean removePartThis(IIpsObjectPart part) {
        if (part instanceof IAttributeValue) {
            removeAttributeValue((IAttributeValue)part);
            return true;
        }
        return false;
    }

    private void objectHasChanged() {
        // container.objectHasChanged();
    }

}