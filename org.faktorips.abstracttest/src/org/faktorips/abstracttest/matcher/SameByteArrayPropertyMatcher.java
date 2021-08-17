/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.abstracttest.matcher;

import java.util.Arrays;
import java.util.function.Function;

import org.hamcrest.Matcher;

public class SameByteArrayPropertyMatcher<T> extends SamePropertyMatcher<T, byte[]> {

    public SameByteArrayPropertyMatcher(Function<T, byte[]> propertyGetter, String propertyDescription,
            T objectToMatch) {
        super(propertyGetter, propertyDescription, objectToMatch);
    }

    @Override
    public boolean matches(Object item) {
        return Arrays.equals(getNullSafe(item), getNullSafe(getObjectToMatch()));
    }

    public static <T> Matcher<T> sameByteArray(Function<T, byte[]> propertyGetter,
            String propertyDescription,
            T objectToMatch) {
        return new SameByteArrayPropertyMatcher<>(propertyGetter, propertyDescription, objectToMatch);
    }

}
