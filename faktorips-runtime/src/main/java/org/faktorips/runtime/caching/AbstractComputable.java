/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.caching;

/**
 * Basic implementation of {@link IComputable}, taking the value class as a parameter.
 * <p>
 * Use {@link IComputable#of(Class, java.util.function.Function)} instead of extending this class if
 * your computation method can be expressed as a method reference or short lambda expression.
 */
public abstract class AbstractComputable<K, V> implements IComputable<K, V> {

    private final Class<? super V> valueClass;

    public AbstractComputable(Class<? super V> valueClass) {
        this.valueClass = valueClass;
    }

    @Override
    public Class<? super V> getValueClass() {
        return valueClass;
    }

}
