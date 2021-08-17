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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.BooleanSupplier;

import org.eclipse.jface.action.IAction;
import org.junit.Test;

public class DynamicallyVisibleActionContributionItemTest {

    @Test
    public void testUpdate() throws Exception {
        IAction action = mock(IAction.class);
        BooleanSupplier visibleSupplier = mock(BooleanSupplier.class);
        when(visibleSupplier.getAsBoolean()).thenReturn(true, false);

        DynamicallyVisibleActionContributionItem item = new DynamicallyVisibleActionContributionItem(action,
                visibleSupplier);

        verify(visibleSupplier, times(1)).getAsBoolean();
        assertThat(item.isVisible(), is(true));

        item.update();

        verify(visibleSupplier, times(2)).getAsBoolean();
        assertThat(item.isVisible(), is(false));
    }

}
