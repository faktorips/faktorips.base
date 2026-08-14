/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.productcmpt;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.faktorips.devtools.model.internal.productcmpt.SingleValueHolder;
import org.faktorips.devtools.model.internal.value.StringValue;
import org.faktorips.devtools.model.value.IValue;
import org.junit.jupiter.api.Test;

public class DelegatingValueHolderTest<T> {

    private static final String VALUE = "foo";

    IAttributeValue parent = mock(IAttributeValue.class);
    IAttributeValue delegateParent = mock(IAttributeValue.class);
    SingleValueHolder delegate = new SingleValueHolder(delegateParent, VALUE);
    DelegatingValueHolder<IValue<?>> delegatingValueHolder = DelegatingValueHolder.of(parent, delegate);

    @Test
    public void testGetStringValue() {
        assertThat(delegatingValueHolder.getStringValue(), is(VALUE));
    }

    @Test
    public void testGetValue() {
        assertThat(delegatingValueHolder.getValue(), is((Object)delegate.getValue()));
    }

    @Test
    public void testSetValue() {
        assertThrows(UnsupportedOperationException.class, () -> {
            delegatingValueHolder.setValue(new StringValue("foo"));
        });
    }

    @Test
    public void testEqualsValueHolder() throws Exception {
        assertTrue(delegatingValueHolder.equalsValueHolder(delegatingValueHolder));
        assertTrue(delegate.equalsValueHolder(delegate));
        assertTrue(delegate.equalsValueHolder(delegatingValueHolder));
        assertTrue(delegatingValueHolder.equalsValueHolder(delegate));
        SingleValueHolder other = new SingleValueHolder(delegateParent, "bar");
        assertFalse(delegate.equalsValueHolder(other));
        assertFalse(other.equalsValueHolder(delegate));
        assertFalse(delegatingValueHolder.equalsValueHolder(other));
        assertFalse(other.equalsValueHolder(delegatingValueHolder));
    }

}
