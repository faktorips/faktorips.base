/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.SingletonMockHelper;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CategoryPmoTest {

    @Mock
    private IProductCmptType productCmptType;

    @Mock
    private IProductCmptProperty property;

    @Mock
    private IProductCmptCategory category1;

    @Mock
    private IProductCmptCategory category2;

    @Mock
    private IProductCmptCategory category3;

    @Mock
    private IIpsModel ipsModel;

    private List<IProductCmptCategory> categories;

    @Before
    public void setUp() throws CoreException {
        MockitoAnnotations.initMocks(this);
        categories = Arrays.asList(category1, category2, category3);

        when(category1.getName()).thenReturn("category1");
        when(category2.getName()).thenReturn("category2");
        when(category3.getName()).thenReturn("category3");

        when(property.findProductCmptType(any(IIpsProject.class))).thenReturn(productCmptType);
        when(property.getIpsModel()).thenReturn(ipsModel);
        when(productCmptType.findCategories(any(IIpsProject.class))).thenReturn(categories);
    }

    @Test
    public void testListenerAdded() {
        IpsPlugin ipsPlugin = IpsPlugin.getDefault();
        ipsPlugin = spy(ipsPlugin);
        SingletonMockHelper singletonMockHelper = new SingletonMockHelper();
        try {
            singletonMockHelper.setSingletonInstance(IpsPlugin.class, ipsPlugin);
            doReturn(ipsModel).when(ipsPlugin).getIpsModel();

            ContentsChangeListener categoryPmo = new CategoryPmo(property);

            verify(ipsModel).addChangeListener(categoryPmo);

        } finally {
            singletonMockHelper.reset();
        }
    }

    @Test
    public void testGetCategories() {
        assertEquals(categories, new CategoryPmo(property).getCategories());
    }

    @Test
    public void testGetCategories_ProductCmptTypeNotFound() throws CoreException {
        when(property.findProductCmptType(any(IIpsProject.class))).thenReturn(null);

        assertTrue(new CategoryPmo(property).getCategories().isEmpty());
    }

    @Test
    public void testGetCategories_ReturnDefensiveCopy() {
        CategoryPmo pmo = new CategoryPmo(property);

        List<IProductCmptCategory> categories = pmo.getCategories();
        categories.remove(0);

        assertEquals(3, pmo.getCategories().size());
    }

    @Test
    public void testGetCategory() {
        when(property.getCategory()).thenReturn("category2");

        assertEquals(category2, new CategoryPmo(property).getCategory());
    }

    @Test
    public void testGetCategory_ReturnCorrespondingDefaultCategoryIfNoCategorySpecified() {
        when(property.getCategory()).thenReturn("");
        when(category2.isDefaultFor(property)).thenReturn(true);

        assertEquals(category2, new CategoryPmo(property).getCategory());
    }

    @Test
    public void testGetCategory_ReturnFirstCorrespondingDefaultCategoryIfMultipleAreFound() {
        when(property.getCategory()).thenReturn("");
        when(category1.isDefaultFor(property)).thenReturn(true);
        when(category2.isDefaultFor(property)).thenReturn(true);
        when(category3.isDefaultFor(property)).thenReturn(true);

        assertEquals(category1, new CategoryPmo(property).getCategory());
    }

    @Test
    public void testSetCategory() {
        new CategoryPmo(property).setCategory(category1);

        verify(property).setCategory("category1");
    }

}
