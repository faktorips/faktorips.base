/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.model.type;

import java.util.HashMap;
import java.util.Map;

import org.faktorips.annotation.UtilityClass;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.values.Decimal;
import org.faktorips.values.Money;
import org.faktorips.values.NullObject;
import org.faktorips.values.NullObjectSupport;

/**
 * Helper to get the {@link NullObject} for datatypes, handling primitives, Strings and
 * {@link NullObjectSupport}.
 *
 * @since 24.1
 */
@UtilityClass
class NullObjects {

    private static final Map<Class<?>, Object> NULL_OBJECTS = new HashMap<>();
    static {
        NULL_OBJECTS.put(Decimal.class, Decimal.NULL);
        NULL_OBJECTS.put(Money.class, Money.NULL);
        NULL_OBJECTS.put(String.class, IpsStringUtils.EMPTY);
        NULL_OBJECTS.put(short.class, (short)0);
        NULL_OBJECTS.put(int.class, 0);
        NULL_OBJECTS.put(long.class, 0L);
        NULL_OBJECTS.put(double.class, 0.0d);
        NULL_OBJECTS.put(float.class, 0.0f);
    }

    private NullObjects() {
        // Util
    }

    @SuppressWarnings("unchecked")
    static <T> T of(Class<T> clazz) {
        return (T)NULL_OBJECTS.get(clazz);
    }

}
