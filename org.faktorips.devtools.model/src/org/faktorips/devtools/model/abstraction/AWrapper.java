/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.abstraction;

import java.util.Objects;

/**
 * A wrapper is a platform-dependent implementation of an {@link AAbstraction abstraction} that can
 * be {@link #unwrap() unwrapped} to get access to the platform-specific code.
 */
public abstract class AWrapper<T> implements AAbstraction {

    private final T wrapped;

    public AWrapper(T wrapped) {
        this.wrapped = wrapped;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T unwrap() {
        return wrapped;
    }

    @Override
    public int hashCode() {
        return Objects.hash(wrapped);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AWrapper)) {
            return false;
        }
        AWrapper<?> other = (AWrapper<?>)obj;
        return Objects.equals(wrapped, other.wrapped);
    }

    @Override
    public String toString() {
        return unwrap().toString();
    }

}
