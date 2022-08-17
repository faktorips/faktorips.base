/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.core.table;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.junit.Test;
import org.mockito.InOrder;

public class AlternateRowTablePageElementLayoutTest {

    @Test
    public void testHeadlineIsIgnored() {
        TableRowPageElement rowPageElement = mock(TableRowPageElement.class);

        when(rowPageElement.hasStyle(Style.TABLE_HEADLINE)).thenReturn(true);

        AlternateRowTablePageElementLayout layout = new AlternateRowTablePageElementLayout(true);

        layout.layoutRow(0, rowPageElement);
        layout.layoutRow(1, rowPageElement);
        layout.layoutRow(2, rowPageElement);

        layout = new AlternateRowTablePageElementLayout(false);

        layout.layoutRow(0, rowPageElement);
        layout.layoutRow(1, rowPageElement);
        layout.layoutRow(2, rowPageElement);

        verify(rowPageElement, never()).addStyles(any(Style[].class));
    }

    @Test
    public void testFirstLineIsIgnored() {
        TableRowPageElement rowPageElement = mock(TableRowPageElement.class);

        when(rowPageElement.hasStyle(Style.TABLE_HEADLINE)).thenReturn(false);

        AlternateRowTablePageElementLayout layout = new AlternateRowTablePageElementLayout(true);

        layout.layoutRow(0, rowPageElement);
        layout.layoutRow(1, rowPageElement);
        layout.layoutRow(2, rowPageElement);
        layout.layoutRow(3, rowPageElement);

        InOrder inOrder = inOrder(rowPageElement);
        inOrder.verify(rowPageElement).addStyles(Style.TABLE_ROW_EVEN);
        inOrder.verify(rowPageElement).addStyles(Style.TABLE_ROW_UNEVEN);
        inOrder.verify(rowPageElement).addStyles(Style.TABLE_ROW_EVEN);
        inOrder.verify(rowPageElement, never()).addStyles(Style.TABLE_ROW_EVEN);
        inOrder.verify(rowPageElement, never()).addStyles(Style.TABLE_ROW_UNEVEN);
    }

    @Test
    public void testFirstLineIsNotIgnored() {
        TableRowPageElement rowPageElement = mock(TableRowPageElement.class);

        when(rowPageElement.hasStyle(Style.TABLE_HEADLINE)).thenReturn(false);

        AlternateRowTablePageElementLayout layout = new AlternateRowTablePageElementLayout(false);

        layout.layoutRow(0, rowPageElement);
        layout.layoutRow(1, rowPageElement);
        layout.layoutRow(2, rowPageElement);
        layout.layoutRow(3, rowPageElement);

        InOrder inOrder = inOrder(rowPageElement);
        inOrder.verify(rowPageElement).addStyles(Style.TABLE_ROW_UNEVEN);
        inOrder.verify(rowPageElement).addStyles(Style.TABLE_ROW_EVEN);
        inOrder.verify(rowPageElement).addStyles(Style.TABLE_ROW_UNEVEN);
        inOrder.verify(rowPageElement).addStyles(Style.TABLE_ROW_EVEN);
    }
}
