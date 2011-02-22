/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime.productdataprovider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IRuntimeRepositoryManager;
import org.faktorips.runtime.internal.DateTime;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.internal.toc.IReadonlyTableOfContents;
import org.faktorips.runtime.internal.toc.ProductCmptTocEntry;
import org.faktorips.runtime.testrepository.motor.MotorProduct;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class DetachedContentRuntimeRepositoryTest {

	private IRuntimeRepositoryManager repositoryManager;
	private IProductDataProvider pdpMock;
	private IReadonlyTableOfContents tocMock;

	@Before
	public void setUp() throws Exception {
		IProductDataProviderFactory pdpFactoryMock = Mockito
				.mock(IProductDataProviderFactory.class);
		pdpMock = Mockito.mock(IProductDataProvider.class);
		// when(pdpMock.getVersion()).thenReturn("0");
		when(pdpMock.isCompatibleToBaseVersion()).thenReturn(true);

		tocMock = Mockito.mock(IReadonlyTableOfContents.class);
		when(pdpMock.getToc()).thenReturn(tocMock);

		when(pdpFactoryMock.newInstance()).thenReturn(pdpMock);
		repositoryManager = new DetachedContentRuntimeRepositoryManager.Builder(
				pdpFactoryMock).build();
	}

	@Test
	public void testClientCall() {
		IRuntimeRepository client1 = repositoryManager
				.getActualRuntimeRepository();
		IRuntimeRepository client2 = repositoryManager
				.getActualRuntimeRepository();
		IRuntimeRepository client3 = repositoryManager
				.getActualRuntimeRepository();

		// every client repository should be the same (equal)
		assertEquals(client1, client2);
		assertEquals(client2, client3);

		ProductComponent pcMock1 = Mockito.mock(ProductComponent.class);
//		<ProductComponent ipsObjectId="motor.MotorPlus" ipsObjectQualifiedName="motor.MotorPlus" kindId="motor.MotorPlus" versionId="2005-01" xmlResource="org/faktorips/runtime/testrepository/motor/MotorPlus.ipsproduct" implementationClass="org.faktorips.runtime.testrepository.motor.MotorProduct" policyCmptClass="org.faktorips.runtime.testrepository.motor.MotorPolicy" validTo="2010-01-16">
		 ProductCmptTocEntry tocEntry = new  ProductCmptTocEntry("motor.MotorBasic", "motor.MotorBasic", "motor.MotorBasic", "2005-01", "org/faktorips/runtime/testrepository/motor/MotorPlus.ipsproduct", "org.faktorips.runtime.testrepository.motor.MotorProduct", "org.faktorips.runtime.testrepository.motor.MotorProductGen", new DateTime(2010, 1, 16));
		when(tocMock.getProductCmptTocEntry("motor.MotorBasic")).thenReturn(tocEntry);

		assertNotNull(client1.getProductComponent("motor.MotorBasic"));

		assertNotNull(client2.getProductComponent("motor.MotorPlus"));

		assertNotNull(client3.getProductComponent("motor.MotorBasic"));

		when(pdpMock.getVersion()).thenReturn("1000");

		// should NOT throw an exception because product component is in cache
		// and
		// we did not call checkForModifications()
		try {
			client1.getProductComponent("motor.MotorBasic");
		} catch (Exception e) {
			fail();
		}

		client1 = repositoryManager.getActualRuntimeRepository();
		assertNotSame(client1, client2);
		// still equals
		assertEquals(client2, client3);

		// shold NOT throw an exception because we just called startRequest.
		client1.getProductComponent("motor.MotorPlus");

		when(pdpMock.getVersion()).thenReturn("2000");

		try {
			// should throw an exception because version changed and requested
			// product component is
			// not in cache
			client1.getProductComponent("motor.MotorBasic");
			fail("Should throw a runtime exception");
		} catch (RuntimeException e) {
			assertTrue(e.getCause() instanceof DataModifiedException);
			DataModifiedException dme = (DataModifiedException) e.getCause();
			assertEquals("1000", dme.oldVersion);
			assertEquals("2000", dme.newVersion);
		}

		// MotorPlus is cached --> no exception until checkForModifications()
		client1.getProductComponent("motor.MotorPlus");

		// try again MotorBasic - did not call checkForModifications --> still
		// same exception
		try {
			client1.getProductComponent("motor.MotorBasic");
			fail("Should throw a runtime exception");

		} catch (RuntimeException e) {
			assertTrue(e.getCause() instanceof DataModifiedException);
			DataModifiedException dme = (DataModifiedException) e.getCause();
			assertEquals("1000", dme.oldVersion);
			assertEquals("2000", dme.newVersion);
		}

		// client2 sill works on old version. MotroBasic is still in cache
		assertNotNull(client2.getProductComponent("motor.MotorBasic"));
		// exception should also be thrown if data not cached
		try {
			client2.getProductComponent("home.HomeBasic");
			fail("Should throw a runtime exception");

		} catch (RuntimeException e) {
			assertTrue(e.getCause() instanceof DataModifiedException);
			DataModifiedException dme = (DataModifiedException) e.getCause();
			assertEquals("0", dme.oldVersion);
			assertEquals("2000", dme.newVersion);
		}

		client1 = repositoryManager.getActualRuntimeRepository();
		assertNotSame(client1, client2);
		// still equals
		assertEquals(client2, client3);

		// no exception anymore for client1
		assertNotNull(client1.getProductComponent("home.HomeBasic"));

		// MotorBasic still cached
		assertNotNull(client2.getProductComponent("motor.MotorBasic"));

		// but still exception for not cached content in client2
		try {
			client2.getProductComponent("home.HomeBasic");
			fail("Should throw a runtime exception");

		} catch (RuntimeException e) {
			assertTrue(e.getCause() instanceof DataModifiedException);
			DataModifiedException dme = (DataModifiedException) e.getCause();
			// client2 was still on version 0 - never called
			// checkForModifications
			assertEquals("0", dme.oldVersion);
			assertEquals("2000", dme.newVersion);
		}

		// MotorBasic also for client3 in cache
		assertNotNull(client3.getProductComponent("motor.MotorBasic"));
		// and still exception for client3 for not cached data
		try {
			client3.getProductComponent("home.HomeBasic");
			fail("Should throw a runtime exception");

		} catch (RuntimeException e) {
			assertTrue(e.getCause() instanceof DataModifiedException);
			DataModifiedException dme = (DataModifiedException) e.getCause();
			// client2 was still on version 0 - never called
			// checkForModifications
			assertEquals("0", dme.oldVersion);
			assertEquals("2000", dme.newVersion);
		}

		client2 = repositoryManager.getActualRuntimeRepository();
		// client2 now equals to client1
		assertEquals(client1, client2);
		assertNotSame(client2, client3);

		// no exception anymore for client1 and client2
		assertNotNull(client1.getProductComponent("home.HomeBasic"));
		assertNotNull(client2.getProductComponent("home.HomeBasic"));
		assertNotNull(client1.getTable("motor.RateTable"));
		assertNotNull(client2.getTable("motor.RateTable"));

		// MotorBasic also for client3 in cache
		assertNotNull(client3.getProductComponent("motor.MotorBasic"));
		// but still exception for client3
		try {
			client3.getProductComponent("home.HomeBasic");
			fail("Should throw a runtime exception");

		} catch (RuntimeException e) {
			assertTrue(e.getCause() instanceof DataModifiedException);
			DataModifiedException dme = (DataModifiedException) e.getCause();
			assertEquals("0", dme.oldVersion);
			assertEquals("2000", dme.newVersion);
		}

		client3 = repositoryManager.getActualRuntimeRepository();
		assertEquals(client1, client2);
		assertEquals(client2, client3);
		assertNotNull(client1.getProductComponent("motor.MotorPlus"));
		assertNotNull(client2.getProductComponent("home.HomeBasic"));
		assertNotNull(client3.getProductComponent("motor.MotorBasic"));
	}

}
