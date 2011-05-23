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
public class PropertyValueHolder {

    private final ClassToInstancesMap<IPropertyValue> classToInstancesMap;

    /**
     * Creates a new {@link PropertyValueHolder}.
     * 
     */
    public PropertyValueHolder() {
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
        return getPropertyValue(property.getProductCmptPropertyType(), property.getPropertyName());
    }

    /**
     * Returns the {@link IPropertyValue} corresponding to the given property name and
     * {@link ProductCmptPropertyType}.
     * <p>
     * The property value is searched by the property name. If there are multiple property values
     * with the same name, the first one is returned. Property values returned by this method may be
     * safely casted to the {@link ProductCmptPropertyType}'s value class.
     * 
     * @param type the type of {@link IPropertyValue} that is requested
     * @param propertyName the name of the requested property value
     * @return the {@link IPropertyValue} for the indicated type and name. Returns <code>null</code>
     *         if the given type is <code>null</code> or if no property could be found.
     */
    public IPropertyValue getPropertyValue(ProductCmptPropertyType type, String propertyName) {
        if (type == null) {
            return null;
        }
        return getPropertyValueFromList(classToInstancesMap.get(type.getValueClass()), propertyName);
    }

    /**
     * Searches all {@link IPropertyValue}s registered with this {@link PropertyValueHolder} for one
     * with the indicated name.
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
     * @param type the property type of the requested property value
     * @return a list of property values of the indicated type, or an empty list if none exist.
     */
    public List<IPropertyValue> getPropertyValues(ProductCmptPropertyType type) {
        return new ArrayList<IPropertyValue>(classToInstancesMap.get(type.getValueClass()));
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
     * @param property the {@link IProductCmptProperty} the {@link IPropertyValue} is created for
     * @param partId the new part's id
     * @return the newly created {@link IPropertyValue} or <code>null</code> if the given property
     *         is <code>null</code>.
     */
    public IPropertyValue newPropertyValue(IPropertyValueContainer container,
            IProductCmptProperty property,
            String partId) {
        if (property == null) {
            return null;
        }
        IPropertyValue propertyValue = property.getProductCmptPropertyType().createPropertyValue(container, property,
                partId);
        addPropertyValue(propertyValue);
        return propertyValue;
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
            IPropertyValue newPropertyValue = newPropertyValue(container, propertyType, partId);
            return newPropertyValue;
        }
        return null;
    }

    /**
     * Caution: This Method creates an {@link IPropertyValue} without initializing it properly and
     * thereby setting the property name to "". Use
     * {@link #newPropertyValue(IPropertyValueContainer, IProductCmptProperty, String)} in all cases
     * an {@link IProductCmptProperty} is available.
     * 
     * @param type the {@link ProductCmptPropertyType} of the created {@link IPropertyValue}
     * @param partId the new part's id
     * @return the newly created {@link IPropertyValue}
     */
    public IPropertyValue newPropertyValue(IPropertyValueContainer container,
            ProductCmptPropertyType type,
            String partId) {
        IPropertyValue value = type.createPropertyValue(container, null, partId);
        addPropertyValue(value);
        return value;
    }

    /**
     * Adds the given property value to this holder. Note that all #newPropertyValue() methods add
     * the newly created part automatically.
     * 
     * @param value the value to be added
     */
    public boolean addPropertyValue(IPropertyValue value) {
        classToInstancesMap.putWithRuntimeCheck(value.getPropertyType().getValueClass(), value);
        return true;
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

    // /**
    // * Adds the given part to this holder. If both the following are valid:
    // * <ul>
    // * <li>the part is an {@link IPropertyValue}</li>
    // * <li>this holder's {@link IPropertyValueContainer} is the part's parent</li>
    // * </ul>
    // *
    // * @param part the part to add
    // * @return <code>true</code> if the part was added to this holder, <code>false</code>
    // otherwise.
    // */
    // public boolean addPart(IPropertyValue part) {
    // addPropertyValue(part);
    // return true;
    // }
    //
    // /**
    // * Removes the given part from this holder.
    // *
    // * @param part the part to remove
    // * @return <code>true</code> if the part was removed from this holder, <code>false</code>
    // * otherwise.
    // */
    // public boolean removePart(IPropertyValue part) {
    // return removePropertyValue(part);
    // }

}