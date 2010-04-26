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

package org.faktorips.runtime.internal;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import junit.framework.TestCase;

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

/**
 * 
 * @author Jan Ortmann
 */
public class TestAbstractRuntimeRepositoryTest extends TestCase {

    private InMemoryRuntimeRepository mainRepository;
    private InMemoryRuntimeRepository inBetweenRepositoryA;
    private InMemoryRuntimeRepository inBetweenRepositoryB;
    private InMemoryRuntimeRepository baseRepository;

    private final DateTime validFrom = new DateTime(2006, 1, 1);
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

    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
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

    }

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

    public void testGetProductComponent_ById() {
        assertEquals(mainPc, mainRepository.getProductComponent("mainPc"));
        assertEquals(inAPc, mainRepository.getProductComponent("inAPc"));
        assertEquals(inBPc, mainRepository.getProductComponent("inBPc"));
        assertEquals(basePc, mainRepository.getProductComponent("basePc"));

        assertNull(mainRepository.getProductComponent("unknown"));
    }

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

    public void testGetProductComponentGeneration() {
        assertEquals(mainPcGen, mainRepository.getProductComponentGeneration("mainPc", effectiveDate));
        assertEquals(inAPcGen, mainRepository.getProductComponentGeneration("inAPc", effectiveDate));
        assertEquals(inBPcGen, mainRepository.getProductComponentGeneration("inBPc", effectiveDate));
        assertEquals(basePcGen, mainRepository.getProductComponentGeneration("basePc", effectiveDate));

        assertNull(mainRepository.getProductComponentGeneration("unknown", effectiveDate));
        assertNull(mainRepository.getProductComponentGeneration("mainPc", new GregorianCalendar(2000, 0, 1)));
    }

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

    public void testGetProductComponent_ByKindIdVersionId() {
        assertEquals(mainPc, mainRepository.getProductComponent("mainKind", "mainVersion"));
        assertEquals(inAPc, mainRepository.getProductComponent("inAKind", "inAVersion"));
        assertEquals(inBPc, mainRepository.getProductComponent("inBKind", "inBVersion"));
        assertEquals(basePc, mainRepository.getProductComponent("baseKind", "baseVersion"));
    }

    public void testGetAllProductComponents_ByKindId() {
        assertEquals(1, mainRepository.getAllProductComponents("mainKind").size());
        assertEquals(mainPc, mainRepository.getAllProductComponents("mainKind").get(0));

        assertEquals(1, mainRepository.getAllProductComponents("baseKind").size());
        assertEquals(basePc, mainRepository.getAllProductComponents("baseKind").get(0));
    }

    public void testGetProductComponentGeneration_ByIdAndEffectiveDate() {
        assertEquals(mainPcGen, mainRepository.getProductComponentGeneration("mainPc", effectiveDate));
        assertEquals(inAPcGen, mainRepository.getProductComponentGeneration("inAPc", effectiveDate));
        assertEquals(inBPcGen, mainRepository.getProductComponentGeneration("inBPc", effectiveDate));
        assertEquals(basePcGen, mainRepository.getProductComponentGeneration("basePc", effectiveDate));
    }

    public void testGetAllProductComponents_ByClass() {
        List<IProductComponent> result = mainRepository.getAllProductComponents(TestProductComponent.class);
        assertEquals(4, result.size());
        assertEquals(mainPc, result.get(0));
        assertEquals(inAPc, result.get(1));
        assertEquals(inBPc, result.get(2));
        assertEquals(basePc, result.get(3));

        result = inBetweenRepositoryA.getAllProductComponents(TestProductComponent.class);
        assertEquals(2, result.size());
        assertEquals(inAPc, result.get(0));
        assertEquals(basePc, result.get(1));

        result = baseRepository.getAllProductComponents(TestProductComponent.class);
        assertEquals(1, result.size());
        assertEquals(basePc, result.get(0));
    }

    public void testGetAllProductComponents() {
        List<IProductComponent> result = mainRepository.getAllProductComponents();
        assertEquals(4, result.size());
        assertEquals(mainPc, result.get(0));
        assertEquals(inAPc, result.get(1));
        assertEquals(inBPc, result.get(2));
        assertEquals(basePc, result.get(3));

        result = inBetweenRepositoryA.getAllProductComponents();
        assertEquals(2, result.size());
        assertEquals(inAPc, result.get(0));
        assertEquals(basePc, result.get(1));

        result = baseRepository.getAllProductComponents();
        assertEquals(1, result.size());
        assertEquals(basePc, result.get(0));
    }

    public void testGetAllProductComponentIds() {
        List<String> result = mainRepository.getAllProductComponentIds();
        assertEquals(4, result.size());
        assertEquals(mainPc.getId(), result.get(0));
        assertEquals(inAPc.getId(), result.get(1));
        assertEquals(inBPc.getId(), result.get(2));
        assertEquals(basePc.getId(), result.get(3));

        result = inBetweenRepositoryA.getAllProductComponentIds();
        assertEquals(2, result.size());
        assertEquals(inAPc.getId(), result.get(0));
        assertEquals(basePc.getId(), result.get(1));

        result = baseRepository.getAllProductComponentIds();
        assertEquals(1, result.size());
        assertEquals(basePc.getId(), result.get(0));
    }

    public void testGetProductComponentGenerations() {
        assertEquals(1, mainRepository.getProductComponentGenerations(mainPc).size());
        assertEquals(mainPcGen, mainRepository.getProductComponentGenerations(mainPc).get(0));
        assertEquals(basePcGen, mainRepository.getProductComponentGenerations(basePc).get(0));
    }

    public void testGetTable() {
        assertEquals(testTable, mainRepository.getTable(TestTable.class));
        assertEquals(testTable, baseRepository.getTable(TestTable.class));
    }

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
        }
    }

    public void testAddEnumValueLookup() {
        Lookup lookup = new Lookup();
        mainRepository.addEnumValueLookupService(lookup);
        assertEquals(lookup, mainRepository.getEnumValueLookupService(TestEnumValue.class));

        Lookup lookup2 = new Lookup();
        mainRepository.addEnumValueLookupService(lookup2);
        assertEquals(lookup2, mainRepository.getEnumValueLookupService(TestEnumValue.class));
    }

    public void testGetEnumValueLookup() {
        mainRepository.getEnumValueLookupService(TestEnumValue.class);

        Lookup lookup = new Lookup();
        mainRepository.addEnumValueLookupService(lookup);
        assertEquals(lookup, mainRepository.getEnumValueLookupService(TestEnumValue.class));

        Lookup lookup2 = new Lookup();
        mainRepository.addEnumValueLookupService(lookup2);
        assertEquals(lookup2, mainRepository.getEnumValueLookupService(TestEnumValue.class));
    }

    public void testRemoveEnumValueLookup() {
        Lookup lookup = new Lookup();
        mainRepository.removeEnumValueLookupService(lookup);

        mainRepository.addEnumValueLookupService(lookup);
        assertEquals(lookup, mainRepository.getEnumValueLookupService(TestEnumValue.class));
        mainRepository.removeEnumValueLookupService(lookup);
        assertNull(mainRepository.getEnumValueLookupService(TestEnumValue.class));
    }

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

        // test if list is unmodfiable
        try {
            values.add(new TestEnumValue("value3"));
            fail();
        } catch (UnsupportedOperationException e) {

        }

    }

    class TestTable implements ITable {

    }

    private class Lookup implements IEnumValueLookupService<TestEnumValue> {

        private TestEnumValue value1 = new TestEnumValue("value1");
        private TestEnumValue value2 = new TestEnumValue("value2");

        private List<TestEnumValue> values = new ArrayList<TestEnumValue>();

        public Lookup() {
            values.add(value1);
            values.add(value2);
        }

        public Class<TestEnumValue> getEnumTypeClass() {
            return TestEnumValue.class;
        }

        public TestEnumValue getEnumValue(Object id) {
            for (TestEnumValue value : values) {
                if (value.getEnumValueId().equals(id)) {
                    return value;
                }
            }
            return null;
        }

        public List<TestEnumValue> getEnumValues() {
            return values;
        }

        public XmlAdapter<?, TestEnumValue> getXmlAdapter() {
            return null;
        }

    }
}
