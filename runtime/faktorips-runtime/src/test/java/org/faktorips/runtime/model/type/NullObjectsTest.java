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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.values.Decimal;
import org.faktorips.values.Money;
import org.junit.Test;

public class NullObjectsTest {

    @Test
    public void testOf() {
        assertThat(NullObjects.of(String.class), is(IpsStringUtils.EMPTY));
        assertThat(NullObjects.of(Decimal.class), is(Decimal.NULL));
        assertThat(NullObjects.of(Money.class), is(Money.NULL));
        assertThat(NullObjects.of(Integer.class), is(nullValue()));
        assertThat(NullObjects.of(short.class), is((short)0));
        assertThat(NullObjects.of(int.class), is(0));
        assertThat(NullObjects.of(long.class), is(0L));
        assertThat(NullObjects.of(double.class), is(0.0d));
        assertThat(NullObjects.of(float.class), is(0.0f));
        assertThat(NullObjects.of(boolean.class), is(false));
    }

}
