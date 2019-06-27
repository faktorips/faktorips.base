/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.wizards.productcmpt;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.ui.wizards.productcmpt.TypeAndTemplateSelectionComposite.LabelProvider;
import org.junit.Test;

public class TypeAndTemplateSelectionCompositeTest {

    private static final String TEST_DESCRIPTION = ("This is a test text");
    private LabelProvider labelProvider = new TypeAndTemplateSelectionComposite.LabelProvider();

    @Test
    public void testGetToolTipText_null() throws Exception {
        assertThat(labelProvider.getToolTipText(null), is(nullValue()));
    }

    @Test
    public void testGetToolTipText_describedElement() throws Exception {
        IDescribedElement describedElement = mock(IDescribedElement.class);
        IDescription description = mock(IDescription.class);
        when(description.getText()).thenReturn(TEST_DESCRIPTION);
        when(describedElement.getDescription(any(Locale.class))).thenReturn(description);

        assertThat(labelProvider.getToolTipText(describedElement), is(TEST_DESCRIPTION));
    }

    @Test
    public void testGetToolTipText_viewItem() throws Exception {
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        IProductCmpt productCmpt = mock(IProductCmpt.class);
        when(ipsSrcFile.getIpsObject()).thenReturn(productCmpt);
        ProductCmptViewItem viewItem = new ProductCmptViewItem(ipsSrcFile);
        IDescription description = mock(IDescription.class);
        when(description.getText()).thenReturn(TEST_DESCRIPTION);
        when(productCmpt.getDescription(any(Locale.class))).thenReturn(description);

        assertThat(labelProvider.getToolTipText(viewItem), is(TEST_DESCRIPTION));
    }

    @Test
    public void testGetToolTipText_viewItem_noProductCmpt() throws Exception {
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        ProductCmptViewItem viewItem = new ProductCmptViewItem(ipsSrcFile);

        assertThat(labelProvider.getToolTipText(viewItem), is(nullValue()));
    }

}