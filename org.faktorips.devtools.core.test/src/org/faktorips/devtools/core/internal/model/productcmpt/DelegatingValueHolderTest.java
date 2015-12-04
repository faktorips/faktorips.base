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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.faktorips.devtools.core.internal.model.value.StringValue;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.value.IValue;
import org.junit.Test;

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

    @Test(expected = UnsupportedOperationException.class)
    public void testSetStringValue() {
        delegatingValueHolder.setStringValue("bar");
    }

    @Test
    public void testGetValue() {
        assertThat(delegatingValueHolder.getValue(), is((Object)delegate.getValue()));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetValue() {
        delegatingValueHolder.setValue(new StringValue("foo"));
    }

}
