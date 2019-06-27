/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.editors.productcmpt;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.core.internal.model.productcmpt.Cardinality;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmptLink;
import org.junit.Test;

public class TemplatedValueFormatterTest {

    @Test(expected = NullPointerException.class)
    public void testFormat_ThrowsExceptionForNullValue() {
        TemplatedValueFormatter.format(null);
    }

    @Test
    public void testFormat() {
        ProductCmptLink link = mock(ProductCmptLink.class);
        when(link.getCardinality()).thenReturn(new Cardinality(1, 1, 1));
        assertThat(TemplatedValueFormatter.format(link), is("[1..1, 1]"));
    }
}
