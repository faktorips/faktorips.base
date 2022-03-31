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

import java.util.List;
import java.util.Objects;

import com.google.common.base.Preconditions;

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IValueHolder;
import org.faktorips.devtools.model.value.IValue;
import org.faktorips.devtools.model.value.ValueType;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.Severity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A {@link IValueHolder} implementation that delegates read-access to another {@code IValueHolder}
 * and throws an {@code UnsupportedOperationException} for write-access.
 */
public class DelegatingValueHolder<T> implements IValueHolder<T> {

    private final IAttributeValue parent;
    private final AbstractValueHolder<T> delegate;

    public DelegatingValueHolder(IAttributeValue parent, AbstractValueHolder<T> delegate) {
        this.parent = Objects.requireNonNull(parent);
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public Element toXml(Document doc) {
        return delegate.toXml(doc);
    }

    @Override
    public void initFromXml(Element element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isValid(IIpsProject ipsProject) {
        return getValidationResultSeverity(ipsProject) != Severity.ERROR;
    }

    @Override
    public Severity getValidationResultSeverity(IIpsProject ipsProject) {
        return validate(ipsProject).getSeverity();
    }

    @Override
    public MessageList validate(IIpsProject ipsProject) {
        return delegate.newValidator(parent, ipsProject).validate();
    }

    @Override
    public IIpsProject getIpsProject() {
        return parent.getIpsProject();
    }

    @Override
    public int compareTo(IValueHolder<T> o) {
        if (o == null) {
            return 1;
        }
        if (o == this) {
            return 0;
        }
        return delegate.compareTo(o);
    }

    @Override
    public int hashCode() {
        return 31 + delegate.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (getClass() != o.getClass()) {
            return false;
        }

        return delegate.equals(((DelegatingValueHolder<?>)o).delegate);
    }

    @Override
    public boolean equalsValueHolder(IValueHolder<?> o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }

        if (o instanceof DelegatingValueHolder) {
            return delegate.equals(((DelegatingValueHolder<?>)o).delegate);
        } else {
            return delegate.equals(o);
        }
    }

    @Override
    public IAttributeValue getParent() {
        return parent;
    }

    @Override
    public String getStringValue() {
        return delegate.getStringValue();
    }

    @Override
    public T getValue() {
        return delegate.getValue();
    }

    @Override
    public void setValue(T value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<IValue<?>> getValueList() {
        return delegate.getValueList();
    }

    @Override
    public void setValueList(List<IValue<?>> values) {
        delegate.setValueList(values);
    }

    @Override
    public boolean isNullValue() {
        return delegate.isNullValue();
    }

    @Override
    public ValueType getValueType() {
        return delegate.getValueType();
    }

    @Override
    public boolean isMultiValue() {
        return delegate.isMultiValue();
    }

    /** Returns the value holder that read-access is delegated to. */
    public IValueHolder<T> getDelegate() {
        return delegate;
    }

    @Override
    public IValueHolder<?> copy(IAttributeValue parent) {
        return delegate.copy(parent);
    }

    /**
     * Creates a new {@code DelegatingValueHolder} to use by the given parent to delegate to the
     * given value holder.
     * 
     * @param parent the parent from which the value holder is delegated. This usually is not the
     *            parent of the delegate.
     * @param delegate the value holder to delegate to
     * @return a new {@code DelegatingValueHolder} with the given parent that delegates read-access
     *         to the given value holder
     */
    public static <U> DelegatingValueHolder<U> of(IAttributeValue parent, IValueHolder<U> delegate) {
        Preconditions.checkArgument(delegate instanceof AbstractValueHolder,
                "Can only delegate to AbstractValueHolder"); //$NON-NLS-1$

        return new DelegatingValueHolder<>(parent, (AbstractValueHolder<U>)delegate);
    }

}
