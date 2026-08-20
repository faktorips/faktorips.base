/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import static org.faktorips.runtime.testutil.MockUtil.createMocks;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;

import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IRuntimeRepositoryLookup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoSession;
import org.w3c.dom.Element;

public class ProductConfigurationTest {


    private static final String PODUCT_COMPONENT_ID = "My_PC-ID";

    /**
     * Needs to be static to have access from the {@link TestRuntimeRepositoryLookup}
     */
    @Mock
    private static IRuntimeRepository repository;

    @Mock
    private IProductComponent productCmpt;

    @Mock
    private IProductComponentGeneration productCmptGeneration;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Element element;

    private MockitoSession mockito;

    private final Calendar calendar = Calendar.getInstance(ProductConfiguration.TIME_ZONE);

    @BeforeEach
    public void createAbstractConfigurableModelObject() throws Exception {
        mockito = createMocks(this);
        lenient().when(productCmpt.getId()).thenReturn(PODUCT_COMPONENT_ID);
        lenient().when(productCmpt.getRepository()).thenReturn(repository);
        lenient().when(productCmpt.getGenerationBase(calendar)).thenReturn(productCmptGeneration);
        lenient().when(productCmptGeneration.getProductComponent()).thenReturn(productCmpt);
        lenient().when(productCmptGeneration.getValidFrom(ProductConfiguration.TIME_ZONE)).thenReturn(calendar.getTime());

        lenient().when(element.getAttribute("productCmpt")).thenReturn(PODUCT_COMPONENT_ID);
        lenient().when(repository.getExistingProductComponent(PODUCT_COMPONENT_ID)).thenReturn(productCmpt);

        lenient().when(repository.getProductComponent(PODUCT_COMPONENT_ID)).thenReturn(productCmpt);
        lenient().when(repository.getProductComponentGeneration(PODUCT_COMPONENT_ID, calendar)).thenReturn(productCmptGeneration);
    }

    @AfterEach
    void tearDown() {
        mockito.finishMocking();
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

    @Test
    public void testSerialization_noLookup() throws Exception {
        assertThrows(IllegalStateException.class, () -> {
            ProductConfiguration productConfiguration = initForSerialization();

            serializeProductConfiguration(productConfiguration);
        });
    }

    @Test
    public void testSerialization() throws Exception {
        when(repository.getRuntimeRepositoryLookup()).thenReturn(new TestRuntimeRepositoryLookup());
        ProductConfiguration productConfiguration = initForSerialization();

        byte[] serialize = serializeProductConfiguration(productConfiguration);
        ProductConfiguration deserializedProductConfiguration = deserializeProductConfiguration(serialize);

        assertEquals(productConfiguration.getProductComponent(),
                deserializedProductConfiguration.getProductComponent());
        assertEquals(productConfiguration.getProductCmptGeneration(null),
                deserializedProductConfiguration.getProductCmptGeneration(null));
    }

    @Test
    public void testSerialization_noGeneration() throws Exception {
        when(repository.getRuntimeRepositoryLookup()).thenReturn(new TestRuntimeRepositoryLookup());
        ProductConfiguration productConfiguration = new ProductConfiguration();
        productConfiguration.setProductComponent(productCmpt);

        byte[] serialize = serializeProductConfiguration(productConfiguration);
        ProductConfiguration deserializedProductConfiguration = deserializeProductConfiguration(serialize);

        assertEquals(productConfiguration.getProductComponent(),
                deserializedProductConfiguration.getProductComponent());
        assertNull(deserializedProductConfiguration.getProductCmptGeneration(null));
    }

    @Test
    public void testSerialization_noProductCmpt() throws Exception {
        ProductConfiguration productConfiguration = new ProductConfiguration();

        byte[] serialize = serializeProductConfiguration(productConfiguration);
        ProductConfiguration deserializedProductConfiguration = deserializeProductConfiguration(serialize);

        assertNull(deserializedProductConfiguration.getProductComponent());
        assertNull(deserializedProductConfiguration.getProductCmptGeneration(null));
    }

    private ProductConfiguration initForSerialization() {
        ProductConfiguration productConfiguration = new ProductConfiguration();
        productConfiguration.setProductCmptGeneration(productCmptGeneration);
        return productConfiguration;
    }

    private byte[] serializeProductConfiguration(ProductConfiguration productConfiguration) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(512);

        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(productConfiguration);

        return baos.toByteArray();
    }

    private ProductConfiguration deserializeProductConfiguration(byte[] byteArray) throws IOException,
            ClassNotFoundException {
        try (ObjectInputStream osgiCompatibleInputStream = new ObjectInputStream(new ByteArrayInputStream(byteArray))) {
            return (ProductConfiguration)osgiCompatibleInputStream
                    .readObject();
        }

    }

    private static class TestRuntimeRepositoryLookup implements IRuntimeRepositoryLookup {

        private static final long serialVersionUID = 1L;

        @Override
        public IRuntimeRepository getRuntimeRepository() {
            return repository;
        }

    }

}
