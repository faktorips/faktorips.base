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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

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
import org.faktorips.runtime.xml.IIpsXmlAdapter;
import org.faktorips.sample.model.TestAbstractEnum;
import org.faktorips.sample.model.TestConcreteExtensibleEnum;
import org.faktorips.sample.model.TestConcreteJavaEnum;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class AbstractRuntimeRepositoryTest {

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

    private final ITable<?> testTable = new TestTable();
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
        assertThat(mainRepository.getDirectlyReferencedRepositories().size(), is(1));
        assertThat(mainRepository.getDirectlyReferencedRepositories().get(0), is(r1));

        mainRepository.addDirectlyReferencedRepository(r2);
        assertThat(mainRepository.getDirectlyReferencedRepositories().size(), is(2));
        assertThat(mainRepository.getDirectlyReferencedRepositories().get(0), is(r1));
        assertThat(mainRepository.getDirectlyReferencedRepositories().get(1), is(r2));
    }

    @Test
    public void testGetAllReferencedRepositories() {
        List<IRuntimeRepository> result = baseRepository.getAllReferencedRepositories();
        assertThat(result.size(), is(0));

        result = inBetweenRepositoryA.getAllReferencedRepositories();
        assertThat(result.size(), is(1));
        assertThat(result.get(0), is(baseRepository));

        result = mainRepository.getAllReferencedRepositories();
        assertThat(result.size(), is(3));
        assertThat(result.get(0), is(inBetweenRepositoryA));
        assertThat(result.get(1), is(inBetweenRepositoryB));
        assertThat(result.get(2), is(baseRepository));
    }

    @Test
    public void testGetProductComponent_ById() {
        assertThat(mainRepository.getProductComponent("mainPc"), is(mainPc));
        assertThat(mainRepository.getProductComponent("inAPc"), is(inAPc));
        assertThat(mainRepository.getProductComponent("inBPc"), is(inBPc));
        assertThat(mainRepository.getProductComponent("basePc"), is(basePc));

        assertThat(mainRepository.getProductComponent("unknown"), is(nullValue()));
    }

    @Test
    public void testGetExistingProductComponent_ById() {
        assertThat(mainRepository.getExistingProductComponent("mainPc"), is(mainPc));
        assertThat(mainRepository.getExistingProductComponent("inAPc"), is(inAPc));
        assertThat(mainRepository.getExistingProductComponent("inBPc"), is(inBPc));
        assertThat(mainRepository.getExistingProductComponent("basePc"), is(basePc));

        try {
            mainRepository.getExistingProductComponent("unknown");
            fail();
        } catch (ProductCmptNotFoundException e) {
            assertThat(e.getProductCmptId(), is("unknown"));
            assertThat(e.getRepositoryName(), is(mainRepository.getName()));
        }
    }

    @Test
    public void testGetProductComponentGeneration() {
        assertThat(mainRepository.getProductComponentGeneration("mainPc", effectiveDate), is(mainPcGen));
        assertThat(mainRepository.getProductComponentGeneration("inAPc", effectiveDate), is(inAPcGen));
        assertThat(mainRepository.getProductComponentGeneration("inBPc", effectiveDate), is(inBPcGen));
        assertThat(mainRepository.getProductComponentGeneration("basePc", effectiveDate), is(basePcGen));

        assertThat(mainRepository.getProductComponentGeneration("unknown", effectiveDate), is(nullValue()));
        assertThat(mainRepository.getProductComponentGeneration("mainPc", new GregorianCalendar(2000, 0, 1)),
                is(nullValue()));
    }

    @Test
    public void testGetExistingProductComponentGeneration() {
        assertThat(mainRepository.getExistingProductComponentGeneration("mainPc", effectiveDate), is(mainPcGen));
        assertThat(mainRepository.getExistingProductComponentGeneration("inAPc", effectiveDate), is(inAPcGen));
        assertThat(mainRepository.getExistingProductComponentGeneration("inBPc", effectiveDate), is(inBPcGen));
        assertThat(mainRepository.getExistingProductComponentGeneration("basePc", effectiveDate), is(basePcGen));

        try {
            mainRepository.getExistingProductComponentGeneration("unknown", effectiveDate);
            fail();
        } catch (ProductCmptGenerationNotFoundException e) {
            assertThat(e.getRepositoryName(), is(mainRepository.getName()));
            assertThat(e.getProductCmptId(), is("unknown"));
            assertThat(e.getEffetiveDate(), is(effectiveDate));
            assertThat(e.productCmptWasFound(), is(false));
        }

        try {
            mainRepository.getExistingProductComponentGeneration("mainPc", new GregorianCalendar(2000, 0, 1));
            fail();
        } catch (ProductCmptGenerationNotFoundException e) {
            assertThat(e.getRepositoryName(), is(mainRepository.getName()));
            assertThat(e.getProductCmptId(), is("mainPc"));
            assertThat(e.getEffetiveDate(), is(new GregorianCalendar(2000, 0, 1)));
            assertThat(e.productCmptWasFound(), is(true));
        }
    }

    @Test
    public void testGetProductComponent_ByKindIdVersionId() {
        assertThat(mainRepository.getProductComponent("mainKind", "mainVersion"), is(mainPc));
        assertThat(mainRepository.getProductComponent("inAKind", "inAVersion"), is(inAPc));
        assertThat(mainRepository.getProductComponent("inBKind", "inBVersion"), is(inBPc));
        assertThat(mainRepository.getProductComponent("baseKind", "baseVersion"), is(basePc));
    }

    @Test
    public void testGetAllProductComponents_ByKindId() {
        assertThat(mainRepository.getAllProductComponents("mainKind").size(), is(1));
        assertThat(mainRepository.getAllProductComponents("mainKind").get(0), is(mainPc));

        assertThat(mainRepository.getAllProductComponents("baseKind").size(), is(1));
        assertThat(mainRepository.getAllProductComponents("baseKind").get(0), is(basePc));
    }

    @Test
    public void testGetProductComponentGeneration_ByIdAndEffectiveDate() {
        assertThat(mainRepository.getProductComponentGeneration("mainPc", effectiveDate), is(mainPcGen));
        assertThat(mainRepository.getProductComponentGeneration("inAPc", effectiveDate), is(inAPcGen));
        assertThat(mainRepository.getProductComponentGeneration("inBPc", effectiveDate), is(inBPcGen));
        assertThat(mainRepository.getProductComponentGeneration("basePc", effectiveDate), is(basePcGen));
        // Tests with validTo
        assertThat(mainRepository.getProductComponentGeneration("validToPc", effectiveDate), is(validToPcGen));
        assertThat(mainRepository.getProductComponentGeneration("validToPc",
                validTo.toGregorianCalendar(effectiveDate.getTimeZone())), is(validToPcGen));
        GregorianCalendar tooLate = validTo.toGregorianCalendar(effectiveDate.getTimeZone());
        tooLate.add(Calendar.MILLISECOND, 1);
        assertThat(mainRepository.getProductComponentGeneration("validToPc", tooLate), is(nullValue()));
    }

    @Test
    public void testGetAllProductComponents_ByClass() {
        List<TestProductComponent> result = mainRepository.getAllProductComponents(TestProductComponent.class);
        assertThat(result.size(), is(5));
        assertThat(mainPc + " not exists", result.contains(mainPc), is(true));
        assertThat(validToPc + " not exists", result.contains(validToPc), is(true));
        assertThat(inAPc + " not exists", result.contains(inAPc), is(true));
        assertThat(inBPc + " not exists", result.contains(inBPc), is(true));
        assertThat(basePc + " not exists", result.contains(basePc), is(true));

        result = inBetweenRepositoryA.getAllProductComponents(TestProductComponent.class);
        assertThat(result.size(), is(2));
        assertThat(inAPc + " not exists", result.contains(inAPc), is(true));
        assertThat(basePc + " not exists", result.contains(basePc), is(true));

        result = baseRepository.getAllProductComponents(TestProductComponent.class);
        assertThat(result.size(), is(1));
        assertThat(result.get(0), is(basePc));
    }

    @Test
    public void testGetAllProductComponents() {
        List<IProductComponent> result = mainRepository.getAllProductComponents();
        assertThat(result.size(), is(5));
        assertThat(mainPc + " not exists", result.contains(mainPc), is(true));
        assertThat(validToPc + " not exists", result.contains(validToPc), is(true));
        assertThat(inAPc + " not exists", result.contains(inAPc), is(true));
        assertThat(inBPc + " not exists", result.contains(inBPc), is(true));
        assertThat(basePc + " not exists", result.contains(basePc), is(true));

        result = inBetweenRepositoryA.getAllProductComponents();
        assertThat(result.size(), is(2));
        assertThat(inAPc + " not exists", result.contains(inAPc), is(true));
        assertThat(basePc + " not exists", result.contains(basePc), is(true));

        result = baseRepository.getAllProductComponents();
        assertThat(result.size(), is(1));
        assertThat(result.get(0), is(basePc));
    }

    @Test
    public void testGetAllProductComponentIds() {
        List<String> result = mainRepository.getAllProductComponentIds();
        assertThat(result.size(), is(5));
        assertThat(mainPc + " not exists", result.contains(mainPc.getId()), is(true));
        assertThat(validToPc + " not exists", result.contains(validToPc.getId()), is(true));
        assertThat(inAPc + " not exists", result.contains(inAPc.getId()), is(true));
        assertThat(inBPc + " not exists", result.contains(inBPc.getId()), is(true));
        assertThat(basePc + " not exists", result.contains(basePc.getId()), is(true));

        result = inBetweenRepositoryA.getAllProductComponentIds();
        assertThat(result.size(), is(2));
        assertThat(inAPc + " not exists", result.contains(inAPc.getId()), is(true));
        assertThat(basePc + " not exists", result.contains(basePc.getId()), is(true));

        result = baseRepository.getAllProductComponentIds();
        assertThat(result.size(), is(1));
        assertThat(result.get(0), is(basePc.getId()));
    }

    @Test
    public void testGetProductComponentGenerations() {
        assertThat(mainRepository.getProductComponentGenerations(mainPc).size(), is(1));
        assertThat(mainRepository.getProductComponentGenerations(mainPc).get(0), is(mainPcGen));
        assertThat(mainRepository.getProductComponentGenerations(basePc).get(0), is(basePcGen));
    }

    @Test
    public void testGetTable() {
        assertThat(mainRepository.getTable(TestTable.class), is(testTable));
        assertThat(baseRepository.getTable(TestTable.class), is(testTable));
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
        assertThat(suite.getQualifiedName(), is("myPack"));

        suite = mainRepository.getIpsTestSuite("pack");
        List<IpsTest2> tests = suite.getTests();
        assertThat(tests.size(), is(5));
        assertThat(suite.getTest("Test1"), is(notNullValue()));
        assertThat(suite.getTest("Test2"), is(notNullValue()));
        assertThat(suite.getTest("a"), is(notNullValue()));
        assertThat(suite.getTest("b"), is(notNullValue()));
        assertThat(suite.getTest("x"), is(notNullValue()));

        IpsTestSuite suiteA = (IpsTestSuite)suite.getTest("a");
        tests = suiteA.getTests();
        assertThat(tests.size(), is(3));

        IpsTestSuite suiteX = (IpsTestSuite)suite.getTest("x");
        IpsTestSuite suiteY = (IpsTestSuite)suiteX.getTest("y");
        assertThat(suiteY.getTest("Test7"), is(notNullValue()));
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

        assertThat(mainRepository.getIpsTest("pack.Test1"), is(test1));
        IpsTestSuite suite = (IpsTestSuite)mainRepository.getIpsTest("pack");
        assertThat(suite.size(), is(3));

        suite = (IpsTestSuite)mainRepository.getIpsTest("unknown");
        assertThat(suite.size(), is(0));

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
        assertThat(mainRepository.getEnumValueLookupService(TestEnumValue.class), is(lookup));

        Lookup lookup2 = new Lookup();
        mainRepository.addEnumValueLookupService(lookup2);
        assertThat(mainRepository.getEnumValueLookupService(TestEnumValue.class), is(lookup2));
    }

    @Test
    public void testGetEnumValueLookup() {
        mainRepository.getEnumValueLookupService(TestEnumValue.class);

        Lookup lookup = new Lookup();
        mainRepository.addEnumValueLookupService(lookup);
        assertThat(mainRepository.getEnumValueLookupService(TestEnumValue.class), is(lookup));

        Lookup lookup2 = new Lookup();
        mainRepository.addEnumValueLookupService(lookup2);
        assertThat(mainRepository.getEnumValueLookupService(TestEnumValue.class), is(lookup2));
    }

    @Test
    public void testGetEnumValueLookup_Deep() {
        IRuntimeRepository runtimeRepository3 = spy(new InMemoryRuntimeRepository());
        IRuntimeRepository runtimeRepository2 = spy(new InMemoryRuntimeRepository());
        runtimeRepository2.addDirectlyReferencedRepository(runtimeRepository3);
        IRuntimeRepository runtimeRepository1 = spy(new InMemoryRuntimeRepository());
        runtimeRepository1.addDirectlyReferencedRepository(runtimeRepository2);
        inBetweenRepositoryA.addDirectlyReferencedRepository(runtimeRepository1);
        mainRepository.getEnumValueLookupService(TestEnumValue.class);
        verify(runtimeRepository3).getEnumValueLookupService(TestEnumValue.class);
    }

    @Test
    public void testRemoveEnumValueLookup() {
        Lookup lookup = new Lookup();
        mainRepository.removeEnumValueLookupService(lookup);

        mainRepository.addEnumValueLookupService(lookup);
        assertThat(mainRepository.getEnumValueLookupService(TestEnumValue.class), is(lookup));
        mainRepository.removeEnumValueLookupService(lookup);
        assertThat(mainRepository.getEnumValueLookupService(TestEnumValue.class), is(nullValue()));
    }

    @Test
    public void testGetEnumValueFromLookup() {
        Lookup lookup = new Lookup();
        assertThat(baseRepository.getEnumValue(TestEnumValue.class, lookup.value1.getEnumValueId()), is(nullValue()));

        baseRepository.addEnumValueLookupService(lookup);
        assertThat(baseRepository.getEnumValue(TestEnumValue.class, lookup.value1.getEnumValueId()), is(lookup.value1));
        assertThat(baseRepository.getEnumValue(TestEnumValue.class, lookup.value2.getEnumValueId()), is(lookup.value2));
        assertThat(baseRepository.getEnumValue(TestEnumValue.class, "unknownId"), is(nullValue()));
        assertThat(baseRepository.getEnumValue(null, null), is(nullValue()));
    }

    @Test
    public void testGetEnumValueFromReferencedLookup() {
        ConcreteLookup lookup = new ConcreteLookup();
        assertThat(baseRepository.getEnumValue(TestConcreteExtensibleEnum.class, lookup.extendedValue1.getId()),
                is(nullValue()));

        // test if the search through referenced repositories works
        baseRepository.addEnumValueLookupService(lookup);
        assertThat(mainRepository.getEnumValue(TestConcreteExtensibleEnum.class, lookup.extendedValue1.getId()),
                is(lookup.extendedValue1));
        assertThat(mainRepository.getEnumValue(TestConcreteExtensibleEnum.class,
                TestConcreteExtensibleEnum.CLASS_VALUE_2.getId()), is(TestConcreteExtensibleEnum.CLASS_VALUE_2));
        assertThat(mainRepository.getEnumValue(TestConcreteExtensibleEnum.class, "unknownId"), is(nullValue()));
        assertThat(mainRepository.getEnumValue(null, null), is(nullValue()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEnumValueWithoutLookup_WithAbstractBaseEnum_() {
        baseRepository.getEnumValue(TestAbstractEnum.class, TestConcreteExtensibleEnum.CLASS_VALUE_2.getId());
    }

    @Test
    public void testGetEnumValueWithoutLookup_ExtensibleEnum() {
        assertThat(baseRepository.getEnumValue(TestConcreteExtensibleEnum.class,
                TestConcreteExtensibleEnum.CLASS_VALUE_1.getId()),
                is(TestConcreteExtensibleEnum.CLASS_VALUE_1));
    }

    @Test
    public void testGetEnumValueWithoutLookup_JavaEnum() {
        assertThat(baseRepository.getEnumValue(TestConcreteJavaEnum.class, TestConcreteJavaEnum.JAVA_VALUE_1.getId()),
                is(TestConcreteJavaEnum.JAVA_VALUE_1));
    }

    @Test
    public void testGetEnumValueFromReferencedLookup_WithAbstractBaseEnum() {
        // test if the search through referenced repositories works
        AbstractLookup lookup = new AbstractLookup();
        baseRepository.addEnumValueLookupService(lookup);
        assertThat(mainRepository.getEnumValue(TestAbstractEnum.class, lookup.extendedValue1.getId()),
                is(lookup.extendedValue1));
        assertThat(
                mainRepository.getEnumValue(TestAbstractEnum.class, TestConcreteExtensibleEnum.CLASS_VALUE_2.getId()),
                is(TestConcreteExtensibleEnum.CLASS_VALUE_2));
        assertThat(mainRepository.getEnumValue(TestAbstractEnum.class, "unknownId"), is(nullValue()));
        assertThat(mainRepository.getEnumValue(null, null), is(nullValue()));
    }

    @Test
    public void testGetExistingEnumValueFromLookup() {
        Lookup lookup = new Lookup();
        baseRepository.addEnumValueLookupService(lookup);

        assertThat(baseRepository.getEnumValue(TestEnumValue.class, lookup.value1.getEnumValueId()), is(lookup.value1));
        assertThat(baseRepository.getEnumValue(TestEnumValue.class, lookup.value2.getEnumValueId()), is(lookup.value2));
        assertThat(baseRepository.getEnumValue(null, null), is(nullValue()));

        // test if the search through referenced repositories works
        assertThat(mainRepository.getEnumValue(TestEnumValue.class, lookup.value1.getEnumValueId()), is(lookup.value1));
        assertThat(mainRepository.getEnumValue(TestEnumValue.class, lookup.value2.getEnumValueId()), is(lookup.value2));
        assertThat(mainRepository.getEnumValue(null, null), is(nullValue()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetExistingEnumValueFromLookup_NoLookupService() {
        Lookup lookup = new Lookup();
        baseRepository.getExistingEnumValue(TestEnumValue.class, lookup.value1.getEnumValueId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetExistingEnumValueFromLookup_Missing() {
        Lookup lookup = new Lookup();
        baseRepository.addEnumValueLookupService(lookup);
        baseRepository.getExistingEnumValue(TestEnumValue.class, "unkownId");
    }

    @Test
    public void testGetEnumValuesFromLookup() {
        Lookup lookup = new Lookup();
        assertThat(baseRepository.getEnumValues(TestEnumValue.class).isEmpty(), is(true));

        baseRepository.addEnumValueLookupService(lookup);
        List<TestEnumValue> values = baseRepository.getEnumValues(TestEnumValue.class);
        assertThat(values.get(0), is(lookup.value1));
        assertThat(values.get(1), is(lookup.value2));

        values = mainRepository.getEnumValues(TestEnumValue.class);
        assertThat(values.get(0), is(lookup.value1));
        assertThat(values.get(1), is(lookup.value2));

        // test if list is unmodifiable
        try {
            values.add(new TestEnumValue("value3"));
            fail();
        } catch (UnsupportedOperationException e) {
            // OK
        }

    }

    class TestTable implements ITable<Void> {
        // test class
        @Override
        public String getName() {
            return "qualifiedName";
        }

        @Override
        public List<Void> getAllRows() {
            return List.of();
        }

        @Override
        public Element toXml(Document document) {
            return null;
        }
    }

    private static class Lookup implements IEnumValueLookupService<TestEnumValue> {

        private final TestEnumValue value1 = new TestEnumValue("value1");
        private final TestEnumValue value2 = new TestEnumValue("value2");

        private final List<TestEnumValue> values = new ArrayList<>();

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
        public IIpsXmlAdapter<?, TestEnumValue> getXmlAdapter() {
            return null;
        }

    }

    private static class AbstractLookup implements IEnumValueLookupService<TestAbstractEnum> {

        private final TestConcreteExtensibleEnum extendedValue1 = new TestConcreteExtensibleEnum(2, "E1",
                "Extended Value 1");
        private final TestConcreteExtensibleEnum extendedValue2 = new TestConcreteExtensibleEnum(3, "E2",
                "Extended Value 2");

        private final List<TestAbstractEnum> values = List.of(TestConcreteExtensibleEnum.CLASS_VALUE_1,
                TestConcreteExtensibleEnum.CLASS_VALUE_2, extendedValue1, extendedValue2);

        @Override
        public Class<TestAbstractEnum> getEnumTypeClass() {
            return TestAbstractEnum.class;
        }

        @Override
        public TestAbstractEnum getEnumValue(Object id) {
            for (TestAbstractEnum value : values) {
                if (value.getId().equals(id)) {
                    return value;
                }
            }
            return null;
        }

        @Override
        public List<TestAbstractEnum> getEnumValues() {
            return values;
        }

        @Override
        public IIpsXmlAdapter<?, TestAbstractEnum> getXmlAdapter() {
            return null;
        }

    }

    private static class ConcreteLookup implements IEnumValueLookupService<TestConcreteExtensibleEnum> {

        private final TestConcreteExtensibleEnum extendedValue1 = new TestConcreteExtensibleEnum(2, "E1",
                "Extended Value 1");
        private final TestConcreteExtensibleEnum extendedValue2 = new TestConcreteExtensibleEnum(3, "E2",
                "Extended Value 2");

        private final List<TestConcreteExtensibleEnum> values = List.of(TestConcreteExtensibleEnum.CLASS_VALUE_1,
                TestConcreteExtensibleEnum.CLASS_VALUE_2, extendedValue1, extendedValue2);

        @Override
        public Class<TestConcreteExtensibleEnum> getEnumTypeClass() {
            return TestConcreteExtensibleEnum.class;
        }

        @Override
        public TestConcreteExtensibleEnum getEnumValue(Object id) {
            for (TestConcreteExtensibleEnum value : values) {
                if (value.getId().equals(id)) {
                    return value;
                }
            }
            return null;
        }

        @Override
        public List<TestConcreteExtensibleEnum> getEnumValues() {
            return values;
        }

        @Override
        public IIpsXmlAdapter<?, TestConcreteExtensibleEnum> getXmlAdapter() {
            return null;
        }
    }

}
