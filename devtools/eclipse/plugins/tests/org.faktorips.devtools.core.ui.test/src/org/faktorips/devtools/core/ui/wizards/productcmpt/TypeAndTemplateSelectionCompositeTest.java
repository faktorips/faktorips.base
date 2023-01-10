/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.ui.util.DescriptionFinder;
import org.faktorips.devtools.core.ui.wizards.productcmpt.TypeAndTemplateSelectionComposite.LabelProvider;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptType;
import org.faktorips.devtools.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.junit.Test;

public class TypeAndTemplateSelectionCompositeTest extends AbstractIpsPluginTest {

    private static final String TEST_DESCRIPTION = ("This is a test text");
    private static final String OVERRIDE_DESCRIPTION = ("This is an override text");
    private LabelProvider labelProvider = new TypeAndTemplateSelectionComposite.LabelProvider();

    @Test
    public void testGetToolTipText_null() throws Exception {
        assertThat(labelProvider.getToolTipText(null), is(nullValue()));
    }

    @Test
    public void testGetToolTipText_describedElement() throws Exception {
        IDescribedElement describedElement = mock(IDescribedElement.class);
        when(describedElement.getDescriptionText(any(Locale.class))).thenReturn(TEST_DESCRIPTION);

        assertThat(labelProvider.getToolTipText(describedElement), is(TEST_DESCRIPTION));
    }

    @Test
    public void testGetToolTipText_viewItem() throws Exception {
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        IProductCmpt productCmpt = mock(IProductCmpt.class);
        when(ipsSrcFile.getIpsObject()).thenReturn(productCmpt);
        ProductCmptViewItem viewItem = new ProductCmptViewItem(ipsSrcFile);
        when(productCmpt.getDescriptionText(any(Locale.class))).thenReturn(TEST_DESCRIPTION);

        assertThat(labelProvider.getToolTipText(viewItem), is(TEST_DESCRIPTION));
    }

    @Test
    public void testGetToolTipText_viewItem_noProductCmpt() throws Exception {
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        ProductCmptViewItem viewItem = new ProductCmptViewItem(ipsSrcFile);

        assertThat(labelProvider.getToolTipText(viewItem), is(nullValue()));
    }

    @Test
    public void testGetLocalizedDescription_DescriptionFinder() {
        IIpsProject project = newIpsProject();
        IProductCmpt productCmpt = mock(IProductCmpt.class);
        DescriptionFinder df = new DescriptionFinder(project);

        when(productCmpt.getDescriptionText(any(Locale.class))).thenReturn(TEST_DESCRIPTION);

        df.start(productCmpt);

        assertThat(df.getLocalizedDescription(), is(TEST_DESCRIPTION));
    }

    @Test
    public void testGetLocalizedDescription_DescriptionFinder_inherited() {
        IIpsProject project = newIpsProject();
        ProductCmptType productCmptParent = newProductCmptType(project, "ParentPdct");
        ProductCmptType productCmptChild = newProductCmptType(productCmptParent, "ChildPdct");

        productCmptParent.setDescriptionText(Locale.getDefault(), TEST_DESCRIPTION);

        DescriptionFinder df = new DescriptionFinder(project);

        df.start(productCmptParent);
        assertThat(df.getLocalizedDescription(), is(TEST_DESCRIPTION));
        df.start(productCmptChild);
        assertThat(df.getLocalizedDescription(), is(TEST_DESCRIPTION));
        productCmptChild.setDescriptionText(Locale.getDefault(), OVERRIDE_DESCRIPTION);
        df.start(productCmptChild);
        assertThat(df.getLocalizedDescription(), is(OVERRIDE_DESCRIPTION));
    }

}
