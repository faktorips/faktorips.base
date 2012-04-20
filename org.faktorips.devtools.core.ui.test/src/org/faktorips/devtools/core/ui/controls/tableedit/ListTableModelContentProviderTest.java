/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls.tableedit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.internal.model.productcmpt.SingleValueHolder;
import org.faktorips.devtools.core.ui.dialogs.MultiValueTableModel;
import org.faktorips.devtools.core.ui.dialogs.MultiValueTableModel.SingleValueViewItem;
import org.junit.Test;

public class ListTableModelContentProviderTest {

    @Test
    public void testGetElements() {
        ListTableModelContentProvider multiValueContentProvider = new ListTableModelContentProvider();
        List<SingleValueViewItem> list = new ArrayList<SingleValueViewItem>();
        MultiValueTableModel model = mock(MultiValueTableModel.class);

        when(model.getElements()).thenReturn(list);
        Object[] elements = multiValueContentProvider.getElements(model);
        assertEquals(0, elements.length);

        list.add(new SingleValueViewItem(new SingleValueHolder(null)));
        list.add(new SingleValueViewItem(new SingleValueHolder(null)));
        elements = multiValueContentProvider.getElements(model);
        assertEquals(2, elements.length);
    }
}
