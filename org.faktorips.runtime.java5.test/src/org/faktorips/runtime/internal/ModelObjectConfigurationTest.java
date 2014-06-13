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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Calendar;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IRuntimeRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.Element;

/**
 * TODO Test Klasse in Runtime verschieben, {@link ConfVertrag} löschen
 */
@RunWith(MockitoJUnitRunner.class)
public class ModelObjectConfigurationTest {

    @Mock
    private IProductComponent productCmpt;
    @Mock
    private IProductComponentGeneration productCmptGeneration;
    @Mock
    private IRuntimeRepository repository;
    @Mock
    private Element element;

    @Spy
    private final IConfigurableModelObject configurableMO = new ConfVertrag(new ModelObjectConfiguration());

    private Calendar calendar;

    @Before
    public void createAbstractConfigurableModelObject() throws Exception {
        configurableMO.setProductComponent(productCmpt);
        configurableMO.setProductCmptGeneration(productCmptGeneration);

        doReturn(calendar).when(configurableMO).getEffectiveFromAsCalendar();
        calendar = mock(Calendar.class);
        doReturn(calendar).when(configurableMO).getEffectiveFromAsCalendar();
        when(productCmpt.getGenerationBase(calendar)).thenReturn(productCmptGeneration);

        when(element.getAttribute("product-component.id")).thenReturn("PC-ID");
        when(repository.getProductComponent("PC-ID")).thenReturn(productCmpt);

        assertNotNull(configurableMO.getProductCmptGeneration());
    }

    @Test
    public void testEffectiveFromHasChanged_dontResetWhenDateIsNull() throws Exception {
        doReturn(null).when(configurableMO).getEffectiveFromAsCalendar();

        configurableMO.effectiveFromHasChanged();

        assertNotNull(configurableMO.getProductCmptGeneration());
    }

    @Test
    public void testEffectiveFromHasChanged() throws Exception {
        configurableMO.effectiveFromHasChanged();

        assertNull(configurableMO.getProductCmptGeneration());
    }

    @Test
    public void testGetProductComponentGeneration() {
        assertNotNull(configurableMO.getProductCmptGeneration());
    }

    @Test
    public void testGetProductComponentGeneration_returnNullIfProduCmptIsNull() {
        configurableMO.setProductComponent(null);

        assertNull(configurableMO.getProductCmptGeneration());
    }

    // @Test
    // public void testInitFromXML() {
    // configurableMO.setProductComponent(null);
    // assertNull(configurableMO.getProductComponent());
    //
    // configurableMO.initFromXML(element, true, repository, null, null, null);
    //
    // assertEquals(productCmpt, configurableMO.getProductComponent());
    // }
}
