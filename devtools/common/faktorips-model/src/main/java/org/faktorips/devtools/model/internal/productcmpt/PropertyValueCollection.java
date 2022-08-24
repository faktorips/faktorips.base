/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.model.type.IProductCmptProperty;
import org.faktorips.devtools.model.type.ProductCmptPropertyType;
import org.faktorips.util.ClassToInstancesMap;

/**
 * Class for holding {@link IPropertyValue}s for a {@link IPropertyValueContainer}. An instance of
 * this class holds property values of different {@link ProductCmptPropertyType}'s at the same time.
 * 
 * @author Stefan Widmaier
 * @since 3.4
 */
public class PropertyValueCollection {

    private static final PropertyValueComparator COMPARATOR = new PropertyValueComparator();

    private final ClassToInstancesMap<IPropertyValue> classToInstancesMap;
    private final IPropertyValueContainer propertyValueContainer;

    /**
     * Creates a new {@link PropertyValueCollection}.
     * 
     * @param propertyValueContainer the container using this collection
     * 
     */
    public PropertyValueCollection(IPropertyValueContainer propertyValueContainer) {
        this.propertyValueContainer = propertyValueContainer;
        classToInstancesMap = new ClassToInstancesMap<>();
    }

    /**
     * Returns the {@link IPropertyValue} corresponding to the given {@link IProductCmptProperty}.
     * <p>
     * The property value is searched by the property name. If there are multiple property values
     * with the same name, the first one is returned. Property values returned by this method may be
     * safely casted to the {@link IProductCmptProperty}'s value class.
     * 
     * @param property the product component property a {@link IPropertyValue} is requested for
     * @param type the expected type of the property value, needs to be the interface type!
     * @return the {@link IPropertyValue} for the indicated property. Returns <code>null</code> if
     *             the given property is <code>null</code> or if no property could be found.
     */
    public <T extends IPropertyValue> T getPropertyValue(IProductCmptProperty property, Class<T> type) {
        if (property == null) {
            return null;
        }
        return getPropertyValue(property.getPropertyName(), type);
    }

    public List<IPropertyValue> getPropertyValues(final IProductCmptProperty property) {
        if (property == null) {
            return Collections.emptyList();
        }
        return property.getPropertyValueTypes().stream().filter(t -> t != null)
                .map(type -> getPropertyValue(property, type.getInterfaceClass())).filter(value -> value != null)
                .collect(Collectors.toList());
    }

    /**
     * Returns the property value corresponding to the given property name and of the given type.
     * <p>
     * The property value is searched by the property name. If there are multiple property values
     * with the same name, the first one is returned.
     * 
     * @param propertyName the name of the requested property value
     * @param type the type of {@link IPropertyValue} that is requested
     * @return the {@link IPropertyValue} for the indicated type and name. Returns <code>null</code>
     *             if no property could be found.
     */
    public <T extends IPropertyValue> T getPropertyValue(String propertyName, Class<T> type) {
        List<T> list = classToInstancesMap.get(type);
        for (T propertyValue : list) {
            if (propertyValue.getPropertyName().equals(propertyName)) {
                return propertyValue;
            }
        }
        return null;
    }

    /**
     * Searches all {@link IPropertyValue}s registered with this {@link PropertyValueCollection} for
     * any with the indicated name.
     * <p>
     * Note that a safe cast can not be guaranteed as {@link IPropertyValue}s of a different
     * class/type may have the same property name.
     * 
     * @param propertyName the name of the requested {@link IPropertyValue}
     * @return a list of {@link IPropertyValue} with the given name. Returns an empty list if the
     *             given Property name is <code>null</code> or if no property with the indicated
     *             name could be found.
     */
    public <T extends IPropertyValue> List<T> getPropertyValues(String propertyName) {
        List<IPropertyValue> allValues = getAllPropertyValues();
        return getPropertyValuesFromList(allValues, propertyName);
    }

    /**
     * Searches the given list for properties with the given name. Returns an empty list if no
     * element can be found, or if one of the arguments is <code>null</code>.
     * 
     * @param valueList the list to search
     * @param propertyName the name of the property
     * @return the first element with the given name in the list.
     */
    private static <T extends IPropertyValue> List<T> getPropertyValuesFromList(List<IPropertyValue> valueList,
            String propertyName) {
        List<T> result = new ArrayList<>();
        if (propertyName == null || valueList == null) {
            return result;
        }
        for (IPropertyValue value : valueList) {
            if (propertyName.equals(value.getPropertyName())) {
                @SuppressWarnings("unchecked")
                T castedValue = (T)value;
                result.add(castedValue);
            }
        }
        return result;
    }

    /**
     * Returns all {@link IPropertyValue}s of the given type this value container contains.
     * 
     * @param clazz the class type of the properties you want to get
     * @return a list of property values of the indicated type, or an empty list if none exist.
     */
    public <T extends IPropertyValue> List<T> getPropertyValues(Class<T> clazz) {
        return new ArrayList<>(classToInstancesMap.get(clazz));
    }

    /**
     * Creates and returns all necessary property values for a given {@link IProductCmptProperty}
     * and add the new values to the {@link IPropertyValueContainer}.
     * 
     * @param container the container that should be the parent of the new {@link IPropertyValue}
     * @param property the {@link IProductCmptProperty} the values are created for
     * @param partId the new part's id
     * @return the newly created property values or an empty list if the given property is
     *             <code>null</code>.
     */
    public List<IPropertyValue> newPropertyValues(IPropertyValueContainer container,
            IProductCmptProperty property,
            String partId) {
        ArrayList<IPropertyValue> result = new ArrayList<>();
        for (PropertyValueType valueType : property.getPropertyValueTypes()) {
            IPropertyValue propertyValue = newPropertyValue(property, partId, valueType.getInterfaceClass());
            result.add(propertyValue);
        }
        return result;
    }

    /**
     * Creates a new part for the given XML tag adds it to this holder and returns it.
     * 
     * @param xmlTagName the XML tag a {@link IPropertyValue} should be created for
     * @param partId the new part's id
     * 
     * @return the newly created part or <code>null</code> if the given XML tag corresponds to no
     *             {@link IPropertyValue} or {@link ProductCmptPropertyType} respectively.
     */
    public IIpsObjectPart newPropertyValue(String xmlTagName, String partId) {
        PropertyValueType propertyType = PropertyValueType.getTypeForXmlTag(xmlTagName);
        if (propertyType != null) {
            return newPropertyValue(partId, propertyType.getInterfaceClass());
        } else if (LegacyConfigElement.XML_TAG.equals(xmlTagName)) {
            return new LegacyConfigElement(this);
        }
        return null;
    }

    /**
     * Creating a property value for the given class type.
     * 
     * Caution: This Method creates an {@link IPropertyValue} without initializing it properly and
     * thereby setting the property name to "". Use
     * {@link #newPropertyValue(IProductCmptProperty, String, Class)} in all cases an
     * {@link IProductCmptProperty} is available.
     * 
     * @param partId the part id for the new property value
     * @param type the type of the property value you want to create
     * 
     * @param <T> The type of the property value you want to create
     * @return the created property value
     */
    public <T extends IPropertyValue> T newPropertyValue(String partId, Class<T> type) {
        return newPropertyValue((IProductCmptProperty)null, partId, type);
    }

    /**
     * Creating a new {@link IPropertyValue} and initialize it with the given parameters. The clazz
     * specifying the type of the container. The caller have to make sure that the given
     * {@link IProductCmptProperty} is of the correct type.
     * 
     * @param property the {@link IProductCmptProperty} the {@link IPropertyValue} is created for
     * @param partId the new part's id
     * @param clazz the class parameter is used to get a type safe return value.
     * @return the newly created {@link IPropertyValue} or <code>null</code> if the given property
     *             is <code>null</code>.
     */
    public <T extends IPropertyValue> T newPropertyValue(IProductCmptProperty property, String partId, Class<T> clazz) {
        T propertyValue = PropertyValueType.createPropertyValue(getPropertyValueContainer(), property, partId, clazz);
        addPropertyValue(propertyValue);
        return propertyValue;
    }

    /**
     * Adds the given property value to this holder. Note that all #newPropertyValue() methods add
     * the newly created part automatically.
     * 
     * @param value the value to be added
     */
    public boolean addPropertyValue(IPropertyValue value) {
        Class<? extends IPropertyValue> interfaceClass = value.getPropertyValueType().getInterfaceClass();
        List<? extends IPropertyValue> list = classToInstancesMap.get(interfaceClass);
        if (list.contains(value)) {
            return true;
        } else {
            return classToInstancesMap.putWithRuntimeCheck(interfaceClass, value) != null;
        }
    }

    /**
     * Removes the given property value from this {@link PropertyValueCollection}.
     * 
     * @param value the value to be removed
     * @return <code>true</code> if the given value was removed from this holder, <code>false</code>
     *             otherwise.
     */
    public boolean removePropertyValue(IPropertyValue value) {
        return classToInstancesMap.remove(value.getPropertyValueType().getInterfaceClass(), value);
    }

    /**
     * Returns a list of all {@link IPropertyValue}s in this container. Parts are sorted by
     * {@link ProductCmptPropertyType} and their name.
     *
     * @return all property values this container manages.
     */
    public List<IPropertyValue> getAllPropertyValues() {
        return classToInstancesMap.values().stream().sorted(COMPARATOR).collect(Collectors.toList());
    }

    /**
     * Removes all {@link IPropertyValue}s from this holder.
     */
    public void clear() {
        classToInstancesMap.clear();
    }

    public IPropertyValueContainer getPropertyValueContainer() {
        return propertyValueContainer;
    }

}
