/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.caching;

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
