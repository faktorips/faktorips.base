/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.filter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.junit.Before;
import org.junit.Test;

public class AbstractProductCmptPropertyFilterTest {

    private TestFilter filter;

    @Before
    public void setUp() {
        filter = new TestFilter();
    }

    @Test
    public void testNotifyController() {
        IPropertyVisibleController controller = mock(IPropertyVisibleController.class);
        filter.setPropertyVisibleController(controller);

        filter.notifyController();

        verify(controller).updateUI();
    }

    private static class TestFilter extends AbstractProductCmptPropertyFilter {

        @Override
        public boolean isFiltered(IProductCmptProperty property) {
            return false;
        }

    }

}
