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

package org.faktorips.runtime.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.faktorips.runtime.IEnumValueLookupService;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.ITable;
import org.faktorips.runtime.InMemoryRuntimeRepository;
import org.faktorips.runtime.ProductCmptGenerationNotFoundException;
import org.faktorips.runtime.ProductCmptNotFoundException;
import org.faktorips.runtime.test.IpsTest2;
import org.faktorips.runtime.test.IpsTestSuite;
import org.faktorips.runtime.testrepository.test.TestPremiumCalculation;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class TestAbstractRuntimeRepositoryTest {

    private InMemoryRuntimeRepository mainRepository;
    private InMemoryRuntimeRepository inBetweenRepositoryA;
    private InMemoryRuntimeRepository inBetweenRepositoryB;
    private InMemoryRuntimeRepository baseRepository;

    private final DateTime validFrom = new DateTime(2006, 1, 1);
    private final DateTime validTo = new DateTime(2007, 1, 1);
    private final Calendar effectiveDate = new GregorianCalendar(2006, 6, 1);

    // one product component and generation residing in each repository
    private ProductComponent basePc;
    private ProductComponentGeneration basePcGen;
    private ProductComponent inAPc;
    private ProductComponentGeneration inAPcGen;
    private ProductComponent inBPc;
    private ProductComponentGeneration inBPcGen;
    private ProductComponent mainPc;
    private ProductComponentGeneration mainPcGen;

    private final ITable testTable = new TestTable();
    private TestProductComponent validToPc;
    private TestProductCmptGeneration validToPcGen;

    @Before
    public void setUp() throws Exception {
        mainRepository = new InMemoryRuntimeRepository();
        inBetweenRepositoryA = new InMemoryRuntimeRepository();
        inBetweenRepositoryB = new InMemoryRuntimeRepository();
        baseRepository = new InMemoryRuntimeRepository();

        mainRepository.addDirectlyReferencedRepository(inBetweenRepositoryA);
        mainRepository.addDirectlyReferencedRepository(inBetweenRepositoryB);
        inBetweenRepositoryA.addDirectlyReferencedRepository(baseRepository);
        inBetweenRepositoryB.addDirectlyReferencedRepository(baseRepository);

        basePc = new TestProductComponent(baseRepository, "basePc", "baseKind", "baseVersion");
        basePcGen = new TestProductCmptGeneration(basePc);
        basePcGen.setValidFrom(validFrom);
        baseRepository.putProductCmptGeneration(basePcGen);
        baseRepository.putTable(testTable);

        inAPc = new TestProductComponent(inBetweenRepositoryA, "inAPc", "inAKind", "inAVersion");
        inAPcGen = new TestProductCmptGeneration(inAPc);
        inAPcGen.setValidFrom(validFrom);
        inBetweenRepositoryA.putProductCmptGeneration(inAPcGen);

        inBPc = new TestProductComponent(inBetweenRepositoryB, "inBPc", "inBKind", "inBVersion");
        inBPcGen = new TestProductCmptGeneration(inBPc);
        inBPcGen.setValidFrom(validFrom);
        inBetweenRepositoryB.putProductCmptGeneration(inBPcGen);

        mainPc = new TestProductComponent(mainRepository, "mainPc", "mainKind", "mainVersion");
        mainPcGen = new TestProductCmptGeneration(mainPc);
        mainPcGen.setValidFrom(validFrom);
        mainRepository.putProductCmptGeneration(mainPcGen);

        validToPc = new TestProductComponent(mainRepository, "validToPc", "validToKind", "validToVersion");
        validToPc.setValidTo(validTo);
        validToPcGen = new TestProductCmptGeneration(validToPc);
        validToPcGen.setValidFrom(validFrom);
        mainRepository.putProductCmptGeneration(validToPcGen);
    }

    @Test
    public void testAddRuntimeRepository() {
        mainRepository = new InMemoryRuntimeRepository();
        InMemoryRuntimeRepository r1 = new InMemoryRuntimeRepository();
        InMemoryRuntimeRepository r2 = new InMemoryRuntimeRepository();

        mainRepository.addDirectlyReferencedRepository(r1);
        assertEquals(1, mainRepository.getDirectlyReferencedRepositories().size());
        assertEquals(r1, mainRepository.getDirectlyReferencedRepositories().get(0));

        mainRepository.addDirectlyReferencedRepository(r2);
        assertEquals(2, mainRepository.getDirectlyReferencedRepositories().size());
        assertEquals(r1, mainRepository.getDirectlyReferencedRepositories().get(0));
        assertEquals(r2, mainRepository.getDirectlyReferencedRepositories().get(1));
    }

    @Test
    public void testGetAllReferencedRepositories() {
        List<IRuntimeRepository> result = baseRepository.getAllReferencedRepositories();
        assertEquals(0, result.size());

        result = inBetweenRepositoryA.getAllReferencedRepositories();
        assertEquals(1, result.size());
        assertEquals(baseRepository, result.get(0));

        result = mainRepository.getAllReferencedRepositories();
        assertEquals(3, result.size());
        assertEquals(inBetweenRepositoryA, result.get(0));
        assertEquals(inBetweenRepositoryB, result.get(1));
        assertEquals(baseRepository, result.get(2));
    }

    @Test
    public void testGetProductComponent_ById() {
        assertEquals(mainPc, mainRepository.getProductComponent("mainPc"));
        assertEquals(inAPc, mainRepository.getProductComponent("inAPc"));
        assertEquals(inBPc, mainRepository.getProductComponent("inBPc"));
        assertEquals(basePc, mainRepository.getProductComponent("basePc"));

        assertNull(mainRepository.getProductComponent("unknown"));
    }

    @Test
    public void testGetExistingProductComponent_ById() {
        assertEquals(mainPc, mainRepository.getProductComponent("mainPc"));
        assertEquals(inAPc, mainRepository.getProductComponent("inAPc"));
        assertEquals(inBPc, mainRepository.getProductComponent("inBPc"));
        assertEquals(basePc, mainRepository.getProductComponent("basePc"));

        try {
            mainRepository.getExistingProductComponent("unknown");
            fail();
        } catch (ProductCmptNotFoundException e) {
            assertEquals("unknown", e.getProductCmptId());
            assertEquals(mainRepository.getName(), e.getRepositoryName());
        }
    }

    @Test
    public void testGetProductComponentGeneration() {
        assertEquals(mainPcGen, mainRepository.getProductComponentGeneration("mainPc", effectiveDate));
        assertEquals(inAPcGen, mainRepository.getProductComponentGeneration("inAPc", effectiveDate));
        assertEquals(inBPcGen, mainRepository.getProductComponentGeneration("inBPc", effectiveDate));
        assertEquals(basePcGen, mainRepository.getProductComponentGeneration("basePc", effectiveDate));

        assertNull(mainRepository.getProductComponentGeneration("unknown", effectiveDate));
        assertNull(mainRepository.getProductComponentGeneration("mainPc", new GregorianCalendar(2000, 0, 1)));
    }

    @Test
    public void testGetExistingProductComponentGeneration() {
        assertEquals(mainPcGen, mainRepository.getExistingProductComponentGeneration("mainPc", effectiveDate));
        assertEquals(inAPcGen, mainRepository.getExistingProductComponentGeneration("inAPc", effectiveDate));
        assertEquals(inBPcGen, mainRepository.getExistingProductComponentGeneration("inBPc", effectiveDate));
        assertEquals(basePcGen, mainRepository.getExistingProductComponentGeneration("basePc", effectiveDate));

        try {
            mainRepository.getExistingProductComponentGeneration("unknown", effectiveDate);
            fail();
        } catch (ProductCmptGenerationNotFoundException e) {
            assertEquals(mainRepository.getName(), e.getRepositoryName());
            assertEquals("unknown", e.getProductCmptId());
            assertEquals(effectiveDate, e.getEffetiveDate());
            assertFalse(e.productCmptWasFound());
        }

        try {
            mainRepository.getExistingProductComponentGeneration("mainPc", new GregorianCalendar(2000, 0, 1));
            fail();
        } catch (ProductCmptGenerationNotFoundException e) {
            assertEquals(mainRepository.getName(), e.getRepositoryName());
            assertEquals("mainPc", e.getProductCmptId());
            assertEquals(new GregorianCalendar(2000, 0, 1), e.getEffetiveDate());
            assertTrue(e.productCmptWasFound());
        }
    }

    @Test
    public void testGetProductComponent_ByKindIdVersionId() {
        assertEquals(mainPc, mainRepository.getProductComponent("mainKind", "mainVersion"));
        assertEquals(inAPc, mainRepository.getProductComponent("inAKind", "inAVersion"));
        assertEquals(inBPc, mainRepository.getProductComponent("inBKind", "inBVersion"));
        assertEquals(basePc, mainRepository.getProductComponent("baseKind", "baseVersion"));
    }

    @Test
    public void testGetAllProductComponents_ByKindId() {
        assertEquals(1, mainRepository.getAllProductComponents("mainKind").size());
        assertEquals(mainPc, mainRepository.getAllProductComponents("mainKind").get(0));

        assertEquals(1, mainRepository.getAllProductComponents("baseKind").size());
        assertEquals(basePc, mainRepository.getAllProductComponents("baseKind").get(0));
    }

    @Test
    public void testGetProductComponentGeneration_ByIdAndEffectiveDate() {
        assertEquals(mainPcGen, mainRepository.getProductComponentGeneration("mainPc", effectiveDate));
        assertEquals(inAPcGen, mainRepository.getProductComponentGeneration("inAPc", effectiveDate));
        assertEquals(inBPcGen, mainRepository.getProductComponentGeneration("inBPc", effectiveDate));
        assertEquals(basePcGen, mainRepository.getProductComponentGeneration("basePc", effectiveDate));
        // Tests with validTo
        assertEquals(validToPcGen, mainRepository.getProductComponentGeneration("validToPc", effectiveDate));
        assertEquals(
                validToPcGen,
                mainRepository.getProductComponentGeneration("validToPc",
                        validTo.toGregorianCalendar(effectiveDate.getTimeZone())));
        GregorianCalendar tooLate = validTo.toGregorianCalendar(effectiveDate.getTimeZone());
        tooLate.add(Calendar.MILLISECOND, 1);
        assertNull(mainRepository.getProductComponentGeneration("validToPc", tooLate));
    }

    @Test
    public void testGetAllProductComponents_ByClass() {
        List<IProductComponent> result = mainRepository.getAllProductComponents(TestProductComponent.class);
        assertEquals(5, result.size());
        assertTrue(mainPc + " not exists", result.contains(mainPc));
        assertTrue(validToPc + " not exists", result.contains(validToPc));
        assertTrue(inAPc + " not exists", result.contains(inAPc));
        assertTrue(inBPc + " not exists", result.contains(inBPc));
        assertTrue(basePc + " not exists", result.contains(basePc));

        result = inBetweenRepositoryA.getAllProductComponents(TestProductComponent.class);
        assertEquals(2, result.size());
        assertTrue(inAPc + " not exists", result.contains(inAPc));
        assertTrue(basePc + " not exists", result.contains(basePc));

        result = baseRepository.getAllProductComponents(TestProductComponent.class);
        assertEquals(1, result.size());
        assertEquals(basePc, result.get(0));
    }

    @Test
    public void testGetAllProductComponents() {
        List<IProductComponent> result = mainRepository.getAllProductComponents();
        assertEquals(5, result.size());
        assertTrue(mainPc + " not exists", result.contains(mainPc));
        assertTrue(validToPc + " not exists", result.contains(validToPc));
        assertTrue(inAPc + " not exists", result.contains(inAPc));
        assertTrue(inBPc + " not exists", result.contains(inBPc));
        assertTrue(basePc + " not exists", result.contains(basePc));

        result = inBetweenRepositoryA.getAllProductComponents();
        assertEquals(2, result.size());
        assertTrue(inAPc + " not exists", result.contains(inAPc));
        assertTrue(basePc + " not exists", result.contains(basePc));

        result = baseRepository.getAllProductComponents();
        assertEquals(1, result.size());
        assertEquals(basePc, result.get(0));
    }

    @Test
    public void testGetAllProductComponentIds() {
        List<String> result = mainRepository.getAllProductComponentIds();
        assertEquals(5, result.size());
        assertTrue(mainPc + " not exists", result.contains(mainPc.getId()));
        assertTrue(validToPc + " not exists", result.contains(validToPc.getId()));
        assertTrue(inAPc + " not exists", result.contains(inAPc.getId()));
        assertTrue(inBPc + " not exists", result.contains(inBPc.getId()));
        assertTrue(basePc + " not exists", result.contains(basePc.getId()));

        result = inBetweenRepositoryA.getAllProductComponentIds();
        assertEquals(2, result.size());
        assertTrue(inAPc + " not exists", result.contains(inAPc.getId()));
        assertTrue(basePc + " not exists", result.contains(basePc.getId()));

        result = baseRepository.getAllProductComponentIds();
        assertEquals(1, result.size());
        assertEquals(basePc.getId(), result.get(0));
    }

    @Test
    public void testGetProductComponentGenerations() {
        assertEquals(1, mainRepository.getProductComponentGenerations(mainPc).size());
        assertEquals(mainPcGen, mainRepository.getProductComponentGenerations(mainPc).get(0));
        assertEquals(basePcGen, mainRepository.getProductComponentGenerations(basePc).get(0));
    }

    @Test
    public void testGetTable() {
        assertEquals(testTable, mainRepository.getTable(TestTable.class));
        assertEquals(testTable, baseRepository.getTable(TestTable.class));
    }

    @Test
    public void testGetTestSuite() throws Exception {
        mainRepository = new InMemoryRuntimeRepository();
        TestPremiumCalculation test1 = new TestPremiumCalculation("pack.Test1");
        TestPremiumCalculation test2 = new TestPremiumCalculation("pack.Test2");
        TestPremiumCalculation test3 = new TestPremiumCalculation("pack.a.Test3");
        TestPremiumCalculation test4 = new TestPremiumCalculation("pack.b.Test4");
        TestPremiumCalculation test5 = new TestPremiumCalculation("pack.a.Test5");
        TestPremiumCalculation test6 = new TestPremiumCalculation("pack.a.c.Test6");
        TestPremiumCalculation test7 = new TestPremiumCalculation("pack.x.y.Test7");
        mainRepository.putIpsTestCase(test1);
        mainRepository.putIpsTestCase(test2);
        mainRepository.putIpsTestCase(test3);
        mainRepository.putIpsTestCase(test4);
        mainRepository.putIpsTestCase(test5);
        mainRepository.putIpsTestCase(test6);
        mainRepository.putIpsTestCase(test7);

        IpsTestSuite suite = mainRepository.getIpsTestSuite("myPack");
        assertEquals("myPack", suite.getQualifiedName());

        suite = mainRepository.getIpsTestSuite("pack");
        List<IpsTest2> tests = suite.getTests();
        assertEquals(5, tests.size());
        assertNotNull(suite.getTest("Test1"));
        assertNotNull(suite.getTest("Test2"));
        assertNotNull(suite.getTest("a"));
        assertNotNull(suite.getTest("b"));
        assertNotNull(suite.getTest("x"));

        IpsTestSuite suiteA = (IpsTestSuite)suite.getTest("a");
        tests = suiteA.getTests();
        assertEquals(3, tests.size());

        IpsTestSuite suiteX = (IpsTestSuite)suite.getTest("x");
        IpsTestSuite suiteY = (IpsTestSuite)suiteX.getTest("y");
        assertNotNull(suiteY.getTest("Test7"));
    }

    @Test
    public void testGetIpsTest() throws Exception {
        mainRepository = new InMemoryRuntimeRepository();
        TestPremiumCalculation test1 = new TestPremiumCalculation("pack.Test1");
        TestPremiumCalculation test2 = new TestPremiumCalculation("pack.Test2");
        TestPremiumCalculation test3 = new TestPremiumCalculation("pack.a.Test3");
        mainRepository.putIpsTestCase(test1);
        mainRepository.putIpsTestCase(test2);
        mainRepository.putIpsTestCase(test3);

        assertEquals(test1, mainRepository.getIpsTest("pack.Test1"));
        IpsTestSuite suite = (IpsTestSuite)mainRepository.getIpsTest("pack");
        assertEquals(3, suite.size());

        suite = (IpsTestSuite)mainRepository.getIpsTest("unknown");
        assertEquals(0, suite.size());

        try {
            mainRepository.getIpsTest(null);
            fail();
        } catch (Exception e) {
            // OK
        }
    }

    @Test
    public void testAddEnumValueLookup() {
        Lookup lookup = new Lookup();
        mainRepository.addEnumValueLookupService(lookup);
        assertEquals(lookup, mainRepository.getEnumValueLookupService(TestEnumValue.class));

        Lookup lookup2 = new Lookup();
        mainRepository.addEnumValueLookupService(lookup2);
        assertEquals(lookup2, mainRepository.getEnumValueLookupService(TestEnumValue.class));
    }

    @Test
    public void testGetEnumValueLookup() {
        mainRepository.getEnumValueLookupService(TestEnumValue.class);

        Lookup lookup = new Lookup();
        mainRepository.addEnumValueLookupService(lookup);
        assertEquals(lookup, mainRepository.getEnumValueLookupService(TestEnumValue.class));

        Lookup lookup2 = new Lookup();
        mainRepository.addEnumValueLookupService(lookup2);
        assertEquals(lookup2, mainRepository.getEnumValueLookupService(TestEnumValue.class));
    }

    @Test
    public void testRemoveEnumValueLookup() {
        Lookup lookup = new Lookup();
        mainRepository.removeEnumValueLookupService(lookup);

        mainRepository.addEnumValueLookupService(lookup);
        assertEquals(lookup, mainRepository.getEnumValueLookupService(TestEnumValue.class));
        mainRepository.removeEnumValueLookupService(lookup);
        assertNull(mainRepository.getEnumValueLookupService(TestEnumValue.class));
    }

    @Test
    public void testGetEnumValueFromLookup() {
        Lookup lookup = new Lookup();
        assertNull(baseRepository.getEnumValue(TestEnumValue.class, lookup.value1.getEnumValueId()));

        baseRepository.addEnumValueLookupService(lookup);
        assertEquals(lookup.value1, baseRepository.getEnumValue(TestEnumValue.class, lookup.value1.getEnumValueId()));
        assertEquals(lookup.value2, baseRepository.getEnumValue(TestEnumValue.class, lookup.value2.getEnumValueId()));
        assertNull(baseRepository.getEnumValue(TestEnumValue.class, "unknownId"));

        // test if the search through referenced repositories works
        assertEquals(lookup.value1, mainRepository.getEnumValue(TestEnumValue.class, lookup.value1.getEnumValueId()));
        assertEquals(lookup.value2, mainRepository.getEnumValue(TestEnumValue.class, lookup.value2.getEnumValueId()));

        assertNull(mainRepository.getEnumValue(TestEnumValue.class, "unknownId"));
    }

    @Test
    public void testGetEnumValuesFromLookup() {
        Lookup lookup = new Lookup();
        assertNull(baseRepository.getEnumValues(TestEnumValue.class));

        baseRepository.addEnumValueLookupService(lookup);
        List<TestEnumValue> values = baseRepository.getEnumValues(TestEnumValue.class);
        assertEquals(lookup.value1, values.get(0));
        assertEquals(lookup.value2, values.get(1));

        values = mainRepository.getEnumValues(TestEnumValue.class);
        assertEquals(lookup.value1, values.get(0));
        assertEquals(lookup.value2, values.get(1));

        // test if list is unmodifiable
        try {
            values.add(new TestEnumValue("value3"));
            fail();
        } catch (UnsupportedOperationException e) {
            // OK
        }

    }

    class TestTable implements ITable {
        // test class
    }

    private class Lookup implements IEnumValueLookupService<TestEnumValue> {

        private final TestEnumValue value1 = new TestEnumValue("value1");
        private final TestEnumValue value2 = new TestEnumValue("value2");

        private final List<TestEnumValue> values = new ArrayList<TestEnumValue>();

        public Lookup() {
            values.add(value1);
            values.add(value2);
        }

        @Override
        public Class<TestEnumValue> getEnumTypeClass() {
            return TestEnumValue.class;
        }

        @Override
        public TestEnumValue getEnumValue(Object id) {
            for (TestEnumValue value : values) {
                if (value.getEnumValueId().equals(id)) {
                    return value;
                }
            }
            return null;
        }

        @Override
        public List<TestEnumValue> getEnumValues() {
            return values;
        }

        @Override
        public XmlAdapter<?, TestEnumValue> getXmlAdapter() {
            return null;
        }

    }

}
