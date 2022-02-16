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

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.ObjectUtils;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.internal.value.StringValue;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.AttributeValueType;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IAttributeValueHolderFactory;
import org.faktorips.devtools.model.productcmpt.ISingleValueHolder;
import org.faktorips.devtools.model.productcmpt.IValueHolder;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.value.IValue;
import org.faktorips.devtools.model.value.ValueFactory;
import org.faktorips.devtools.model.value.ValueType;
import org.faktorips.runtime.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The default value holder for attribute values holding a single value. The StringValue does not
 * mean that the element in the model needs to be of type String but that it is persisted as a
 * String value. It can also be an InternationalStringValue.
 * 
 * @since 3.7
 * @author dirmeier
 */
public class SingleValueHolder extends AbstractValueHolder<IValue<?>> implements ISingleValueHolder {

    public static final String DEFAULT_XML_TYPE_NAME = "SingleValue"; //$NON-NLS-1$

    private IValue<?> value;

    private final PropertyChangeListener propertyChangeListener;

    /**
     * Create a new SingleValueHolder with an empty {@link IValue} as value
     * 
     * @param parent Attribute
     */
    public SingleValueHolder(IAttributeValue parent) {
        this(parent, createValueInternal(parent));
    }

    /**
     * Create a new SingleValueHolder with a new StringValue
     * 
     * @param parent Attribute
     * @param value String
     */
    public SingleValueHolder(IAttributeValue parent, String value) {
        this(parent, ValueFactory.createStringValue(value));
    }

    /**
     * Create a new SingleValueHolder with the {@link IValue}
     * 
     * @param parent Attribute
     * @param value IValue
     */
    public SingleValueHolder(IAttributeValue parent, IValue<?> value) {
        super(parent);
        propertyChangeListener = evt -> objectHasChanged(null, evt.getNewValue());
        setValueInternal(value);
    }

    private static IValue<?> createValueInternal(IAttributeValue parent) {
        IProductCmptTypeAttribute attribute = findAttribute(parent);
        if (attribute != null) {
            return ValueFactory.createValue(attribute.isMultilingual(), null);
        } else {
            return ValueFactory.createStringValue(null);
        }
    }

    private static IProductCmptTypeAttribute findAttribute(IAttributeValue parent) {
        if (parent != null && parent.getIpsProject() != null) {
            return parent.findAttribute(parent.getIpsProject());
        }
        return null;
    }

    private void setValueInternal(IValue<?> value) {
        if (this.value != null) {
            this.value.removePropertyChangeListener(propertyChangeListener);
        }
        this.value = value;
        if (this.value != null) {
            this.value.addPropertyChangeListener(propertyChangeListener);
        }
    }

    @Override
    protected AttributeValueType getType() {
        return AttributeValueType.SINGLE_VALUE;
    }

    /**
     * @return Returns the value.
     */
    @Override
    public IValue<?> getValue() {
        return value;
    }

    @Override
    public void setValue(IValue<?> value) {
        IValue<?> oldValue = this.value;
        setValueInternal(value);
        objectHasChanged(oldValue, value);
    }

    @Override
    public List<IValue<?>> getValueList() {
        ArrayList<IValue<?>> result = new ArrayList<>(1);
        result.add(getValue());
        return result;
    }

    @Override
    public void setValueList(List<IValue<?>> values) {
        if (values.isEmpty()) {
            setValue(new StringValue(null));
        } else if (values.size() == 1) {
            setValue(values.get(0));
        } else {
            throw new IllegalArgumentException("Cannot set multiple values in a SingleValueHolder"); //$NON-NLS-1$
        }
    }

    @Override
    public ValueType getValueType() {
        return ValueType.getValueType(getValue());
    }

    @Override
    public boolean isMultiValue() {
        return false;
    }

    @Override
    public MessageList validate(IIpsProject ipsProject) {
        return new SingleValueHolderValidator(this, getParent(), ipsProject).validate();
    }

    @Override
    protected void contentToXml(Element valueEl, Document doc) {
        if (value != null && value.getContent() != null) {
            valueEl.appendChild(value.toXml(doc));
        }
    }

    @Override
    public void initFromXml(Element element) {
        setValueInternal(ValueFactory.createValue(element));
    }

    @Override
    public String getStringValue() {
        if (value != null) {
            return value.getContentAsString();
        }
        return null;
    }

    @Override
    public boolean isNullValue() {
        boolean isNull = true;
        if (getValue() != null) {
            isNull = getValue().getContent() == null;
        }
        return isNull;
    }

    @Override
    protected SingleValueHolderValidator newValidator(IAttributeValue parent, IIpsProject ipsProject) {
        return new SingleValueHolderValidator(this, parent, ipsProject);
    }

    @Override
    public int compareTo(IValueHolder<IValue<?>> o) {
        if (o == null) {
            return -1;
        }
        if (this.equals(o)) {
            return 0;
        }
        if (value == null) {
            return ObjectUtils.compare(null, o.getStringValue());
        }
        ValueDatatype datatype = getParent().findAttribute(getIpsProject()).findValueDatatype(getIpsProject());
        return value.compare(o.getValue(), datatype);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SingleValueHolder other = (SingleValueHolder)obj;
        return Objects.equals(value, other.value);
    }

    /**
     * This factory creates {@link SingleValueHolder} objects.
     * 
     * @author dirmeier
     */
    public static class Factory implements IAttributeValueHolderFactory<IValue<?>> {

        @Override
        public IValueHolder<IValue<?>> createValueHolder(IAttributeValue parent) {
            return new SingleValueHolder(parent);
        }

        @Override
        public IValueHolder<IValue<?>> createValueHolder(IAttributeValue parent, IValue<?> defaultValue) {
            return new SingleValueHolder(parent, defaultValue);
        }

    }

}
