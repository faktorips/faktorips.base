/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.util;

import static org.faktorips.runtime.util.ReflectionHelper.findFieldValue;
import static org.faktorips.runtime.util.ReflectionHelper.findStaticFieldValue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import org.junit.Test;

public class ReflectionHelperTest {

    private static final String STATIC_FIELD = "foo";
    @SuppressWarnings("unused")
    private static final String STATIC_NULL_FIELD = null;
    private final int field = 23;
    @SuppressWarnings("unused")
    private final String nullField = null;

    @Test
    public void testFindStaticFieldValue() {
        Optional<String> staticFieldValue = findStaticFieldValue(ReflectionHelperTest.class, "STATIC_FIELD");
        assertThat(staticFieldValue.isPresent(), is(true));
        assertThat(staticFieldValue.get(), is(STATIC_FIELD));
    }

    @Test
    public void testFindStaticFieldValue_Null() {
        Optional<String> staticFieldValue = findStaticFieldValue(ReflectionHelperTest.class, "STATIC_NULL_FIELD");
        assertThat(staticFieldValue.isEmpty(), is(true));
    }

    @Test
    public void testFindStaticFieldValue_NotPresent() {
        Optional<String> staticFieldValue = findStaticFieldValue(ReflectionHelperTest.class, "UNKNOWN_FIELD");
        assertThat(staticFieldValue.isEmpty(), is(true));
    }

    @Test
    public void testFindStaticFieldValue_WrongType() {
        assertThrows(ClassCastException.class,
                () -> {
                    Optional<Integer> oi = ReflectionHelper.<Integer> findStaticFieldValue(ReflectionHelperTest.class,
                            "STATIC_FIELD");
                    Integer i = oi.get();
                    System.out.println(i);
                });
    }

    @Test
    public void testFindFieldValue_Static() {
        Optional<String> staticFieldValue = findFieldValue(ReflectionHelperTest.class, "STATIC_FIELD", null);
        assertThat(staticFieldValue.isPresent(), is(true));
        assertThat(staticFieldValue.get(), is(STATIC_FIELD));
    }

    @Test
    public void testFindFieldValue() {
        Optional<Integer> fieldValue = findFieldValue(ReflectionHelperTest.class, "field", this);
        assertThat(fieldValue.isPresent(), is(true));
        assertThat(fieldValue.get(), is(field));
    }

    @Test
    public void testFindFieldValue_Null() {
        Optional<Integer> fieldValue = findFieldValue(ReflectionHelperTest.class, "nullField", this);
        assertThat(fieldValue.isEmpty(), is(true));
    }

    @Test
    public void testFindFieldValue_NotPresent() {
        Optional<Integer> fieldValue = findFieldValue(ReflectionHelperTest.class, "unknownField", this);
        assertThat(fieldValue.isEmpty(), is(true));
    }

    @Test
    public void testFindFieldValue_WrongType() {
        assertThrows(ClassCastException.class,
                () -> {
                    Optional<String> os = ReflectionHelper.<ReflectionHelperTest, String> findFieldValue(
                            ReflectionHelperTest.class,
                            "field", this);
                    String s = os.get();
                    System.out.println(s);
                });
    }
}
