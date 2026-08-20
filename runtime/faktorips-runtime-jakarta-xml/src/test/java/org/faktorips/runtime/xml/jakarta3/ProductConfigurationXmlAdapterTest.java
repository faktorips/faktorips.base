/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.xml.jakarta3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.lenient;

import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.ProductConfiguration;
import org.faktorips.runtime.xml.jakarta.ProductConfigurationXmlAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;

public class ProductConfigurationXmlAdapterTest {

    @Mock
    IProductComponent productCmpt;
    @Mock
    IRuntimeRepository repository;

    private MockitoSession mockito;
    private ProductConfigurationXmlAdapter xmlAdapter;

    @BeforeEach
    public void setUp() {
        mockito = Mockito.mockitoSession()
                .initMocks(this)
                .strictness(Strictness.STRICT_STUBS)
                .startMocking();
        lenient().when(productCmpt.getId()).thenReturn("someId");
        lenient().when(repository.getProductComponent("someId")).thenReturn(productCmpt);
        xmlAdapter = new ProductConfigurationXmlAdapter(repository);
    }

    @AfterEach
    void tearDown() {
        mockito.finishMocking();
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
