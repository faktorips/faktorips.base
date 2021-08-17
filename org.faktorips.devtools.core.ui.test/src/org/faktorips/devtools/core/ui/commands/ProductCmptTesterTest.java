/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.commands;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.junit.Test;

public class ProductCmptTesterTest {

    private static final Object[] NO_ARGS = new Object[] {};

    @Test
    public void testTest_NullValues() throws Exception {
        ProductCmptTester tester = new ProductCmptTester();
        assertThat(tester.test(null, null, null, null), is(false));
    }

    @Test
    public void testTest_IsProductTemplate() throws Exception {
        IProductCmptType type = mock(IProductCmptType.class);
        IProductCmpt product = mock(IProductCmpt.class);
        IProductCmpt template = mock(IProductCmpt.class);
        when(template.isProductTemplate()).thenReturn(true);

        ProductCmptTester tester = new ProductCmptTester();

        assertThat(tester.test(type, ProductCmptTester.IS_PRODUCT_TEMPLATE, NO_ARGS, null), is(false));
        assertThat(tester.test(product, ProductCmptTester.IS_PRODUCT_TEMPLATE, NO_ARGS, null), is(false));
        assertThat(tester.test(template, ProductCmptTester.IS_PRODUCT_TEMPLATE, NO_ARGS, null), is(true));
    }

}
