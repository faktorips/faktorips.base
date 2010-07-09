/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.runtime.productprovider;

import junit.framework.TestCase;

public class ClientRuntimeRepositoryTest extends TestCase {

    private TestProductDataProvider productDataProvider;
    private ProductDataProviderRuntimeRepository repository;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        super.setUp();
        Builder builder = new Builder(getClass().getClassLoader(),
                "org/faktorips/runtime/testrepository/faktorips-repository-toc.xml");
        repository = new ProductDataProviderRuntimeRepository("testRR", getClass().getClassLoader(), builder, null);
        productDataProvider = builder.testProductDataProvider;
    }

    public void testClientCall() {
        ClientRuntimeRepository client1 = new ClientRuntimeRepository(repository);
        ClientRuntimeRepository client2 = new ClientRuntimeRepository(repository);
        ClientRuntimeRepository client3 = new ClientRuntimeRepository(repository);

        assertNotNull(client1.getProductComponent("motor.MotorBasic"));

        assertNotNull(client2.getProductComponent("motor.MotorPlus"));

        assertNotNull(client3.getProductComponent("home.HomeBasic"));

        productDataProvider.modStamp = "1";

        client1.checkForModifications();
        //
        // try {
        // client1.getProductComponent("motor.MotorBasic");
        // fail("Should throw a runtime exception");
        // } catch (RuntimeException e) {
        // assertTrue(e.getCause() instanceof DataModifiedException);
        // DataModifiedException dme = (DataModifiedException)e.getCause();
        // assertEquals("0", dme.oldVersion);
        // assertEquals("1", dme.newVersion);
        // }
        //
        // // try again - error should not change!
        // try {
        // client1.getProductComponent("motor.MotorPlus");
        // fail("Should throw a runtime exception");
        //
        // } catch (RuntimeException e) {
        // assertTrue(e.getCause() instanceof DataModifiedException);
        // DataModifiedException dme = (DataModifiedException)e.getCause();
        // assertEquals("0", dme.oldVersion);
        // assertEquals("1", dme.newVersion);
        // }
        //
        // // error should also be thrown for other clients
        // try {
        // client2.getProductComponent("motor.MotorBasic");
        // fail("Should throw a runtime exception");
        //
        // } catch (RuntimeException e) {
        // assertTrue(e.getCause() instanceof DataModifiedException);
        // DataModifiedException dme = (DataModifiedException)e.getCause();
        // assertEquals("0", dme.oldVersion);
        // assertEquals("1", dme.newVersion);
        // }
        //
        // client1.reload();
        // // no exception anymore for client1
        // assertNotNull(client1.getProductComponent("motor.MotorBasic"));
        //
        // // but still exception for client2
        // try {
        // client2.getProductComponent("motor.MotorBasic");
        // fail("Should throw a runtime exception");
        //
        // } catch (RuntimeException e) {
        // assertTrue(e.getCause() instanceof DataModifiedException);
        // DataModifiedException dme = (DataModifiedException)e.getCause();
        // assertEquals("0", dme.oldVersion);
        // assertEquals("1", dme.newVersion);
        // }
        //
        // // and still exception for client3
        // try {
        // client3.getProductComponent("motor.MotorBasic");
        // fail("Should throw a runtime exception");
        //
        // } catch (RuntimeException e) {
        // assertTrue(e.getCause() instanceof DataModifiedException);
        // DataModifiedException dme = (DataModifiedException)e.getCause();
        // assertEquals("0", dme.oldVersion);
        // assertEquals("1", dme.newVersion);
        // }
        //
        // client2.reload();
        // // no exception anymore for client1 and client2
        // assertNotNull(client1.getProductComponent("motor.MotorPlus"));
        // assertNotNull(client2.getProductComponent("motor.MotorBasic"));
        //
        // // but still exception for client3
        // try {
        // client3.getProductComponent("motor.MotorBasic");
        // fail("Should throw a runtime exception");
        //
        // } catch (RuntimeException e) {
        // assertTrue(e.getCause() instanceof DataModifiedException);
        // DataModifiedException dme = (DataModifiedException)e.getCause();
        // assertEquals("0", dme.oldVersion);
        // assertEquals("1", dme.newVersion);
        // }
        //
        // client3.reload();
        // assertNotNull(client1.getProductComponent("motor.MotorPlus"));
        // assertNotNull(client2.getProductComponent("home.HomeBasic"));
        // assertNotNull(client3.getProductComponent("motor.MotorBasic"));

    }

    private static class TestProductDataProvider extends ClassLoaderProductDataProvider {

        String modStamp = "0";

        protected TestProductDataProvider(Builder builder) {
            super(builder);
            setCheckTocModifications(true);
        }

        @Override
        public String getProductDataVersion() {
            return modStamp;
        }

    }

    public static class Builder extends ClassLoaderProductDataProvider.Builder {

        private TestProductDataProvider testProductDataProvider;

        public Builder(ClassLoader cl, String tocResourcePath) {
            super(cl, tocResourcePath);
        }

        @Override
        public IProductDataProvider build() {
            testProductDataProvider = new TestProductDataProvider(this);
            return testProductDataProvider;
        }

    }

}
