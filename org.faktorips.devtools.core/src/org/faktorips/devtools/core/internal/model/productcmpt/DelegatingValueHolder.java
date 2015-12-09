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

import com.google.common.base.Preconditions;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IValueHolder;
import org.faktorips.devtools.core.model.value.ValueType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
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
        this.parent = Preconditions.checkNotNull(parent);
        this.delegate = Preconditions.checkNotNull(delegate);
    }

    @Override
    public Element toXml(Document doc) {
        return delegate.toXml(doc);
    }

    @Override
    public void initFromXml(Element element) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * 
     * @deprecated since 3.0.0, use {@link #getValidationResultSeverity(IIpsProject)}
     */
    @Override
    @Deprecated
    public boolean isValid() throws CoreException {
        return isValid(getIpsProject());
    }

    @Override
    public boolean isValid(IIpsProject ipsProject) throws CoreException {
        return getValidationResultSeverity(ipsProject) != Message.ERROR;
    }

    /**
     * {@inheritDoc}
     * 
     * @deprecated since 3.0.0, use {@link #getValidationResultSeverity(IIpsProject)}
     */
    @Override
    @Deprecated
    public int getValidationResultSeverity() throws CoreException {
        return getValidationResultSeverity(getIpsProject());
    }

    @Override
    public int getValidationResultSeverity(IIpsProject ipsProject) throws CoreException {
        return validate(ipsProject).getSeverity();
    }

    @Override
    public MessageList validate(IIpsProject ipsProject) throws CoreException {
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
    public IIpsObjectPart getParent() {
        return parent;
    }

    @Override
    public String getStringValue() {
        return delegate.getStringValue();
    }

    @Override
    public void setStringValue(String value) {
        throw new UnsupportedOperationException();
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
        Preconditions
                .checkArgument(delegate instanceof AbstractValueHolder, "Can only delegate to AbstractValueHolder"); //$NON-NLS-1$
        return new DelegatingValueHolder<U>(parent, (AbstractValueHolder<U>)delegate);
    }

}
