/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.value.StringValue;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.AttributeValueType;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValueHolderFactory;
import org.faktorips.devtools.core.model.productcmpt.IValueHolder;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.value.IValue;
import org.faktorips.devtools.core.model.value.ValueFactory;
import org.faktorips.devtools.core.model.value.ValueType;
import org.faktorips.util.message.MessageList;
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
public class SingleValueHolder extends AbstractValueHolder<IValue<?>> {

    public static final String DEFAULT_XML_TYPE_NAME = "SingleValue"; //$NON-NLS-1$

    private IValue<?> value;

    private final Observer valueObserver;

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
        valueObserver = new Observer() {

            @Override
            public void update(Observable arg0, Object newValue) {
                objectHasChanged(null, newValue);
            }
        };
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
        try {
            if (parent != null && parent.getIpsProject() != null) {
                return parent.findAttribute(parent.getIpsProject());
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        return null;
    }

    private void setValueInternal(IValue<?> value) {
        if (this.value != null) {
            this.value.deleteObserver(valueObserver);
        }
        this.value = value;
        if (this.value != null) {
            this.value.addObserver(valueObserver);
        }
    }

    @Override
    public IAttributeValue getParent() {
        return (IAttributeValue)super.getParent();
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
    public ValueType getValueType() {
        return ValueType.getValueType(getValue());
    }

    @Override
    public boolean isMultiValue() {
        return false;
    }

    @Override
    public MessageList validate(IIpsProject ipsProject) throws CoreException {
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

    /**
     * @deprecated Use {@link #setValue(IValue)}
     */
    @Deprecated
    @Override
    public void setStringValue(String value) {
        setValue(new StringValue(value));
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
        if (value.equals(o.getValue())) {
            return 0;
        }
        return value.getContentAsString().compareTo(o.getValue().getContentAsString());
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
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
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
