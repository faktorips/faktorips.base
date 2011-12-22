/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IRuntimeRepositoryManager;
import org.faktorips.runtime.InMemoryRuntimeRepository;
import org.faktorips.runtime.internal.DateTime;
import org.faktorips.runtime.internal.toc.EnumContentTocEntry;
import org.faktorips.runtime.internal.toc.GenerationTocEntry;
import org.faktorips.runtime.internal.toc.ProductCmptTocEntry;
import org.faktorips.runtime.internal.toc.TableContentTocEntry;
import org.faktorips.runtime.internal.toc.TestCaseTocEntry;
import org.faktorips.runtime.productdataprovider.DetachedContentRuntimeRepositoryManager.Builder;
import org.faktorips.runtime.test.IpsFormulaTestCase;
import org.faktorips.runtime.test.IpsTest2;
import org.faktorips.runtime.test.IpsTestCase2;
import org.faktorips.runtime.test.IpsTestCaseBase;
import org.faktorips.runtime.testrepository.PnCProduct;
import org.faktorips.runtime.testrepository.home.HomeProduct;
import org.faktorips.runtime.testrepository.home.HomeProductGen;
import org.faktorips.runtime.testrepository.motor.MotorProduct;
import org.faktorips.runtime.testrepository.motor.MotorProductGen;
import org.faktorips.runtime.testrepository.motor.RateTable;
import org.faktorips.runtime.testrepository.test.TestPremiumCalculation;
import org.faktorips.values.Decimal;
import org.faktorips.values.Money;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class DetachedContentRuntimeRepositoryManagerTest {

    private IRuntimeRepository repository;
    private TestProductDataProvider productDataProvider;
    private IRuntimeRepositoryManager runtimeRepositoryManager;
    private MyFactory pdpFactory;
    private TestProductDataProvider directPdp;
    private MyFactory directPdpFactory;

    @Before
    public void setUp() throws Exception {
        // the context class loader must be the class loader of the runtime bundle
        Thread.currentThread().setContextClassLoader(DetachedContentRuntimeRepositoryManager.class.getClassLoader());
        directPdpFactory = new MyFactory(getClass().getClassLoader(),
                "org/faktorips/runtime/testrepository/direct-repository-toc.xml");
        pdpFactory = new MyFactory(getClass().getClassLoader(),
                "org/faktorips/runtime/testrepository/faktorips-repository-toc.xml");
        runtimeRepositoryManager = new Builder(directPdpFactory).build();
        IRuntimeRepositoryManager referencedManager = new Builder(pdpFactory).build();
        runtimeRepositoryManager.addDirectlyReferencedManager(referencedManager);
        repository = runtimeRepositoryManager.getActualRuntimeRepository();
        directPdp = directPdpFactory.testProductDataProvider;
        productDataProvider = pdpFactory.testProductDataProvider;
    }

    @Test
    public void testGetProductComponent() {
        Calendar effectiveDate = new GregorianCalendar(2005, 1, 1);
        MotorProduct motorPk = (MotorProduct)repository.getProductComponent("motor.MotorBasic");
        assertNotNull(motorPk);
        MotorProductGen motorProductGen = (MotorProductGen)motorPk.getGenerationBase(effectiveDate);
        assertNotNull(motorProductGen);
        assertEquals(Decimal.valueOf("0.15"), motorProductGen.getTaxRate());
        assertEquals(Money.euro(15, 0), motorProductGen.getFixedCosts());
        // the valid-to of this product component is read from the TOC entry
        assertEquals(new DateTime(2010, 1, 18), motorPk.getValidTo());

        motorPk = (MotorProduct)repository.getProductComponent("motor.MotorPlus");
        assertNotNull(motorPk);
        motorProductGen = (MotorProductGen)motorPk.getGenerationBase(effectiveDate);
        assertNotNull(motorProductGen);
        assertEquals(Decimal.valueOf("0.15"), motorProductGen.getTaxRate());
        assertEquals(Money.euro(20, 0), motorProductGen.getFixedCosts());
        // the valid-to of this product component is read from the XML content of the product
        // component
        assertEquals(new DateTime(2010, 1, 16), motorPk.getValidTo());

        HomeProduct homePk = (HomeProduct)repository.getProductComponent("home.HomeBasic");
        assertNotNull(homePk);
        HomeProductGen homeProductGen = (HomeProductGen)homePk.getGenerationBase(effectiveDate);
        assertNotNull(homeProductGen);
        assertEquals(Decimal.valueOf("0.16"), homeProductGen.getTaxRate());
        assertEquals(Money.euro(10, 0), homeProductGen.getFixedCosts());

        // request for none existing component
        assertNull(repository.getProductComponent("notThere"));
        assertTrue(productDataProvider.flag);

        productDataProvider.flag = false;
        // should use cached object
        repository.getProductComponent("home.HomeBasic");
        assertFalse(productDataProvider.flag);
        productDataProvider.baseVersion = "1";

        // should use cached object
        repository.getProductComponent("home.HomeBasic");
        assertFalse(productDataProvider.flag);

        repository = runtimeRepositoryManager.getActualRuntimeRepository();
        productDataProvider = pdpFactory.testProductDataProvider;

        repository.getProductComponent("home.HomeBasic");
        // should NOT use cached object
        assertTrue(productDataProvider.flag);

        productDataProvider.flag = false;
        // should use cached object
        repository.getProductComponent("home.HomeBasic");
        assertFalse(productDataProvider.flag);

        directPdp = directPdpFactory.testProductDataProvider;
        directPdp.baseVersion = "654321";

        // should use cached object until getActualRuntimeRepository()
        repository.getProductComponent("home.HomeBasic");
        assertFalse(productDataProvider.flag);

        repository = runtimeRepositoryManager.getActualRuntimeRepository();

        repository.getProductComponent("home.HomeBasic");
        // still cached because HomeBasic is in referenced repository
        assertFalse(productDataProvider.flag);

        directPdp = directPdpFactory.testProductDataProvider;
        repository.getIpsTest("test.CalculationTest2");
        // should NOT use cached object
        assertTrue(directPdp.flag);
    }

    @Test
    public void testGetProductComponent_KindId_VersionId() {
        repository = runtimeRepositoryManager.getActualRuntimeRepository();
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
        assertEquals(Decimal.valueOf("0.15"), motorProductGen.getTaxRate());
        assertEquals(Money.euro(20, 0), motorProductGen.getFixedCosts());

        motorProductGen = (MotorProductGen)repository.getProductComponentGeneration("motor.MotorPlus",
                new GregorianCalendar(2006, 1, 1));
        assertNotNull(motorProductGen);
        assertEquals(Decimal.valueOf("0.16"), motorProductGen.getTaxRate());
        assertEquals(Money.euro(30, 0), motorProductGen.getFixedCosts());

        assertTrue(productDataProvider.flag);
        productDataProvider.flag = false;
        // should use cached object
        repository.getProductComponentGeneration("motor.MotorPlus", new GregorianCalendar(2006, 1, 1));
        assertFalse(productDataProvider.flag);

        productDataProvider.baseVersion = "1";

        // should use cached object
        repository.getProductComponentGeneration("motor.MotorPlus", new GregorianCalendar(2006, 1, 1));
        assertFalse(productDataProvider.flag);

        repository = runtimeRepositoryManager.getActualRuntimeRepository();
        productDataProvider = pdpFactory.testProductDataProvider;

        repository.getProductComponentGeneration("motor.MotorPlus", new GregorianCalendar(2006, 1, 1));
        // should NOT use cached object
        assertTrue(productDataProvider.flag);
    }

    @Test
    public void testGetAllProductComponentsByType() {
        // get all motor products
        List<IProductComponent> list = repository.getAllProductComponents(MotorProduct.class);
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
    public void testGetTableByClass() {
        RateTable table = repository.getTable(RateTable.class);
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

        IpsTestCaseBase test2 = repository.getIpsTestCase("test.CalculationTest2");
        assertNotNull(test);
        assertEquals("test.CalculationTest2", test2.getQualifiedName());

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

    private static class TestProductDataProvider extends ClassLoaderProductDataProvider {

        /**
         * set true by any method called (except getModificationStamp()) indicates that one of the
         * overridden methods was called - that means the data is not loaded from cache
         */
        boolean flag = false;

        String baseVersion = "0";

        public TestProductDataProvider(ClassLoaderDataSource dataSource, String toc) {
            super(dataSource, toc, true);
        }

        @Override
        public InputStream getEnumContentAsStream(EnumContentTocEntry tocEntry) throws DataModifiedException {
            flag = true;
            return super.getEnumContentAsStream(tocEntry);
        }

        @Override
        public Element getProductCmptData(ProductCmptTocEntry tocEntry) throws DataModifiedException {
            flag = true;
            return super.getProductCmptData(tocEntry);
        }

        @Override
        public Element getProductCmptGenerationData(GenerationTocEntry tocEntry) throws DataModifiedException {
            flag = true;
            return super.getProductCmptGenerationData(tocEntry);
        }

        @Override
        public InputStream getTableContentAsStream(TableContentTocEntry tocEntry) throws DataModifiedException {
            flag = true;
            return super.getTableContentAsStream(tocEntry);
        }

        @Override
        public Element getTestcaseElement(TestCaseTocEntry tocEntry) throws DataModifiedException {
            flag = true;
            return super.getTestcaseElement(tocEntry);
        }

        @Override
        public String getBaseVersion() {
            if (baseVersion == null) {
                return "0";
            }
            return baseVersion;
        }

    }

    public static class MyFactory extends ClassLoaderProductDataProviderFactory {

        private TestProductDataProvider testProductDataProvider;
        private final ClassLoader cl;
        private final String tocResourcePath2;

        public MyFactory(ClassLoader cl, String tocResourcePath) {
            super(tocResourcePath);
            this.cl = cl;
            tocResourcePath2 = tocResourcePath;
        }

        @Override
        public IProductDataProvider newInstance() {
            ClassLoaderDataSource dataSource = new ClassLoaderDataSource(cl);
            testProductDataProvider = new TestProductDataProvider(dataSource, tocResourcePath2);
            return testProductDataProvider;
        }

    }

}
