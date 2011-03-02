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

package org.faktorips.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.faktorips.runtime.internal.DateTime;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.internal.TestProductCmptGeneration;
import org.faktorips.runtime.internal.TestProductComponent;
import org.faktorips.runtime.internal.TestTable;
import org.faktorips.runtime.test.IpsFormulaTestCase;
import org.faktorips.runtime.test.IpsTest2;
import org.faktorips.runtime.test.IpsTestCase2;
import org.faktorips.runtime.test.IpsTestSuite;
import org.faktorips.runtime.test.MyFormulaTestCase;
import org.faktorips.runtime.testrepository.test.TestPremiumCalculation;
import org.junit.Before;
import org.junit.Test;

public class InMemoryRuntimeRepositoryTest {

    private InMemoryRuntimeRepository repository;
    private ProductComponent a;
    private ProductComponent b;
    private ProductComponent c;

    @Before
    public void setUp() throws Exception {
        repository = new InMemoryRuntimeRepository();
        a = new TestProductComponent(repository, "a", "aKind", "aVersion");
        b = new TestProductComponent(repository, "b", "bKind", "bVersion");
        c = new TestProductComponent(repository, "c", "cKind", "cVersion");
        repository.putProductComponent(a);
        repository.putProductComponent(b);
        repository.putProductComponent(c);
    }

    @Test
    public void testPutEnumValues() {
        List<TestEnumValue> values = new ArrayList<TestEnumValue>();
        TestEnumValue value1 = new TestEnumValue("1");
        TestEnumValue value2 = new TestEnumValue("2");
        values.add(value1);
        values.add(value2);
        repository.putEnumValues(TestEnumValue.class, values);

        List<TestEnumValue> values2 = repository.getEnumValues(TestEnumValue.class);
        assertEquals(2, values2.size());
        assertEquals(value1, values2.get(0));
        assertEquals(value2, values2.get(1));
    }

    @Test
    public void testPutTable() {
        TestTable t1 = new TestTable();
        repository.putTable(t1);
        assertEquals(t1, repository.getTable(TestTable.class));

        TestTable t2 = new TestTable();
        repository.putTable(t2);
        assertEquals(t2, repository.getTable(TestTable.class));

        // test if adding a subclass also removes the superclass instance
        // this is needed to give developers the possibility to mock tables.
        TestTable2 t3 = new TestTable2();
        repository.putTable(t3);
        assertEquals(t3, repository.getTable(TestTable2.class));

        try {
            repository.putTable(null);
            fail();
        } catch (NullPointerException e) {
            // OK
        }
    }

    @Test
    public void testPutTable_qName() {
        TestTable t1 = new TestTable();
        repository.putTable(t1, "motor.RateTable");
        assertEquals(t1, repository.getTable(TestTable.class));
        assertEquals(t1, repository.getTable("motor.RateTable"));
    }

    @Test
    public void testGetTable_qName() {
        assertNull(repository.getTable("motor.RateTable"));
        TestTable t1 = new TestTable();
        repository.putTable(t1, "motor.RateTable");
        assertEquals(t1, repository.getTable("motor.RateTable"));
    }

    @Test
    public void testGetProductComponent_id() {
        assertEquals(b, repository.getProductComponent("b"));
        assertNull(repository.getProductComponent("notExisting"));
    }

    @Test
    public void testGetProductComponent_KindId_VersionId() {
        TestProductComponent cmpt = new TestProductComponent(repository, "MotorProduct 2005-01", "MotorProduct",
                "2005-01");
        repository.putProductComponent(cmpt);
        assertEquals(cmpt, repository.getProductComponent("MotorProduct", "2005-01"));

        assertNull(repository.getProductComponent(null, "2005-01"));
        assertNull(repository.getProductComponent("unknown", "2005-01"));
    }

    @Test
    public void testGetAllProductComponents_KindId() {
        TestProductComponent cmpt0 = new TestProductComponent(repository, "MotorProduct 2005-01", "MotorProduct",
                "2005-01");
        TestProductComponent cmpt1 = new TestProductComponent(repository, "MotorProduct 2006-01", "MotorProduct",
                "2006-01");
        TestProductComponent cmpt2 = new TestProductComponent(repository, "HomeProduct 2006-01", "HomeProduct",
                "2006-01");
        repository.putProductComponent(cmpt0);
        repository.putProductComponent(cmpt1);
        repository.putProductComponent(cmpt2);
        List<IProductComponent> result = repository.getAllProductComponents("MotorProduct");
        assertEquals(2, result.size());
        assertTrue(result.contains(cmpt0));
        assertTrue(result.contains(cmpt1));

        result = repository.getAllProductComponents((String)null);
        assertEquals(0, result.size());

        result = repository.getAllProductComponents("unknownId");
        assertEquals(0, result.size());
    }

    @Test
    public void testPutProductComponentGeneration() {
        GregorianCalendar date = new GregorianCalendar(2005, 0, 1);
        TestProductCmptGeneration gen = new TestProductCmptGeneration(a);
        gen.setValidFrom(DateTime.createDateOnly(date));

        repository.putProductCmptGeneration(gen);
        assertEquals(gen, repository.getProductComponentGeneration("a", date));
        assertEquals(a, repository.getProductComponent("a"));
    }

    @Test
    public void testPutIpsTestCase() {
        TestPremiumCalculation testCase = new TestPremiumCalculation("ipsTest");
        repository.putIpsTestCase(testCase);
        assertEquals(testCase, repository.getIpsTest("ipsTest"));
        assertEquals(1, repository.getAllIpsTestCases(repository).size());

        IpsFormulaTestCase formulaTestCase = new MyFormulaTestCase("ipsFormulaTest");
        repository.putIpsTestCase(formulaTestCase);
        assertEquals(formulaTestCase, repository.getIpsTest("ipsFormulaTest"));
        assertEquals(2, repository.getAllIpsTestCases(repository).size());
    }

    @Test
    public void testGetProductComponentGeneration() {
        GregorianCalendar date0 = new GregorianCalendar(2005, 0, 1);

        GregorianCalendar date1 = new GregorianCalendar(2006, 0, 1);

        assertNull(repository.getProductComponentGeneration("notExisting", date0));

        TestProductCmptGeneration gen0 = new TestProductCmptGeneration(a);
        gen0.setValidFrom(DateTime.createDateOnly(date0));
        TestProductCmptGeneration gen1 = new TestProductCmptGeneration(a);
        gen1.setValidFrom(DateTime.createDateOnly(date1));

        repository.putProductCmptGeneration(gen0);
        repository.putProductCmptGeneration(gen1);

        assertNull(repository.getProductComponentGeneration("a", new GregorianCalendar(2000, 0, 1)));
        assertEquals(gen0, repository.getProductComponentGeneration("a", date0));
        assertEquals(gen0, repository.getProductComponentGeneration("a", new GregorianCalendar(2005, 10, 15)));
        assertEquals(gen1, repository.getProductComponentGeneration("a", date1));
        assertEquals(gen1, repository.getProductComponentGeneration("a", new GregorianCalendar(2010, 10, 15)));
    }

    @Test
    public void testGetAllProductComponents() {
        List<IProductComponent> list = repository.getAllProductComponents(TestProductComponent.class);
        assertEquals(3, list.size());
        assertTrue(list.contains(a));
        assertTrue(list.contains(b));
        assertTrue(list.contains(c));

        list = repository.getAllProductComponents(String.class);
        assertEquals(0, list.size());
    }

    @Test
    public void testGetProductComponentGenerations() {
        GregorianCalendar date = new GregorianCalendar(2005, 0, 1);
        TestProductCmptGeneration gen = new TestProductCmptGeneration(a);
        gen.setValidFrom(DateTime.createDateOnly(date));
        repository.putProductCmptGeneration(gen);

        List<IProductComponentGeneration> result = repository.getProductComponentGenerations(a);
        assertEquals(1, result.size());
        assertEquals(gen, result.get(0));

        TestProductCmptGeneration gen2 = new TestProductCmptGeneration(a);
        gen2.setValidFrom(DateTime.createDateOnly(new GregorianCalendar(2005, 1, 1)));
        repository.putProductCmptGeneration(gen2);

        result = repository.getProductComponentGenerations(a);
        assertEquals(2, result.size());
        assertEquals(gen, result.get(0));
        assertEquals(gen2, result.get(1));
    }

    @Test
    public void testGetNumberOfProductComponentGenerations() {
        repository = new InMemoryRuntimeRepository();
        a = new TestProductComponent(repository, "a", "aKind", "aVersion");
        TestProductCmptGeneration aGen1 = new TestProductCmptGeneration(a);
        aGen1.setValidFrom(new DateTime(2006, 0, 1));
        repository.putProductCmptGeneration(aGen1);
        TestProductCmptGeneration aGen2 = new TestProductCmptGeneration(a);
        aGen2.setValidFrom(new DateTime(2006, 1, 1));
        repository.putProductCmptGeneration(aGen2);
        TestProductCmptGeneration aGen3 = new TestProductCmptGeneration(a);
        aGen3.setValidFrom(new DateTime(2006, 2, 1));
        repository.putProductCmptGeneration(aGen3);

        b = new TestProductComponent(repository, "b", "bKind", "bVersion");
        TestProductCmptGeneration bGen1 = new TestProductCmptGeneration(b);
        bGen1.setValidFrom(new DateTime(2006, 0, 1));
        repository.putProductCmptGeneration(bGen1);

        int numberOfGens = repository.getNumberOfProductComponentGenerations(a);
        assertEquals(3, numberOfGens);

        numberOfGens = repository.getNumberOfProductComponentGenerations(b);
        assertEquals(1, numberOfGens);
    }

    @Test
    public void testGetNextProductComponentGeneration() {
        repository = new InMemoryRuntimeRepository();
        a = new TestProductComponent(repository, "a", "aKind", "aVersion");
        TestProductCmptGeneration aGen1 = new TestProductCmptGeneration(a);
        aGen1.setValidFrom(new DateTime(2006, 0, 1));
        repository.putProductCmptGeneration(aGen1);
        TestProductCmptGeneration aGen2 = new TestProductCmptGeneration(a);
        aGen2.setValidFrom(new DateTime(2006, 1, 1));
        repository.putProductCmptGeneration(aGen2);
        TestProductCmptGeneration aGen3 = new TestProductCmptGeneration(a);
        aGen3.setValidFrom(new DateTime(2006, 2, 1));
        repository.putProductCmptGeneration(aGen3);

        IProductComponentGeneration expectedGen = repository.getNextProductComponentGeneration(aGen1);
        assertEquals(aGen2, expectedGen);

        expectedGen = repository.getNextProductComponentGeneration(aGen2);
        assertEquals(aGen3, expectedGen);

        expectedGen = repository.getNextProductComponentGeneration(aGen3);
        assertNull(expectedGen);
    }

    // actually testing the AbstractRuntimeRepository
    @Test
    public void testGetNextProductComponentGenerationInReferencedRepository() {

        InMemoryRuntimeRepository subRepository1 = new InMemoryRuntimeRepository();
        a = new TestProductComponent(subRepository1, "a", "aKind", "aVersion");
        TestProductCmptGeneration aGen1 = new TestProductCmptGeneration(a);
        aGen1.setValidFrom(new DateTime(2006, 0, 1));
        subRepository1.putProductCmptGeneration(aGen1);
        TestProductCmptGeneration aGen2 = new TestProductCmptGeneration(a);
        aGen2.setValidFrom(new DateTime(2006, 1, 1));
        subRepository1.putProductCmptGeneration(aGen2);
        TestProductCmptGeneration aGen3 = new TestProductCmptGeneration(a);
        aGen3.setValidFrom(new DateTime(2006, 2, 1));
        subRepository1.putProductCmptGeneration(aGen3);

        InMemoryRuntimeRepository subRepository2 = new InMemoryRuntimeRepository();
        a = new TestProductComponent(subRepository2, "b", "bKind", "bVersion");
        TestProductCmptGeneration rep2bGen1 = new TestProductCmptGeneration(a);
        rep2bGen1.setValidFrom(new DateTime(2006, 0, 1));
        subRepository2.putProductCmptGeneration(rep2bGen1);
        TestProductCmptGeneration rep2bGen2 = new TestProductCmptGeneration(a);
        rep2bGen2.setValidFrom(new DateTime(2006, 1, 1));
        subRepository2.putProductCmptGeneration(rep2bGen2);
        TestProductCmptGeneration rep2bGen3 = new TestProductCmptGeneration(a);
        rep2bGen3.setValidFrom(new DateTime(2006, 2, 1));
        subRepository2.putProductCmptGeneration(rep2bGen3);

        repository = new InMemoryRuntimeRepository();
        repository.addDirectlyReferencedRepository(subRepository1);
        repository.addDirectlyReferencedRepository(subRepository2);

        IProductComponentGeneration expectedGen = repository.getPreviousProductComponentGeneration(aGen3);
        assertEquals(aGen2, expectedGen);

        expectedGen = repository.getPreviousProductComponentGeneration(aGen2);
        assertEquals(aGen1, expectedGen);

        expectedGen = repository.getPreviousProductComponentGeneration(aGen1);
        assertNull(expectedGen);

        expectedGen = repository.getPreviousProductComponentGeneration(rep2bGen3);
        assertEquals(rep2bGen2, expectedGen);

        expectedGen = repository.getPreviousProductComponentGeneration(rep2bGen2);
        assertEquals(rep2bGen1, expectedGen);

        expectedGen = repository.getPreviousProductComponentGeneration(rep2bGen1);
        assertNull(expectedGen);
    }

    @Test
    public void testGetPreviousProductComponentGeneration() {
        repository = new InMemoryRuntimeRepository();
        a = new TestProductComponent(repository, "a", "aKind", "aVersion");
        TestProductCmptGeneration aGen1 = new TestProductCmptGeneration(a);
        aGen1.setValidFrom(new DateTime(2006, 0, 1));
        repository.putProductCmptGeneration(aGen1);
        TestProductCmptGeneration aGen2 = new TestProductCmptGeneration(a);
        aGen2.setValidFrom(new DateTime(2006, 1, 1));
        repository.putProductCmptGeneration(aGen2);
        TestProductCmptGeneration aGen3 = new TestProductCmptGeneration(a);
        aGen3.setValidFrom(new DateTime(2006, 2, 1));
        repository.putProductCmptGeneration(aGen3);

        IProductComponentGeneration expectedGen = repository.getPreviousProductComponentGeneration(aGen3);
        assertEquals(aGen2, expectedGen);

        expectedGen = repository.getPreviousProductComponentGeneration(aGen2);
        assertEquals(aGen1, expectedGen);

        expectedGen = repository.getPreviousProductComponentGeneration(aGen1);
        assertNull(expectedGen);
    }

    @Test
    public void testGetLatestProductComponentGeneration() {
        repository = new InMemoryRuntimeRepository();
        a = new TestProductComponent(repository, "a", "aKind", "aVersion");
        TestProductCmptGeneration aGen1 = new TestProductCmptGeneration(a);
        aGen1.setValidFrom(new DateTime(2006, 0, 1));
        repository.putProductCmptGeneration(aGen1);
        TestProductCmptGeneration aGen2 = new TestProductCmptGeneration(a);
        aGen2.setValidFrom(new DateTime(2006, 1, 1));
        repository.putProductCmptGeneration(aGen2);
        TestProductCmptGeneration aGen3 = new TestProductCmptGeneration(a);
        aGen3.setValidFrom(new DateTime(2006, 2, 1));
        repository.putProductCmptGeneration(aGen3);

        assertEquals(aGen3, repository.getLatestProductComponentGeneration(a));

        try {
            repository.getLatestProductComponentGeneration(null);
            fail();
        } catch (NullPointerException e) {
            // OK
        }
    }

    // actually testing the AbstractRuntimeRepository
    @Test
    public void testGetPreviousProductComponentGenerationInReferencedRepository() {

        InMemoryRuntimeRepository subRepository1 = new InMemoryRuntimeRepository();
        a = new TestProductComponent(subRepository1, "a", "aKind", "aVersion");
        TestProductCmptGeneration aGen1 = new TestProductCmptGeneration(a);
        aGen1.setValidFrom(new DateTime(2006, 0, 1));
        subRepository1.putProductCmptGeneration(aGen1);
        TestProductCmptGeneration aGen2 = new TestProductCmptGeneration(a);
        aGen2.setValidFrom(new DateTime(2006, 1, 1));
        subRepository1.putProductCmptGeneration(aGen2);
        TestProductCmptGeneration aGen3 = new TestProductCmptGeneration(a);
        aGen3.setValidFrom(new DateTime(2006, 2, 1));
        subRepository1.putProductCmptGeneration(aGen3);

        InMemoryRuntimeRepository subRepository2 = new InMemoryRuntimeRepository();
        a = new TestProductComponent(subRepository2, "b", "bKind", "bVersion");
        TestProductCmptGeneration rep2bGen1 = new TestProductCmptGeneration(a);
        rep2bGen1.setValidFrom(new DateTime(2006, 0, 1));
        subRepository2.putProductCmptGeneration(rep2bGen1);
        TestProductCmptGeneration rep2bGen2 = new TestProductCmptGeneration(a);
        rep2bGen2.setValidFrom(new DateTime(2006, 1, 1));
        subRepository2.putProductCmptGeneration(rep2bGen2);
        TestProductCmptGeneration rep2bGen3 = new TestProductCmptGeneration(a);
        rep2bGen3.setValidFrom(new DateTime(2006, 2, 1));
        subRepository2.putProductCmptGeneration(rep2bGen3);

        repository = new InMemoryRuntimeRepository();
        repository.addDirectlyReferencedRepository(subRepository1);
        repository.addDirectlyReferencedRepository(subRepository2);

        IProductComponentGeneration expectedGen = repository.getPreviousProductComponentGeneration(aGen3);
        assertEquals(aGen2, expectedGen);

        expectedGen = repository.getPreviousProductComponentGeneration(aGen2);
        assertEquals(aGen1, expectedGen);

        expectedGen = repository.getPreviousProductComponentGeneration(aGen1);
        assertNull(expectedGen);

        expectedGen = repository.getPreviousProductComponentGeneration(rep2bGen3);
        assertEquals(rep2bGen2, expectedGen);

        expectedGen = repository.getPreviousProductComponentGeneration(rep2bGen2);
        assertEquals(rep2bGen1, expectedGen);

        expectedGen = repository.getPreviousProductComponentGeneration(rep2bGen1);
        assertNull(expectedGen);
    }

    @Test
    public void testGetIpsTestSuite() {
        IpsTestCase2 testCase1 = putIpsTestCase("pack.ipsTest1");
        IpsTestCase2 testCase2 = putIpsTestCase("pack.ipsTest2");
        IpsTestCase2 testCase3 = putIpsTestCase("pack.ipsTest3");
        IpsTestCase2 testCase4 = putIpsTestCase("test.pack.ipsTest1");
        IpsTestCase2 testCase5 = putIpsTestCase("test.pack.ipsTest2");
        IpsTestCase2 testCase6 = putIpsTestCase("test.pack.ipsTest3");

        IpsTestSuite ipsTestSuite = null;
        List<IpsTest2> tests = null;

        ipsTestSuite = repository.getIpsTestSuite("pack");
        tests = ipsTestSuite.getTests();
        assertTrue(tests.contains(testCase1));
        assertTrue(tests.contains(testCase2));
        assertTrue(tests.contains(testCase3));

        ipsTestSuite = repository.getIpsTestSuite("pack.ips");
        tests = ipsTestSuite.getTests();
        assertTrue(tests.contains(testCase1));
        assertTrue(tests.contains(testCase2));
        assertTrue(tests.contains(testCase3));

        ipsTestSuite = repository.getIpsTestSuite("");
        tests = ipsTestSuite.getTests();
        assertEquals(2, tests.size());
        IpsTestSuite ipsTestSuiteA = (IpsTestSuite)tests.get(0);
        assertEquals("pack", ipsTestSuiteA.getName());
        List<IpsTest2> subTest = ipsTestSuiteA.getTests();
        assertTrue(subTest.contains(testCase1));
        assertTrue(subTest.contains(testCase2));
        assertTrue(subTest.contains(testCase3));
        IpsTestSuite ipsTestSuiteB = (IpsTestSuite)tests.get(1);
        assertEquals("test", ipsTestSuiteB.getName());
        tests = ipsTestSuiteB.getTests();
        assertEquals(1, tests.size());
        IpsTestSuite ipsTestSuiteB1 = (IpsTestSuite)tests.get(0);
        assertEquals("pack", ipsTestSuiteB1.getName());
        tests = ipsTestSuiteB1.getTests();
        assertTrue(tests.contains(testCase4));
        assertTrue(tests.contains(testCase5));
        assertTrue(tests.contains(testCase6));
    }

    @Test
    public void testGetIpsTestCaseStartingWith() {
        IpsTestCase2 testCase1 = putIpsTestCase("pack.ipsTest1");
        IpsTestCase2 testCase2 = putIpsTestCase("pack.ipsTest2");
        IpsTestCase2 testCase3 = putIpsTestCase("pack.ipsTest3");
        IpsTestCase2 testCase4 = putIpsTestCase("pack2.ipsTest1");
        IpsTestCase2 testCase5 = putIpsTestCase("pack2.ipsTest2");
        IpsTestCase2 testCase6 = putIpsTestCase("pack2.ipsTest3");
        IpsTestCase2 testCase7 = putIpsTestCase("ipsTest1");

        assertIpsTestCasesStartingWith("pack.", new IpsTestCase2[] { testCase1, testCase2, testCase3 });
        assertIpsTestCasesStartingWith("pack", new IpsTestCase2[] { testCase1, testCase2, testCase3, testCase4,
                testCase5, testCase6 });
        assertIpsTestCasesStartingWith("ipsTest1", new IpsTestCase2[] { testCase7 });
    }

    @Test
    public void testAddEnumXmlAdapter() {
        XmlAdapter<?, TestEnumValue> xmlAdapter = new TestXmlAdapter();
        repository.addEnumXmlAdapter(xmlAdapter);
        List<XmlAdapter<?, ?>> xmlAdapterList = repository.getAllInternalEnumXmlAdapters(repository);
        assertEquals(1, xmlAdapterList.size());
        assertEquals(xmlAdapter, xmlAdapterList.get(0));

    }

    private void assertIpsTestCasesStartingWith(String qNamePrefix, IpsTestCase2[] testCasesExpected) {
        List<IpsTest2> result = repository.getIpsTestCasesStartingWith(qNamePrefix, repository);
        assertEquals("Unexpected number of test cases", testCasesExpected.length, result.size());
        for (IpsTestCase2 element : testCasesExpected) {
            assertTrue("Missing test case: " + element, result.contains(element));
        }
    }

    private IpsTestCase2 putIpsTestCase(String qName) {
        TestPremiumCalculation testCase = new TestPremiumCalculation(qName);
        repository.putIpsTestCase(testCase);
        return testCase;
    }

    class TestTable2 extends TestTable {

    }

    private class TestXmlAdapter extends XmlAdapter<String, TestEnumValue> {

        @Override
        public String marshal(TestEnumValue value) throws Exception {
            return value.getEnumValueId();
        }

        @Override
        public TestEnumValue unmarshal(String value) throws Exception {
            return new TestEnumValue(value);
        }

    }

    private class TestEnumValue {

        private final String id;

        public TestEnumValue(String id) {
            this.id = id;
        }

        public String getEnumValueId() {
            return id;
        }
    }

}
