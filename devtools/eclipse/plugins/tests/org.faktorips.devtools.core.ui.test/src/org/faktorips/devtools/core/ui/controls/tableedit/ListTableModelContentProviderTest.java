/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls.tableedit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.ui.dialogs.MultiValueTableModel;
import org.faktorips.devtools.core.ui.dialogs.MultiValueTableModel.SingleValueViewItem;
import org.faktorips.devtools.model.internal.productcmpt.SingleValueHolder;
import org.junit.Test;

public class ListTableModelContentProviderTest {

    @Test
    public void testGetElements() {
        ListTableModelContentProvider multiValueContentProvider = new ListTableModelContentProvider();
        List<SingleValueViewItem> list = new ArrayList<>();
        MultiValueTableModel model = mock(MultiValueTableModel.class);

        when(model.getElements()).thenReturn(list);
        Object[] elements = multiValueContentProvider.getElements(model);
        assertEquals(0, elements.length);

        list.add(new SingleValueViewItem(new SingleValueHolder(null), 0));
        list.add(new SingleValueViewItem(new SingleValueHolder(null), 1));
        elements = multiValueContentProvider.getElements(model);
        assertEquals(2, elements.length);
    }
}
