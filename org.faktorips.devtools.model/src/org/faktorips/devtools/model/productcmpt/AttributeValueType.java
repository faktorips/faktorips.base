/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.productcmpt;

import org.faktorips.devtools.model.internal.productcmpt.AbstractValueHolder;
import org.faktorips.devtools.model.internal.productcmpt.MultiValueHolder;
import org.faktorips.devtools.model.internal.productcmpt.SingleValueHolder;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.value.IValue;
import org.w3c.dom.Element;

/**
 * An enumeration for legal {@link IValueHolder value holders} in an {@link IAttributeValue}.
 * <p>
 * This enum maps all possible types to the type specified in the XML and provides the corresponding
 * factories to instantiate the value holders.
 * 
 * @author dirmeier
 */
public enum AttributeValueType {

    /**
     * The default value type, holding only {@link String} values.
     */
    SINGLE_VALUE(ISingleValueHolder.DEFAULT_XML_TYPE_NAME) {
        @Override
        public boolean isResponsibleFor(IProductCmptTypeAttribute attribute) {
            return !attribute.isMultiValueAttribute();
        }

        @Override
        @SuppressWarnings("unchecked")
        // we use a covariant return type here
        public Class<SingleValueHolder.Factory> getValueHolderFactory() {
            return SingleValueHolder.Factory.class;
        }

    },

    /**
     * A multi value type holding a list of {@link String} values.
     */
    MULTI_VALUE(IMultiValueHolder.XML_TYPE_NAME) {
        @Override
        public boolean isResponsibleFor(IProductCmptTypeAttribute attribute) {
            return attribute.isMultiValueAttribute();
        }

        @Override
        @SuppressWarnings("unchecked")
        // we use a covariant return type here
        public Class<MultiValueHolder.Factory> getValueHolderFactory() {
            return MultiValueHolder.Factory.class;
        }

    };

    private final String xmlTypeName;

    <T> AttributeValueType(String xmlTypeName) {
        this.xmlTypeName = xmlTypeName;
    }

    /**
     * Getting the name of the type as it is stored in the XML.
     * 
     * @return the XML type name
     */
    public String getXmlTypeName() {
        return xmlTypeName;
    }

    /**
     * Getting the class that implements the {@link IValueHolder}. This class should implement
     * {@link AbstractValueHolder} and must have a constructor with the parent
     * {@link IIpsObjectPart} as one and only argument.
     * 
     * @return the valueHolderFactory
     */
    public abstract <T> Class<? extends IAttributeValueHolderFactory<T>> getValueHolderFactory();

    /**
     * Creates a new instance of value holder for an attribute value. The type of the value holder
     * is not validated with the type expected by the attribute.
     * 
     * @param attributeValue The attribute value used as parent object in the new
     *            {@link IValueHolder}. The new value holder is not set in the specified attribute
     *            value.
     * 
     * @return the new value holder with the specified default value and the attribute set as parent
     */
    public <T> IValueHolder<T> newHolderInstance(IAttributeValue attributeValue) {
        try {
            Class<? extends IAttributeValueHolderFactory<T>> valueHolderFactory = getValueHolderFactory();
            IAttributeValueHolderFactory<T> factory = valueHolderFactory.getConstructor().newInstance();
            return factory.createValueHolder(attributeValue);
            // CSOFF: IllegalCatch
        } catch (Exception e) {
            // CSON: IllegalCatch
            throw new RuntimeException(e);
        }

    }

    /**
     * Creates a new instance of value holder for an attribute value and setting the default. The
     * type of the value holder is not validated with the type expected by the attribute.
     * 
     * @param attributeValue The attribute value used as parent object in the new
     *            {@link IValueHolder}. The new value holder is not set in the specified attribute
     *            value.
     * @param defaultValue The default value set in the new value holder. The type of the default
     *            holder is always an {@link IValue IValue&lt;?&gt;}. In case of multiple values,
     *            the default value will be split using {@link IMultiValueHolder#SEPARATOR}
     * 
     * @return the new value holder with the specified default value and the attribute set as parent
     */
    public <T> IValueHolder<T> newHolderInstance(IAttributeValue attributeValue, IValue<?> defaultValue) {
        try {
            Class<? extends IAttributeValueHolderFactory<T>> valueHolderFactory = getValueHolderFactory();
            IAttributeValueHolderFactory<T> factory = valueHolderFactory.getConstructor().newInstance();
            return factory.createValueHolder(attributeValue, defaultValue);
            // CSOFF: IllegalCatch
        } catch (Exception e) {
            // CSON: IllegalCatch
            throw new RuntimeException(e);
        }
    }

    /**
     * Checking whether this {@link AttributeValueType} is responsible for the specified enum. For
     * example {@link #MULTI_VALUE} would return true only for attributes that are multi value
     * attributes.
     * 
     * @param attribute The attribute you want to check against this {@link AttributeValueType}
     * @return {@code true} if this {@link AttributeValueType} is responsible for the specified
     *             attribute, {@code false} if not
     */
    public abstract boolean isResponsibleFor(IProductCmptTypeAttribute attribute);

    /**
     * Returns the {@link AttributeValueType} for the specified XML element. The element needs to
     * specify the attribute {@link IValueHolder#XML_ATTRIBUTE_VALUE_TYPE}.
     * 
     * @param valueEl The XML element with the {@link IValueHolder#XML_ATTRIBUTE_VALUE_TYPE}
     *            attribute.
     * 
     * @return Returns the {@link AttributeValueType} instance that matches the type specified in
     *             the XML element
     * 
     * @see #getType(String)
     */
    public static AttributeValueType getType(Element valueEl) {
        String valueType = valueEl == null ? null : valueEl.getAttribute(IValueHolder.XML_ATTRIBUTE_VALUE_TYPE);
        return getType(valueType);
    }

    /**
     * Returns the {@link AttributeValueType} that matches the given type name. If there is no
     * matching type the {@link #SINGLE_VALUE} type is returned by default.
     * 
     * @param typeName The name of the type as it is stored in an XML
     * 
     * @return The {@link AttributeValueType} that matches the given type name
     */
    public static AttributeValueType getType(String typeName) {
        for (AttributeValueType type : values()) {
            if (type.xmlTypeName.equals(typeName)) {
                return type;
            }
        }
        return SINGLE_VALUE;
    }

    /**
     * Returning the first {@link AttributeValueType} that is responsible for the given attribute.
     * 
     * @see #isResponsibleFor(IProductCmptTypeAttribute)
     * 
     * @param attribute The attribute you want to get an {@link AttributeValueType} for
     * @return The {@link AttributeValueType} that matches the specified attribute.
     */
    public static AttributeValueType getTypeFor(IProductCmptTypeAttribute attribute) {
        if (attribute != null) {
            for (AttributeValueType type : values()) {
                if (type.isResponsibleFor(attribute)) {
                    return type;
                }
            }
        }
        return SINGLE_VALUE;
    }

}
