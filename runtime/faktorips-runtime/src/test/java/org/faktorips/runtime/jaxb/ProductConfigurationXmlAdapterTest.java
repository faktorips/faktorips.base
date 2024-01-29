/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.ProductConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@SuppressWarnings("deprecation")
@RunWith(MockitoJUnitRunner.class)
public class ProductConfigurationXmlAdapterTest {
    @Mock
    IProductComponent productCmpt;
    @Mock
    IRuntimeRepository repository;
    private ProductConfigurationXmlAdapter xmlAdapter;

    @Before
    public void setUp() {
        when(productCmpt.getId()).thenReturn("someId");
        when(repository.getProductComponent("someId")).thenReturn(productCmpt);
        xmlAdapter = new ProductConfigurationXmlAdapter(repository);
    }

    @Test
    public void testMarshal_Null() throws Exception {
        assertNull(xmlAdapter.marshal(null));
        assertNull(xmlAdapter.marshal(new ProductConfiguration()));
    }

    @Test
    public void testMarshal() throws Exception {
        ProductConfiguration config = new ProductConfiguration(productCmpt);

        String marshalledString = xmlAdapter.marshal(config);

        assertEquals("someId", marshalledString);
    }

    @Test
    public void testUnmarshal() throws Exception {
        ProductConfiguration unmarshalledConfig = xmlAdapter.unmarshal("someId");

        assertEquals(productCmpt, unmarshalledConfig.getProductComponent());
    }

    @Test
    public void testUnmarshal_neverReturnNullConfig() throws Exception {
        ProductConfiguration unmarshalledConfig = xmlAdapter.unmarshal("xxx");

        assertNotNull(unmarshalledConfig);
        assertNull(unmarshalledConfig.getProductComponent());

        unmarshalledConfig = xmlAdapter.unmarshal(null);

        assertNotNull(unmarshalledConfig);
        assertNull(unmarshalledConfig.getProductComponent());
    }

}
