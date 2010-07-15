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

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;

import junit.framework.TestCase;

import org.faktorips.runtime.IRuntimeRepository;

public class DetachedContentRuntimeRepositoryTest extends TestCase {

    private DetachedContentRuntimeRepositoryManager repository;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setTocVersion(0);
        MyBuilder builder = new MyBuilder(getClass().getClassLoader(),
                "org/faktorips/runtime/testrepository/faktorips-repository-toc.xml");
        builder.setCheckTocModifications(true);
        repository = new DetachedContentRuntimeRepositoryManager("testRR", getClass().getClassLoader(), builder, null);
    }

    private void setTocVersion(long version) {
        URL tocUrl = getClass().getClassLoader().getResource(
                "org/faktorips/runtime/testrepository/faktorips-repository-toc.xml");
        try {
            URLConnection connection = tocUrl.openConnection();
            if (connection instanceof JarURLConnection) {
                JarURLConnection jarUrlConnection = (JarURLConnection)connection;
                URL jarUrl = jarUrlConnection.getJarFileURL();
                File jarFile = new File(jarUrl.toURI());
                jarFile.setLastModified(version);
            } else {
                File tocFile = new File(tocUrl.getFile());
                tocFile.setLastModified(version);
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot get last modification stamp of toc url", e);
        }
    }

    public void testClientCall() {
        IRuntimeRepository client1 = repository.startRequest();
        IRuntimeRepository client2 = repository.startRequest();
        IRuntimeRepository client3 = repository.startRequest();

        assertNotNull(client1.getProductComponent("motor.MotorBasic"));

        assertNotNull(client2.getProductComponent("motor.MotorPlus"));

        assertNotNull(client3.getProductComponent("home.HomeBasic"));

        setTocVersion(1000);

        // should NOT throw an exception because product component is in cache and
        // we did not call checkForModifications()
        client1.getProductComponent("motor.MotorBasic");

        client1 = repository.startRequest();
        // shold NOT throw an exception because we just called reloadIfModified.
        client1.getProductComponent("motor.MotorPlus");

        setTocVersion(2000);

        try {
            // should throw an exception because version changed and requested product component is
            // not in cache
            client1.getProductComponent("motor.MotorBasic");
            fail("Should throw a runtime exception");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof DataModifiedException);
            DataModifiedException dme = (DataModifiedException)e.getCause();
            assertEquals("1000", dme.oldVersion);
            assertEquals("2000", dme.newVersion);
        }

        // MotorPlus is cached --> no exception until checkForModifications()
        client1.getProductComponent("motor.MotorPlus");

        // try again MotorBasic - did not call checkForModifications --> still same exception
        try {
            client1.getProductComponent("motor.MotorBasic");
            fail("Should throw a runtime exception");

        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof DataModifiedException);
            DataModifiedException dme = (DataModifiedException)e.getCause();
            assertEquals("1000", dme.oldVersion);
            assertEquals("2000", dme.newVersion);
        }

        // exception should also be thrown for other clients
        try {
            client2.getProductComponent("motor.MotorBasic");
            fail("Should throw a runtime exception");

        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof DataModifiedException);
            DataModifiedException dme = (DataModifiedException)e.getCause();
            assertEquals("1000", dme.oldVersion);
            assertEquals("2000", dme.newVersion);
        }

        client1 = repository.startRequest();
        // no exception anymore for client1
        assertNotNull(client1.getProductComponent("motor.MotorBasic"));

        // but still exception for client2
        try {
            client2.getProductComponent("motor.MotorBasic");
            fail("Should throw a runtime exception");

        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof DataModifiedException);
            DataModifiedException dme = (DataModifiedException)e.getCause();
            // client2 was still on version 0 - never called checkForModifications
            assertEquals("0", dme.oldVersion);
            assertEquals("2000", dme.newVersion);
        }

        // and still exception for client3
        try {
            client3.getProductComponent("motor.MotorBasic");
            fail("Should throw a runtime exception");

        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof DataModifiedException);
            DataModifiedException dme = (DataModifiedException)e.getCause();
            // client2 was still on version 0 - never called checkForModifications
            assertEquals("0", dme.oldVersion);
            assertEquals("2000", dme.newVersion);
        }

        client2 = repository.startRequest();
        // no exception anymore for client1 and client2
        assertNotNull(client1.getProductComponent("motor.MotorPlus"));
        assertNotNull(client2.getProductComponent("motor.MotorBasic"));

        // but still exception for client3
        try {
            client3.getProductComponent("motor.MotorBasic");
            fail("Should throw a runtime exception");

        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof DataModifiedException);
            DataModifiedException dme = (DataModifiedException)e.getCause();
            assertEquals("0", dme.oldVersion);
            assertEquals("2000", dme.newVersion);
        }

        client3 = repository.startRequest();
        assertNotNull(client1.getProductComponent("motor.MotorPlus"));
        assertNotNull(client2.getProductComponent("home.HomeBasic"));
        assertNotNull(client3.getProductComponent("motor.MotorBasic"));
    }

    public class MyBuilder extends ClassLoaderProductDataProvider.Builder {

        private IProductDataProvider provider;

        public MyBuilder(ClassLoader cl, String tocResourcePath) {
            super(cl, tocResourcePath);
        }

        @Override
        public IProductDataProvider build() {
            provider = super.build();
            return provider;
        }

    }

}
