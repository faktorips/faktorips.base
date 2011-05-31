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

import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.faktorips.util.ClassToInstancesMap;

/**
 * Class for holding {@link IPropertyValue}s for a {@link IPropertyValueContainer}. An instance of
 * this class holds property values of different {@link ProductCmptPropertyType}'s at the same time.
 * 
 * @author Stefan Widmaier
 * @since 3.4
 */
public class PropertyValueCollection {

    private final ClassToInstancesMap<IPropertyValue> classToInstancesMap;

    /**
     * Creates a new {@link PropertyValueCollection}.
     * 
     */
    public PropertyValueCollection() {
        classToInstancesMap = new ClassToInstancesMap<IPropertyValue>();
    }

    /**
     * Returns the {@link IPropertyValue} corresponding to the given {@link IProductCmptProperty}.
     * <p>
     * The property value is searched by the property name. If there are multiple property values
     * with the same name, the first one is returned. Property values returned by this method may be
     * safely casted to the {@link IProductCmptProperty}'s value class.
     * 
     * @param property the product component property a {@link IPropertyValue} is requested for
     * @return the {@link IPropertyValue} for the indicated property. Returns <code>null</code> if
     *         the given property is <code>null</code> or if no property could be found.
     */
    public IPropertyValue getPropertyValue(IProductCmptProperty property) {
        if (property == null) {
            return null;
        }
        return getPropertyValue(property.getProductCmptPropertyType().getValueClass(), property.getPropertyName());
    }

    /**
     * Returns the property value corresponding to the given property name and of the given type.
     * <p>
     * The property value is searched by the property name. If there are multiple property values
     * with the same name, the first one is returned.
     * 
     * @param type the type of {@link IPropertyValue} that is requested
     * @param propertyName the name of the requested property value
     * @return the {@link IPropertyValue} for the indicated type and name. Returns <code>null</code>
     *         if no property could be found.
     */
    public <T extends IPropertyValue> T getPropertyValue(Class<T> type, String propertyName) {
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
     * one with the indicated name.
     * <p>
     * Note that a safe cast can not be guaranteed as {@link IPropertyValue}s of a different
     * class/type may have the same property name.
     * 
     * @param propertyName the name of the requested {@link IPropertyValue}
     * @return the {@link IPropertyValue} with the given name. Returns <code>null</code> if the
     *         given Property name is <code>null</code> or if no property with the indicated name
     *         could be found.
     */
    public IPropertyValue getPropertyValue(String propertyName) {
        List<IPropertyValue> allValues = getAllPropertyValues();
        return getPropertyValueFromList(allValues, propertyName);
    }

    /**
     * Searches the given list for a property with the given name. Returns <code>null</code> if no
     * element can be found, or if one of the arguments is <code>null</code>.
     * 
     * @param valueList the list to search
     * @param propertyName the name of the property
     * @return the first element with the given name in the list.
     */
    private static IPropertyValue getPropertyValueFromList(List<? extends IPropertyValue> valueList, String propertyName) {
        if (propertyName == null || valueList == null) {
            return null;
        }
        for (IPropertyValue value : valueList) {
            if (propertyName.equals(value.getPropertyName())) {
                return value;
            }
        }
        return null;
    }

    /**
     * Returns all {@link IPropertyValue}s of the given type this value container contains.
     * 
     * @param clazz the class type of the properties you want to get
     * @return a list of property values of the indicated type, or an empty list if none exist.
     */
    public <T extends IPropertyValue> List<T> getPropertyValues(Class<T> clazz) {
        return new ArrayList<T>(classToInstancesMap.get(clazz));
    }

    /**
     * Creates a new part for the given XML tag adds it to this holder and returns it.
     * 
     * @param xmlTagName the XML tag a {@link IPropertyValue} should be created for
     * @param partId the new part's id
     * @return the newly created part or <code>null</code> if the given XML tag corresponds to no
     *         {@link IPropertyValue} or {@link ProductCmptPropertyType} respectively.
     */
    public IPropertyValue newPropertyValue(IPropertyValueContainer container, String xmlTagName, String partId) {
        ProductCmptPropertyType propertyType = ProductCmptPropertyType.getTypeForXmlTag(xmlTagName);
        if (propertyType != null) {
            IPropertyValue newPropertyValue = newPropertyValue(container, partId, propertyType.getValueClass());
            return newPropertyValue;
        }
        return null;
    }

    /**
     * Creating a property value for the given class type.
     * 
     * Caution: This Method creates an {@link IPropertyValue} without initializing it properly and
     * thereby setting the property name to "". Use
     * {@link #newPropertyValue(IPropertyValueContainer, IProductCmptProperty, String, Class)} in
     * all cases an {@link IProductCmptProperty} is available.
     * 
     * @param <T> The type of the property value you want to create
     * @param container the container to add the property value into
     * @param partId the part id for the new property value
     * @param type the type of the property value you want to create
     * @return the created property value
     */
    public <T extends IPropertyValue> T newPropertyValue(IPropertyValueContainer container, String partId, Class<T> type) {
        return newPropertyValue(container, null, partId, type);
    }

    /**
     * Creating an unspecific {@link IPropertyValue} for the given container. The type of the
     * property is given by the {@link IProductCmptProperty}.
     * 
     * @param container The container in which the new property value should be added
     * @param property the {@link IProductCmptProperty} that is the meta class of the property value
     * @param partId the part id of the created property value
     * @return The newly created property value
     */
    public IPropertyValue newPropertyValue(IPropertyValueContainer container,
            IProductCmptProperty property,
            String partId) {
        return newPropertyValue(container, property, partId, property.getProductCmptPropertyType().getValueClass());
    }

    /**
     * Creating a new {@link IPropertyValue} that and initialize it with the given parameters. The
     * clazz specifying the type of the container. The caller have to make sure that the given
     * {@link IProductCmptProperty} is of the correct type.
     * 
     * @param container the container that should be the parent of the new {@link IPropertyValue}
     * @param property the {@link IProductCmptProperty} the {@link IPropertyValue} is created for
     * @param partId the new part's id
     * @param clazz the class parameter is used to get a type safe return value.
     * @return the newly created {@link IPropertyValue} or <code>null</code> if the given property
     *         is <code>null</code>.
     */
    public <T extends IPropertyValue> T newPropertyValue(IPropertyValueContainer container,
            IProductCmptProperty property,
            String partId,
            Class<T> clazz) {
        T propertyValue = ProductCmptPropertyType.createPropertyValue(container, property, partId, clazz);
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
        return classToInstancesMap.putWithRuntimeCheck(value.getPropertyType().getValueClass(), value) != null;
    }

    /**
     * Removes the given property value from this holder.
     * 
     * @param value the value to be removed
     * @return <code>true</code> if the given value was removed from this holder, <code>false</code>
     *         otherwise.
     */
    public boolean removePropertyValue(IPropertyValue value) {
        boolean removed = classToInstancesMap.remove(value.getPropertyType().getValueClass(), value);
        return removed;
    }

    /**
     * Returns a list of all {@link IPropertyValue}s in this container. Parts of the same
     * {@link ProductCmptPropertyType} retain their natural order but no order can be guaranteed in
     * which the different {@link ProductCmptPropertyType}'s are returned. e.g. it cannot be ensured
     * {@link IAttributeValue} are always the first elements in the returned list.
     * 
     * @return all property values this container manages.
     */
    public List<IPropertyValue> getAllPropertyValues() {
        return classToInstancesMap.values();
    }

    /**
     * Removes all {@link IPropertyValue}s from this holder.
     */
    public void clear() {
        classToInstancesMap.clear();
    }

}