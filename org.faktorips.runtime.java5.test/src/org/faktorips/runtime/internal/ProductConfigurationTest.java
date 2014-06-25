/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.util.Calendar;

import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IRuntimeRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.Element;

@RunWith(MockitoJUnitRunner.class)
public class ProductConfigurationTest {

    @Mock
    private IProductComponent productCmpt;
    @Mock
    private IProductComponentGeneration productCmptGeneration;
    @Mock
    private IRuntimeRepository repository;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Element element;
    @Mock
    private Calendar calendar;

    @Before
    public void createAbstractConfigurableModelObject() throws Exception {
        when(productCmpt.getGenerationBase(calendar)).thenReturn(productCmptGeneration);
        when(productCmptGeneration.getProductComponent()).thenReturn(productCmpt);

        when(element.getAttribute("productCmpt")).thenReturn("PC-ID");
        when(repository.getExistingProductComponent("PC-ID")).thenReturn(productCmpt);
    }

    @Test
    public void testConstructor() throws Exception {
        ProductConfiguration productConfiguration = new ProductConfiguration();
        assertNull(productConfiguration.getProductComponent());
        assertNull(productConfiguration.getProductCmptGeneration(calendar));
    }

    @Test
    public void testConstructor_withProductCmpt() throws Exception {
        ProductConfiguration productConfiguration = new ProductConfiguration(productCmpt);
        assertEquals(productCmpt, productConfiguration.getProductComponent());
        assertEquals(productCmptGeneration, productConfiguration.getProductCmptGeneration(calendar));
    }

    @Test
    public void testGetProductCmptGeneration_nullCalendar() throws Exception {
        ProductConfiguration productConfiguration = new ProductConfiguration(productCmpt);
        assertEquals(productCmpt, productConfiguration.getProductComponent());
        assertNull(productConfiguration.getProductCmptGeneration(null));
    }

    @Test
    public void testSetProductComponent() {
        ProductConfiguration productConfiguration = new ProductConfiguration();
        assertNull(productConfiguration.getProductComponent());
        assertNull(productConfiguration.getProductCmptGeneration(calendar));

        productConfiguration.setProductComponent(productCmpt);

        assertEquals(productCmpt, productConfiguration.getProductComponent());
        assertEquals(productCmptGeneration, productConfiguration.getProductCmptGeneration(calendar));
    }

    @Test
    public void testGetProductComponent() {
        ProductConfiguration productConfiguration = new ProductConfiguration(productCmpt);
        assertEquals(productCmpt, productConfiguration.getProductComponent());
        assertEquals(productCmptGeneration, productConfiguration.getProductCmptGeneration(calendar));

        productConfiguration.setProductComponent(null);

        assertNull(productConfiguration.getProductComponent());
        assertNull(productConfiguration.getProductCmptGeneration(calendar));
    }

    @Test
    public void testCopy() {
        ProductConfiguration productConfiguration = new ProductConfiguration(productCmpt);

        ProductConfiguration copy = new ProductConfiguration();
        copy.copy(productConfiguration);

        assertEquals(productCmpt, productConfiguration.getProductComponent());
        assertEquals(productCmptGeneration, productConfiguration.getProductCmptGeneration(calendar));
    }

    @Test
    public void testInitFromXML() {
        ProductConfiguration productConfiguration = new ProductConfiguration();
        assertNull(productConfiguration.getProductComponent());

        productConfiguration.initFromXml(element, repository);

        assertEquals(productCmpt, productConfiguration.getProductComponent());
    }

    @Test
    public void testSetProductCmptGeneration() {
        ProductConfiguration productConfiguration = new ProductConfiguration();
        assertNull(productConfiguration.getProductComponent());
        assertNull(productConfiguration.getProductCmptGeneration(calendar));

        productConfiguration.setProductCmptGeneration(productCmptGeneration);

        assertEquals(productCmpt, productConfiguration.getProductComponent());
        assertEquals(productCmptGeneration, productConfiguration.getProductCmptGeneration(null));
    }

    @Test
    public void testSetProductCmptGeneration2() {
        ProductConfiguration productConfiguration = new ProductConfiguration(productCmpt);
        assertEquals(productCmpt, productConfiguration.getProductComponent());
        assertEquals(productCmptGeneration, productConfiguration.getProductCmptGeneration(calendar));
        when(productCmptGeneration.getProductComponent()).thenReturn(null);

        productConfiguration.setProductCmptGeneration(productCmptGeneration);

        assertNull(productConfiguration.getProductComponent());
        assertNull(productConfiguration.getProductCmptGeneration(null));
    }

    @Test
    public void testResetProductCmptGeneration() {
        ProductConfiguration productConfiguration = new ProductConfiguration(productCmpt);
        assertNotNull(productConfiguration.getProductCmptGeneration(calendar));

        productConfiguration.resetProductCmptGeneration();
        assertNotNull(productConfiguration.getProductCmptGeneration(calendar));
    }
}
