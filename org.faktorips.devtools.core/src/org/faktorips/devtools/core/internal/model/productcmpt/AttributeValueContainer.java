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
import org.faktorips.util.ClassToInstancesMap;
import org.w3c.dom.Element;

public class AttributeValueContainer {
    private ClassToInstancesMap<IPropertyValue> classToInstancesMap;

    private final IPropertyValueContainer container;

    public AttributeValueContainer(IPropertyValueContainer container) {
        this.container = container;
        classToInstancesMap = new ClassToInstancesMap<IPropertyValue>();
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
        return new ArrayList<IPropertyValue>(classToInstancesMap.get(type.getValueClass()));
    }

    /**
     * @param property the {@link IProductCmptProperty} the {@link IPropertyValue} is created for
     * @return the newly created {@link IPropertyValue} or <code>null</code> if at least one of the
     *         arguments is <code>null</code>.
     */
    public IPropertyValue newPropertyValue(IProductCmptProperty property, String partId) {
        if (property == null || container == null) {
            return null;
        }
        if (property.getProductCmptPropertyType() == ProductCmptPropertyType.VALUE) {
            IPropertyValue value = property.getProductCmptPropertyType().createPropertyValue(container, partId,
                    property.getPropertyName());
            registerPropertyValue(value);
            return value;
        } else {
            return null;
        }
    }

    private void registerPropertyValue(IPropertyValue value) {
        classToInstancesMap.putWithRuntimeCheck(value.getPropertyType().getValueClass(), value);
    }

    private void removePropertyValue(IPropertyValue value) {
        classToInstancesMap.remove(value.getPropertyType().getValueClass(), value);
    }

    private List<IPropertyValue> getAllPropertyValues() {
        List<IPropertyValue> allValues = new ArrayList<IPropertyValue>();
        allValues.addAll(classToInstancesMap.values());
        return allValues;
    }

    /**
     * @return this container's list of attribute values (not a a defensive copy)
     */
    public List<IAttributeValue> getAttributeValues() {
        return classToInstancesMap.get(IAttributeValue.class);
    }

    public IAttributeValue getAttributeValue(String attribute) {
        return (IAttributeValue)getPropertyValue(ProductCmptPropertyType.VALUE, attribute);
    }

    /**
     * 
     * @param type the type of property
     * @param attributeName the name of the attribute
     * @return the corresponding property value or <code>null</code> if it is either non existent or
     *         if it is not of the indicated type.
     */
    private IPropertyValue getPropertyValue(ProductCmptPropertyType type, String attributeName) {
        if (attributeName == null) {
            return null;
        }
        for (IPropertyValue iPropertyValue : classToInstancesMap.get(type
                .getValueClass())) {
            if (attributeName.equals(iPropertyValue.getName())) {
                return iPropertyValue;
            }
        }
        return null;
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
     * Creates a new attribute value without updating the source file.
     */
    public AttributeValue newAttributeValueInternal(String id) {
        AttributeValue av = new AttributeValue(container, id);
        registerPropertyValue(av);
        return av;
    }

    /**
     * Creates a new attribute value without updating the source file.
     */
    private AttributeValue newAttributeValueInternal(String id, IProductCmptTypeAttribute attr, String value) {
        AttributeValue av = new AttributeValue(container, id, attr == null ? "" : attr.getName(), value); //$NON-NLS-1$
        registerPropertyValue(av);
        return av;
    }

    public void addAttributeValue(IAttributeValue part) {
        registerPropertyValue(part);
    }

    public void removeAttributeValue(IAttributeValue part) {
        removePropertyValue(part);
    }

    public void clear() {
        classToInstancesMap.clear();
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