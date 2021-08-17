/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.internal.productcmpt.ProductCmptLink;
import org.faktorips.devtools.model.productcmpt.Cardinality;
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
