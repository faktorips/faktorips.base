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

import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;

/**
 * Wrapper around {@link MatcherAssert#assertThat(Object, Matcher)} calls to be used fluent API
 * style:
 * <p>
 *
 * <pre>
 * public FluentAssert whenFooBar(Foo foo){
 *   foo.setBar();
 *   return FluentAssert.INSTANCE;
 * }
 * [...]
 * whenFooBar().assertThat(foo, is(bar));
 * </pre>
 *
 * or
 *
 * <pre>
 * [...]
 * when(()->foo.set(bar)).assertThat(foo, is(bar));
 * </pre>
 */
public enum FluentAssert {

    INSTANCE;

    public static FluentAssert when(SetUp setUp) throws Exception {
        setUp.setUp();
        return INSTANCE;
    }

    /**
     * @see MatcherAssert#assertThat(Object, Matcher)
     */
    public <T> void assertThat(T actual, Matcher<? super T> matcher) {
        org.hamcrest.MatcherAssert.assertThat(actual, matcher);
    }

    /**
     * @see MatcherAssert#assertThat(String, Object, Matcher)
     */
    public <T> void assertThat(String reason, T actual, Matcher<? super T> matcher) {
        org.hamcrest.MatcherAssert.assertThat(reason, actual, matcher);
    }

    /**
     * @see MatcherAssert#assertThat(String, boolean)
     */
    public <T> void assertThat(String reason, boolean assertion) {
        org.hamcrest.MatcherAssert.assertThat(reason, assertion);
    }

    @FunctionalInterface
    public static interface SetUp {
        void setUp() throws Exception;
    }
}
