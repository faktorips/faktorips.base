/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.product.conditions.types;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.productcmpt.DelegatingValueHolder;
import org.faktorips.devtools.model.productcmpt.IProductPartsContainer;
import org.faktorips.devtools.model.productcmpt.IValueHolder;
import org.junit.Test;

public class AbstractSearchOperatorTest extends AbstractIpsPluginTest {

    @Test
    public void testCheck_NotWhenOperandNull() {
        IOperandProvider operandProvider = mock(IOperandProvider.class);
        TestSearchOperator searchOperator = spy(new TestSearchOperator(operandProvider));
        IProductPartsContainer productPartsContainer = mock(IProductPartsContainer.class);

        searchOperator.check(productPartsContainer);

        verify(searchOperator, never()).check(any(), any(IProductPartsContainer.class));
    }

    @Test
    public void testCheck_WhenOperandNotNull() {
        IOperandProvider operandProvider = mock(IOperandProvider.class);
        TestSearchOperator searchOperator = spy(new TestSearchOperator(operandProvider));
        IProductPartsContainer productPartsContainer = mock(IProductPartsContainer.class);
        when(operandProvider.getSearchOperand(productPartsContainer)).thenReturn("Foo");

        searchOperator.check(productPartsContainer);

        verify(searchOperator).check("Foo", productPartsContainer);
    }

    @Test
    public void testCheck_WhenOperandIsDelegate() {
        IOperandProvider operandProvider = mock(IOperandProvider.class);
        TestSearchOperator searchOperator = spy(new TestSearchOperator(operandProvider));
        IProductPartsContainer productPartsContainer = mock(IProductPartsContainer.class);
        DelegatingValueHolder<?> delegate = mock(DelegatingValueHolder.class);
        IValueHolder<?> valueHolder = mock(IValueHolder.class);
        when(operandProvider.getSearchOperand(productPartsContainer)).thenReturn(delegate);
        doReturn(valueHolder).when(delegate).getDelegate();
        searchOperator.check(productPartsContainer);

        verify(searchOperator).check(valueHolder, productPartsContainer);
    }

    public static class TestSearchOperatorType implements ISearchOperatorType {

        @Override
        public String getLabel() {
            return null;
        }

        @Override
        public ISearchOperator createSearchOperator(IOperandProvider operandProvider,
                ValueDatatype valueDatatype,
                String argument) {
            return null;
        }

        @Override
        public String name() {
            return null;
        }

    }

    public static class TestSearchOperator extends AbstractSearchOperator<TestSearchOperatorType> {

        public TestSearchOperator(IOperandProvider operandProvider) {
            super(null, null, operandProvider, null);
        }

        @Override
        protected boolean check(Object searchOperand, IProductPartsContainer productPartsContainer) {
            return false;
        }

    }

}
