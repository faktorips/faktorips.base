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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.faktorips.devtools.core.internal.model.value.InternationalStringValue;
import org.faktorips.devtools.core.internal.model.value.StringValue;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.value.ValueType;
import org.junit.Test;

public class SingleValueHolderTest {

    @Test
    public void testGetValueType() {
        IAttributeValue attributeValue = mock(IAttributeValue.class);

        SingleValueHolder singleValueHolder = new SingleValueHolder(attributeValue, "abc");
        assertEquals(ValueType.STRING, singleValueHolder.getValueType());

        singleValueHolder = new SingleValueHolder(attributeValue, new StringValue("abc"));
        assertEquals(ValueType.STRING, singleValueHolder.getValueType());

        singleValueHolder = new SingleValueHolder(attributeValue, new InternationalStringValue());
        assertEquals(ValueType.INTERNATIONAL_STRING, singleValueHolder.getValueType());
    }
}
