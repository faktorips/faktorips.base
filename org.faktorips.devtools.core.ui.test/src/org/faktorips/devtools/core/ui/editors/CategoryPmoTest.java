/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
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

    private List<IProductCmptCategory> categories;

    @Before
    public void setUp() throws CoreException {
        MockitoAnnotations.initMocks(this);
        categories = Arrays.asList(category1, category2, category3);

        when(category1.getName()).thenReturn("category1");
        when(category2.getName()).thenReturn("category2");
        when(category3.getName()).thenReturn("category3");

        when(property.findProductCmptType(any(IIpsProject.class))).thenReturn(productCmptType);
        when(productCmptType.findCategories(any(IIpsProject.class))).thenReturn(categories);
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
