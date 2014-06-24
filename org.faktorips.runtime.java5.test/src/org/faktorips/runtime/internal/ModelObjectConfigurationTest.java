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
public class ModelObjectConfigurationTest {

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
        ModelObjectConfiguration modelObjectConfiguration = new ModelObjectConfiguration();
        assertNull(modelObjectConfiguration.getProductComponent());
        assertNull(modelObjectConfiguration.getProductCmptGeneration(calendar));
    }

    @Test
    public void testConstructor_withProductCmpt() throws Exception {
        ModelObjectConfiguration modelObjectConfiguration = new ModelObjectConfiguration(productCmpt);
        assertEquals(productCmpt, modelObjectConfiguration.getProductComponent());
        assertEquals(productCmptGeneration, modelObjectConfiguration.getProductCmptGeneration(calendar));
    }

    @Test
    public void testSetProductComponent() {
        ModelObjectConfiguration modelObjectConfiguration = new ModelObjectConfiguration();
        assertNull(modelObjectConfiguration.getProductComponent());
        assertNull(modelObjectConfiguration.getProductCmptGeneration(calendar));

        modelObjectConfiguration.setProductComponent(productCmpt);

        assertEquals(productCmpt, modelObjectConfiguration.getProductComponent());
        assertEquals(productCmptGeneration, modelObjectConfiguration.getProductCmptGeneration(calendar));
    }

    @Test
    public void testGetProductComponent() {
        ModelObjectConfiguration modelObjectConfiguration = new ModelObjectConfiguration(productCmpt);
        assertEquals(productCmpt, modelObjectConfiguration.getProductComponent());
        assertEquals(productCmptGeneration, modelObjectConfiguration.getProductCmptGeneration(calendar));

        modelObjectConfiguration.setProductComponent(null);

        assertNull(modelObjectConfiguration.getProductComponent());
        assertNull(modelObjectConfiguration.getProductCmptGeneration(calendar));
    }

    @Test
    public void testCopy() {
        ModelObjectConfiguration modelObjectConfiguration = new ModelObjectConfiguration(productCmpt);

        ModelObjectConfiguration copy = new ModelObjectConfiguration();
        copy.copy(modelObjectConfiguration);

        assertEquals(productCmpt, modelObjectConfiguration.getProductComponent());
        assertEquals(productCmptGeneration, modelObjectConfiguration.getProductCmptGeneration(calendar));
    }

    @Test
    public void testInitFromXML() {
        ModelObjectConfiguration modelObjectConfiguration = new ModelObjectConfiguration();
        assertNull(modelObjectConfiguration.getProductComponent());

        modelObjectConfiguration.initFromXml(element, repository);

        assertEquals(productCmpt, modelObjectConfiguration.getProductComponent());
    }

    @Test
    public void testSetProductCmptGeneration() {
        ModelObjectConfiguration modelObjectConfiguration = new ModelObjectConfiguration();
        assertNull(modelObjectConfiguration.getProductComponent());
        assertNull(modelObjectConfiguration.getProductCmptGeneration(calendar));

        modelObjectConfiguration.setProductCmptGeneration(productCmptGeneration);

        assertEquals(productCmpt, modelObjectConfiguration.getProductComponent());
        assertEquals(productCmptGeneration, modelObjectConfiguration.getProductCmptGeneration(calendar));
    }

    @Test
    public void testSetProductCmptGeneration2() {
        ModelObjectConfiguration modelObjectConfiguration = new ModelObjectConfiguration(productCmpt);
        assertEquals(productCmpt, modelObjectConfiguration.getProductComponent());
        assertEquals(productCmptGeneration, modelObjectConfiguration.getProductCmptGeneration(calendar));
        when(productCmptGeneration.getProductComponent()).thenReturn(null);

        modelObjectConfiguration.setProductCmptGeneration(productCmptGeneration);

        assertNull(modelObjectConfiguration.getProductComponent());
        assertNull(modelObjectConfiguration.getProductCmptGeneration(calendar));
    }

}
