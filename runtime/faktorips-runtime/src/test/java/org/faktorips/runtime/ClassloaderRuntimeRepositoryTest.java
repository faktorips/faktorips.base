/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.faktorips.runtime.DummyTocEntryFactory.DummyRuntimeObject;
import org.faktorips.runtime.internal.DateTime;
import org.faktorips.runtime.test.IpsFormulaTestCase;
import org.faktorips.runtime.test.IpsTest2;
import org.faktorips.runtime.test.IpsTestCase2;
import org.faktorips.runtime.testrepository.PnCProduct;
import org.faktorips.runtime.testrepository.home.HomeProduct;
import org.faktorips.runtime.testrepository.home.HomeProductGen;
import org.faktorips.runtime.testrepository.motor.MotorProduct;
import org.faktorips.runtime.testrepository.motor.MotorProductGen;
import org.faktorips.runtime.testrepository.motor.RateTable;
import org.faktorips.runtime.testrepository.motor.RateTableSingle;
import org.faktorips.runtime.testrepository.test.TestPremiumCalculation;
import org.junit.Before;
import org.junit.Test;

/**
 * This test case operates on the org.faktorips.runtime.testrepository.
 *
 * @author Jan Ortmann
 */
public class ClassloaderRuntimeRepositoryTest {

    private ClassloaderRuntimeRepository repository;

    @Before
    public void setUp() throws Exception {
        // The repository loading the empty TOC to include testing referenced repositories
        repository = ClassloaderRuntimeRepository
                .create("org/faktorips/runtime/testrepository/direct-repository-toc.xml");
        ClassloaderRuntimeRepository mainRepository = ClassloaderRuntimeRepository
                .create("org/faktorips/runtime/testrepository/faktorips-repository-toc.xml");
        repository.addDirectlyReferencedRepository(mainRepository);
    }

    @Test
    public void testGetProductComponent() {
        Calendar effectiveDate = new GregorianCalendar(2005, 1, 1);
        MotorProduct motorPk = (MotorProduct)repository.getProductComponent("motor.MotorBasic");
        assertNotNull(motorPk);
        MotorProductGen motorProductGen = (MotorProductGen)motorPk.getGenerationBase(effectiveDate);
        assertNotNull(motorProductGen);
        // the valid-to of this product component is read from the TOC entry
        assertEquals(new DateTime(2010, 1, 18), motorPk.getValidTo());

        motorPk = (MotorProduct)repository.getProductComponent("motor.MotorPlus");
        assertNotNull(motorPk);
        motorProductGen = (MotorProductGen)motorPk.getGenerationBase(effectiveDate);
        assertNotNull(motorProductGen);
        // the valid-to of this product component is read from the XML content of the product
        // component
        assertEquals(new DateTime(2010, 1, 16), motorPk.getValidTo());

        HomeProduct homePk = (HomeProduct)repository.getProductComponent("home.HomeBasic");
        assertNotNull(homePk);
        HomeProductGen homeProductGen = (HomeProductGen)homePk.getGenerationBase(effectiveDate);
        assertNotNull(homeProductGen);

        // request for none existing component
        assertNull(repository.getProductComponent("notThere"));
    }

    @Test
    public void testGetProductComponent_KindId_VersionId() {
        MotorProduct motorProduct = (MotorProduct)repository.getProductComponent("motor.MotorPlus", "2005-01");
        assertNotNull(motorProduct);
        assertEquals("2005-01", motorProduct.getVersionId());
        assertEquals("motor.MotorPlus", motorProduct.getKindId());
        assertNull(repository.getProductComponent(null, "2005-01"));
        assertNull(repository.getProductComponent("Unknown", "2005-01"));
    }

    @Test
    public void testGetAllProductComponents_KindId() {
        MotorProduct motorProduct = (MotorProduct)repository.getProductComponent("motor.MotorPlus", "2005-01");
        assertNotNull(motorProduct);

        List<IProductComponent> result = repository.getAllProductComponents("motor.MotorPlus");
        assertEquals(1, result.size());
        assertEquals(motorProduct, result.get(0));

        assertEquals(0, repository.getAllProductComponents((String)null).size());
        assertEquals(0, repository.getAllProductComponents("unknown").size());
    }

    @Test
    public void testGetProductComponentGeneration() {
        assertNull(repository.getProductComponentGeneration("motor.MotorPlus", new GregorianCalendar(2004, 11, 31)));
        assertNull(repository.getProductComponentGeneration("unknown", new GregorianCalendar(2005, 1, 1)));
        IProductComponentGeneration gen = repository.getProductComponentGeneration("motor.MotorPlus", null);
        assertNull(gen);

        MotorProductGen motorProductGen = (MotorProductGen)repository.getProductComponentGeneration("motor.MotorPlus",
                new GregorianCalendar(2005, 1, 1));
        assertNotNull(motorProductGen);

        motorProductGen = (MotorProductGen)repository.getProductComponentGeneration("motor.MotorPlus",
                new GregorianCalendar(2006, 1, 1));
        assertNotNull(motorProductGen);
    }

    @Test
    public void testGetAllProductComponents_ByClass() {
        // get all motor products
        List<? extends IProductComponent> list = repository.getAllProductComponents(MotorProduct.class);
        assertEquals(2, list.size());
        assertTrue(list.contains(repository.getProductComponent("motor.MotorBasic")));
        assertTrue(list.contains(repository.getProductComponent("motor.MotorPlus")));

        // get all home products
        list = repository.getAllProductComponents(HomeProduct.class);
        assertEquals(1, list.size());
        assertTrue(list.contains(repository.getProductComponent("home.HomeBasic")));

        // get all PnC products (should return all motor and home products)
        list = repository.getAllProductComponents(PnCProduct.class);
        assertEquals(3, list.size());
        assertTrue(list.contains(repository.getProductComponent("motor.MotorBasic")));
        assertTrue(list.contains(repository.getProductComponent("motor.MotorPlus")));
        assertTrue(list.contains(repository.getProductComponent("home.HomeBasic")));
    }

    @Test
    public void testGetTableleByClass() {
        RateTableSingle table = repository.getTable(RateTableSingle.class);
        assertNotNull(table);
    }

    @Test
    public void testGetTableByQualifiedName() {
        RateTable table = (RateTable)repository.getTable("motor.RateTable");
        assertNotNull(table);
        table = (RateTable)repository.getTable("motor.RateTableAlternate");
        assertNotNull(table);
    }

    @Test
    public void testGetAllProductComponentNames() {
        List<String> allNames = repository.getAllProductComponentIds();
        assertEquals(3, allNames.size());
        assertTrue(allNames.contains("motor.MotorBasic"));
        assertTrue(allNames.contains("motor.MotorPlus"));
        assertTrue(allNames.contains("home.HomeBasic"));
    }

    @Test
    public void testGetAllProductComponents() {
        List<IProductComponent> allProductCmpts = repository.getAllProductComponents();
        assertTrue(allProductCmpts.contains(repository.getProductComponent("motor.MotorBasic")));
        assertTrue(allProductCmpts.contains(repository.getProductComponent("motor.MotorPlus")));
        assertTrue(allProductCmpts.contains(repository.getProductComponent("home.HomeBasic")));
    }

    @Test
    public void testGetProductComponentGenerations() {
        IProductComponent motorPk = repository.getProductComponent("motor.MotorBasic");
        List<IProductComponentGeneration> result = repository.getProductComponentGenerations(motorPk);
        assertEquals(1, result.size());

        motorPk = repository.getProductComponent("motor.MotorPlus");
        result = repository.getProductComponentGenerations(motorPk);
        assertEquals(3, result.size());

        IProductComponent unknownPc = new MotorProduct(new InMemoryRuntimeRepository(), "", "", "");
        result = repository.getProductComponentGenerations(unknownPc);
        assertEquals(0, result.size());
    }

    @Test
    public void testGetIpsTestCase() throws Exception {
        TestPremiumCalculation test = (TestPremiumCalculation)repository.getIpsTestCase("test.CalculationTest1");
        assertNotNull(test);
        assertEquals("test.CalculationTest1", test.getQualifiedName());
        assertEquals("42", test.getInputSumInsured());
        assertEquals("43", test.getExpResultPremium());

        IpsFormulaTestCase formulaTest = (IpsFormulaTestCase)repository.getIpsTest("motor.MotorBasic");
        assertNotNull(formulaTest);
    }

    @Test
    public void testGetAllIpsTestCases() throws Exception {
        List<IpsTest2> tests = repository.getAllIpsTestCases(repository);
        assertEquals(3, tests.size());
    }

    @Test
    public void testGetNumberOfProductComponentGenerations() {
        IProductComponent productComponent = repository.getProductComponent("motor.MotorPlus");
        int number = repository.getNumberOfProductComponentGenerations(productComponent);
        assertEquals(3, number);
    }

    @Test
    public void testGetNextProductComponentGeneration() {
        IProductComponent productComponent = repository.getProductComponent("motor.MotorPlus");
        MotorProductGen motorProductGen = (MotorProductGen)productComponent.getGenerationBase(new GregorianCalendar(
                2005, Calendar.JANUARY, 1));
        assertNotNull(motorProductGen);

        MotorProductGen next = (MotorProductGen)repository.getNextProductComponentGeneration(motorProductGen);
        assertEquals(new DateTime(2006, 1, 1).toDate(TimeZone.getDefault()), next.getValidFrom(TimeZone.getDefault()));

        motorProductGen = (MotorProductGen)productComponent.getGenerationBase(new GregorianCalendar(2006,
                Calendar.JANUARY, 1));
        assertNotNull(motorProductGen);

        next = (MotorProductGen)repository.getNextProductComponentGeneration(motorProductGen);
        assertEquals(new DateTime(2007, 1, 1).toDate(TimeZone.getDefault()), next.getValidFrom(TimeZone.getDefault()));

        motorProductGen = (MotorProductGen)productComponent.getGenerationBase(new GregorianCalendar(2007,
                Calendar.JANUARY, 1));
        assertNotNull(motorProductGen);

        next = (MotorProductGen)repository.getNextProductComponentGeneration(motorProductGen);
        assertNull(next);
    }

    @Test
    public void testGetPreviousProductComponentGeneration() {
        IProductComponent productComponent = repository.getProductComponent("motor.MotorPlus");

        MotorProductGen motorProductGen = (MotorProductGen)productComponent.getGenerationBase(new GregorianCalendar(
                2007, Calendar.JANUARY, 1));
        MotorProductGen previous = (MotorProductGen)repository.getPreviousProductComponentGeneration(motorProductGen);
        assertEquals(new DateTime(2006, 1, 1).toDate(TimeZone.getDefault()),
                previous.getValidFrom(TimeZone.getDefault()));

        motorProductGen = (MotorProductGen)productComponent.getGenerationBase(new GregorianCalendar(2006,
                Calendar.JANUARY, 1));
        previous = (MotorProductGen)repository.getPreviousProductComponentGeneration(motorProductGen);
        assertEquals(new DateTime(2005, 1, 1).toDate(TimeZone.getDefault()),
                previous.getValidFrom(TimeZone.getDefault()));

        motorProductGen = (MotorProductGen)productComponent.getGenerationBase(new GregorianCalendar(2005,
                Calendar.JANUARY, 1));
        previous = (MotorProductGen)repository.getPreviousProductComponentGeneration(motorProductGen);
        assertNull(previous);
    }

    @Test
    public void testGetLatestProductComponentGeneration() {
        IProductComponent productComponent = repository.getProductComponent("motor.MotorPlus");
        IProductComponentGeneration generation = repository.getLatestProductComponentGeneration(productComponent);
        assertEquals(new DateTime(2007, 1, 1).toDate(TimeZone.getDefault()),
                generation.getValidFrom(TimeZone.getDefault()));
    }

    @Test
    public void testGetIpsTestCaseStartingWith() {
        assertIpsTestCasesStartingWith("test", new String[] { "test.CalculationTest1", "test.CalculationTest2" });
        assertIpsTestCasesStartingWith("test.CalculationTest", new String[] { "test.CalculationTest1",
                "test.CalculationTest2" });
        assertIpsTestCasesStartingWith("test.CalculationTest1", new String[] { "test.CalculationTest1" });
        assertIpsTestCasesStartingWith("test1", new String[] {});
    }

    private void assertIpsTestCasesStartingWith(String qNamePrefix, String[] testCasesExpected) {
        List<IpsTest2> result = repository.getIpsTestCasesStartingWith(qNamePrefix, repository);
        assertEquals("Unexpected number of test cases", testCasesExpected.length, result.size());
        for (String element : testCasesExpected) {
            boolean found = false;
            for (IpsTest2 ipsTest2 : result) {
                IpsTestCase2 testCase = (IpsTestCase2)ipsTest2;
                if (testCase.getQualifiedName().equals(element)) {
                    found = true;
                    break;
                }

            }
            assertTrue("Missing test case: " + element, found);
        }
    }

    @Test
    public void testGetByType() {
        DummyRuntimeObject dummyRuntimeObject = repository.getCustomRuntimeObject(DummyRuntimeObject.class,
                "dummy.DummyRuntimeObject");
        assertNotNull(dummyRuntimeObject);
        dummyRuntimeObject = repository.getCustomRuntimeObject(DummyRuntimeObject.class, "dummy.DummyRuntimeObject2");
        assertNull(dummyRuntimeObject);
        class NoClass implements IRuntimeObject {

            @Override
            public Set<String> getExtensionPropertyIds() {
                return Set.of();
            }

            @Override
            public Object getExtensionPropertyValue(String propertyId) {
                return null;
            }
        }
        NoClass noClassObject = repository.getCustomRuntimeObject(NoClass.class, "dummy.DummyRuntimeObject");
        assertNull(noClassObject);
    }
}
