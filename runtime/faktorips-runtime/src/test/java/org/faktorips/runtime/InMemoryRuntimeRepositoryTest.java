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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.faktorips.runtime.DummyTocEntryFactory.DummyRuntimeObject;
import org.faktorips.runtime.internal.AbstractModelObject;
import org.faktorips.runtime.internal.DateTime;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.internal.ProductConfiguration;
import org.faktorips.runtime.internal.RuntimeObject;
import org.faktorips.runtime.internal.TestProductCmptGeneration;
import org.faktorips.runtime.internal.TestProductComponent;
import org.faktorips.runtime.internal.TestSingleContentTable;
import org.faktorips.runtime.internal.TestTable;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.annotation.IpsConfiguredBy;
import org.faktorips.runtime.model.annotation.IpsConfigures;
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.faktorips.runtime.model.annotation.IpsTableStructure;
import org.faktorips.runtime.model.table.TableStructureKind;
import org.faktorips.runtime.test.IpsFormulaTestCase;
import org.faktorips.runtime.test.IpsTest2;
import org.faktorips.runtime.test.IpsTestCase2;
import org.faktorips.runtime.test.IpsTestCaseBase;
import org.faktorips.runtime.test.IpsTestSuite;
import org.faktorips.runtime.test.MyFormulaTestCase;
import org.faktorips.runtime.testrepository.test.TestPremiumCalculation;
import org.faktorips.runtime.xml.IIpsXmlAdapter;
import org.faktorips.runtime.xml.IToXmlSupport;
import org.faktorips.values.DefaultInternationalString;
import org.faktorips.values.InternationalString;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

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
        List<TestEnumValue> values = new ArrayList<>();
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
    public void testRemoveEnumValues_EnumTypeExists() {
        List<TestEnumValue> values = new ArrayList<>();
        TestEnumValue value1 = new TestEnumValue("1");
        TestEnumValue value2 = new TestEnumValue("2");
        values.add(value1);
        values.add(value2);
        repository.putEnumValues(TestEnumValue.class, values, new InternationalString() {

            private static final long serialVersionUID = 1L;

            @Override
            public String get(Locale locale) {

                return "Test Description";
            }
        });

        List<TestEnumValue> values2 = repository.getEnumValues(TestEnumValue.class);
        assertEquals(2, values2.size());
        assertEquals(value1, values2.get(0));
        assertEquals(value2, values2.get(1));
        assertEquals("Test Description", repository.getEnumDescription(TestEnumValue.class).get(null));

        repository.removeEnumValues(TestEnumValue.class);
        values2 = repository.getEnumValues(TestEnumValue.class);
        InternationalString enumDescription = repository.getEnumDescription(TestEnumValue.class);
        assertTrue(values2.isEmpty());
        assertEquals(enumDescription, DefaultInternationalString.EMPTY);
    }

    @Test
    public void testRemoveEnumValues_EnumTypeDoesNotExist() {
        assertFalse(repository.removeEnumValues(TestEnumValue.class));
    }

    @Test
    public void testPutTable_multiContent() {
        TestTable t1 = new TestTable("my.MultiTable");
        repository.putTable(t1);

        TestTable t2 = new TestTable("my.AnotherMultiTable");
        Optional<ITable<?>> output = repository.putTable(t2);
        assertEquals(Optional.empty(), output);

        TestMultiContentTable2 t3 = new TestMultiContentTable2("my.MultiTable2");
        repository.putTable(t3);

        assertEquals(t1, repository.getTable("my.MultiTable"));
        assertEquals(t2, repository.getTable("my.AnotherMultiTable"));
        assertEquals(t3, repository.getTable("my.MultiTable2"));

        try {
            repository.putTable(null);
            fail();
        } catch (NullPointerException e) {
            // OK
        }
    }

    @Test
    public void testPutTable_singleContent() {
        TestSingleContentTable t1 = new TestSingleContentTable();
        repository.putTable(t1);
        assertEquals(t1, repository.getTable(TestSingleContentTable.class));

        // test if the returned table is the same as the old one
        TestSingleContentTable t2 = new TestSingleContentTable();
        Optional<ITable<?>> t_old = repository.putTable(t2);
        assertEquals(t1, t_old.get());
        assertEquals(t2, repository.getTable(TestSingleContentTable.class));

        // test if adding a subclass also removes the superclass instance
        // this is needed to give developers the possibility to mock tables.
        TestSingleContentTable2 t3 = new TestSingleContentTable2();
        repository.putTable(t3);
        assertEquals(t3, repository.getTable(TestSingleContentTable2.class));
        assertNotEquals(t2, repository.getTable(TestSingleContentTable.class));

        try {
            repository.putTable(null);
            fail();
        } catch (NullPointerException e) {
            // OK
        }
    }

    @Test
    public void testPutTable_qName_SingleContentTable() {
        TestSingleContentTable t1 = new TestSingleContentTable("motor.RateTable");
        repository.putTable(t1);
        assertEquals(t1, repository.getTable("motor.RateTable"));
        assertEquals(t1, repository.getTable(TestSingleContentTable.class));
    }

    @SuppressWarnings("removal")
    @Test
    public void testPutAndRemoveTable_Deprecated_SingleContentTable() {
        TestSingleContentTable t1 = new TestSingleContentTable("motor.RateTable");
        repository.putTable(t1, "motor.RateTable");
        assertEquals(t1, repository.getTable("motor.RateTable"));
        assertEquals(t1, repository.getTable(TestSingleContentTable.class));
        repository.removeTable(t1);
        assertNull(repository.getTable("motor.RateTable"));
        assertNull(repository.getTable(TestSingleContentTable.class));
    }

    @SuppressWarnings("removal")
    @Test
    public void testPutAndRemoveTable_Deprecated_MultiContentTable() {
        TestTable t1 = new TestTable("motor.RateTable");
        repository.putTable(t1, "motor.RateTable");
        assertEquals(t1, repository.getTable("motor.RateTable"));
        repository.removeTable(t1);
        assertNull(repository.getTable("motor.RateTable"));
    }

    @Test
    public void testGetTable_qName_MultiContentTable() {
        assertNull(repository.getTable("motor.RateTable"));
        TestTable t1 = new TestTable("motor.RateTable");
        repository.putTable(t1);
        assertEquals(t1, repository.getTable("motor.RateTable"));
    }

    @Test
    public void testGetTable_qName_SingleContentTable() {
        assertNull(repository.getTable("motor.RateTable"));
        TestSingleContentTable t1 = new TestSingleContentTable("motor.RateTable");
        repository.putTable(t1);
        assertEquals(t1, repository.getTable("motor.RateTable"));
    }

    @IpsTableStructure(name = "tables.TestTable", type = TableStructureKind.SINGLE_CONTENT, columns = { "company",
            "Gender", "rate" })
    public class TestSingleContentTable3 extends TestSingleContentTable {
        public TestSingleContentTable3(String qName) {
            super(qName);
            rows = new ArrayList<>();
            init();
        }
    }

    @IpsTableStructure(name = "tables.TestTable", type = TableStructureKind.SINGLE_CONTENT, columns = { "company",
            "Gender", "rate" })
    public class TestSingleContentTable4 extends TestSingleContentTable {
        public TestSingleContentTable4(String qName) {
            super(qName);
            rows = new ArrayList<>();
            init();
        }
    }

    @IpsTableStructure(name = "tables.TestTable", type = TableStructureKind.SINGLE_CONTENT, columns = { "company",
            "Gender", "rate" })
    public class TestSingleContentTable5 extends TestSingleContentTable {
        public TestSingleContentTable5(String qName) {
            super(qName);
            rows = new ArrayList<>();
            init();
        }
    }

    @IpsTableStructure(name = "tables.TestTable", type = TableStructureKind.SINGLE_CONTENT, columns = { "company",
            "Gender", "rate" })
    public class TestSingleContentTable6 extends TestSingleContentTable {
        public TestSingleContentTable6(String qName) {
            super(qName);
            rows = new ArrayList<>();
            init();
        }
    }

    @Test
    public void testGetAllTables_MultiAndSingleContent() {
        TestSingleContentTable3 t1 = new TestSingleContentTable3("your.ZSingleContent");
        repository.putTable(t1);
        TestTable t2 = new TestTable("my.MultiContent");
        repository.putTable(t2);
        TestTable t3 = new TestTable("my.MultiContentTwice");
        repository.putTable(t3);
        TestSingleContentTable4 t4 = new TestSingleContentTable4("my.aaSingleContent");
        repository.putTable(t4);
        TestTable t5 = new TestTable("my.FifthTable");
        repository.putTable(t5);

        assertThat(repository.getAllTables(), contains(t4, t5, t2, t3, t1));
    }

    @Test
    public void testGetAllTables_SingleContent() {
        // since all TestSingleContentTables here extend from TestSingleContentTable, we don't test
        // with TestSingleContentTable since putTable overwrites it
        TestSingleContentTable3 t3 = new TestSingleContentTable3("my.aComSingleContentTable");
        repository.putTable(t3);
        TestSingleContentTable4 t4 = new TestSingleContentTable4("my.BSingleContentTable");
        repository.putTable(t4);
        TestSingleContentTable5 t5 = new TestSingleContentTable5("my.aSingleContentTable");
        repository.putTable(t5);
        TestSingleContentTable6 t6 = new TestSingleContentTable6("a.aSingleContentTable");
        repository.putTable(t6);

        assertThat(repository.getAllTables(), contains(t6, t3, t5, t4));
    }

    @Test
    public void testGetAllTables_OverwriteSingleContent() {
        TestSingleContentTable t1 = new TestSingleContentTable("my.S1");
        repository.putTable(t1);
        TestSingleContentTable3 t2 = new TestSingleContentTable3("my.S2");
        repository.putTable(t2);

        assertThat(repository.getAllTables(), contains(t2));
    }

    @Test
    public void testGetAllTables_MultiContent() {
        TestTable t1 = new TestTable("my.Content");
        repository.putTable(t1);
        TestTable t2 = new TestTable("my.Content2");
        repository.putTable(t2);
        TestTable t3 = new TestTable("my.AContent");
        repository.putTable(t3);

        assertThat(repository.getAllTables(), contains(t3, t1, t2));
    }

    @Test
    public void testGetAllTables_EmptyAfterRemoval() {
        TestTable t1 = new TestTable("my.T1");
        repository.putTable(t1);
        TestSingleContentTable t2 = new TestSingleContentTable("my.T2");
        repository.putTable(t2);
        assertThat(repository.getAllTables(), contains(t1, t2));
        repository.removeTable(t1);
        repository.removeTable(t2);

        assertThat(repository.getAllTables(), empty());
    }

    @Test
    public void testGetAllTableIds_MultiAndSingleContent() {
        TestSingleContentTable3 t1 = new TestSingleContentTable3("your.ZSingleContent");
        repository.putTable(t1);
        TestTable t2 = new TestTable("my.MultiContent");
        repository.putTable(t2);
        TestTable t3 = new TestTable("my.MultiContentTwice");
        repository.putTable(t3);
        TestSingleContentTable4 t4 = new TestSingleContentTable4("my.aaSingleContent");
        repository.putTable(t4);
        TestTable t5 = new TestTable("my.FifthTable");
        repository.putTable(t5);

        String id1 = t1.getName();
        String id2 = t2.getName();
        String id3 = t3.getName();
        String id4 = t4.getName();
        String id5 = t5.getName();

        assertThat(repository.getAllTableIds(), contains(id4, id5, id2, id3, id1));
    }

    @Test
    public void testGetAllTableIds_SingleContent() {
        TestSingleContentTable3 t3 = new TestSingleContentTable3("my.aComSingleContentTable");
        repository.putTable(t3);
        TestSingleContentTable4 t4 = new TestSingleContentTable4("my.BSingleContentTable");
        repository.putTable(t4);
        TestSingleContentTable5 t5 = new TestSingleContentTable5("my.aSingleContentTable");
        repository.putTable(t5);
        TestSingleContentTable6 t6 = new TestSingleContentTable6("a.aSingleContentTable");
        repository.putTable(t6);

        String id3 = t3.getName();
        String id4 = t4.getName();
        String id5 = t5.getName();
        String id6 = t6.getName();

        assertThat(repository.getAllTableIds(), contains(id6, id3, id5, id4));
    }

    @Test
    public void testGetAllTableIds_OverwriteSingleContent() {
        TestSingleContentTable t1 = new TestSingleContentTable("my.S1");
        repository.putTable(t1);
        TestSingleContentTable3 t2 = new TestSingleContentTable3("my.S2");
        repository.putTable(t2);

        String id = t2.getName();

        assertThat(repository.getAllTableIds(), contains(id));
    }

    @Test
    public void testGetAllTableIds_MultiContent() {
        TestTable t1 = new TestTable("my.Content");
        repository.putTable(t1);
        TestTable t2 = new TestTable("my.Content2");
        repository.putTable(t2);
        TestTable t3 = new TestTable("my.AContent");
        repository.putTable(t3);

        String id1 = t1.getName();
        String id2 = t2.getName();
        String id3 = t3.getName();

        assertThat(repository.getAllTableIds(), contains(id3, id1, id2));
    }

    @Test
    public void testGetAllTableIds_AfterRemoval() {
        TestTable t1 = new TestTable("my.T1");
        repository.putTable(t1);
        TestSingleContentTable t2 = new TestSingleContentTable("my.T2");
        repository.putTable(t2);
        assertThat(repository.getAllTables(), contains(t1, t2));
        repository.removeTable(t1);
        repository.removeTable(t2);

        assertThat(repository.getAllTableIds(), empty());
    }

    @Test
    public void testGetAllTableIds_DuplicateNames() {
        TestTable t1 = new TestTable("my.T1");
        repository.putTable(t1);
        TestSingleContentTable t2 = new TestSingleContentTable("my.T1");
        repository.putTable(t2);

        String id = t1.getName();

        assertThat(repository.getAllTableIds(), contains(id));
    }

    @Test
    public void testGetTable_qName_WithNamelessSingleContentTable() {
        assertNull(repository.getTable("motor.RateTable"));
        TestSingleContentTable t1 = new TestSingleContentTable();
        TestTable t2 = new TestTable("motor.RateTable");
        repository.putTable(t1);
        repository.putTable(t2);
        assertEquals(t2, repository.getTable("motor.RateTable"));
    }

    @Test
    public void testGetTable_class_SingleContentTable() {
        assertNull(repository.getTable("motor.RateTable"));
        TestSingleContentTable t1 = new TestSingleContentTable("motor.RateTable");
        repository.putTable(t1);
        assertEquals(t1, repository.getTable(TestSingleContentTable.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetTable_class_MultiContentTable() {
        assertNull(repository.getTable("motor.RateTable"));
        TestTable t1 = new TestTable("motor.RateTable");
        repository.putTable(t1);
        repository.getTable(TestTable.class);

    }

    @Test(expected = NullPointerException.class)
    public void testRemoveTable_NullTable() {
        repository.removeTable(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveTable_NameIsBlank_BlankString() {
        TestTable t = new TestTable("");
        repository.removeTable(t);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveTable_NameIsBlank_WhiteSpaceOnly() {
        TestTable t = new TestTable(" ");
        repository.removeTable(t);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveTable_NameIsBlank_NullString() {
        String s = null;
        TestTable t = new TestTable(s);
        repository.removeTable(t);
    }

    @Test
    public void testRemoveTable_MultiContentTable() {
        TestTable t = new TestTable("testTable");
        repository.putTable(t);
        assertTrue(repository.removeTable(t));
        assertNull(repository.getTable("testTable"));
        assertFalse(repository.removeTable(t));

    }

    @Test
    public void testRemoveTable_SingleContentTable() {
        TestSingleContentTable t = new TestSingleContentTable();
        repository.putTable(t);
        assertTrue(repository.removeTable(t));
        assertNull(repository.getTable("testTable"));
        assertNull(repository.getTable(TestSingleContentTable.class));
        assertFalse(repository.removeTable(t));

    }

    @Test
    public void testGetAllModelTypeImplementationClasses() {
        ProductComponent p1 = new TestConfiguringProduct1(repository, "p1", "p1Kind", "p1Version");
        ProductComponent p2 = new TestConfiguringProduct2(repository, "p2", "p2Kind", "p2Version");
        TestSingleContentTable t1 = new TestSingleContentTable();
        repository.putTable(t1);
        repository.putProductComponent(p1);
        repository.putProductComponent(p2);

        TestEnumValue e1 = new TestEnumValue("enumTest");
        repository.putEnumValues(TestEnumValue.class, List.of(e1));

        Set<String> result = repository.getAllModelTypeImplementationClasses();

        assertThat(result, containsInAnyOrder(
                IpsModel.getProductCmptType(p1).getPolicyCmptType().getJavaClass().getName(),
                IpsModel.getProductCmptType(p2).getPolicyCmptType().getJavaClass().getName(),
                IpsModel.getProductCmptType(p1).getJavaClass().getName(),
                IpsModel.getProductCmptType(p2).getJavaClass().getName(),
                IpsModel.getProductCmptType(TestProductComponent.class).getJavaClass().getName(),
                e1.getClass().getName(),
                t1.getClass().getName()));
    }

    @Test
    public void testGetAllModelTypeImplementationClasses_withSubTypes() {
        Product p1 = new Product(repository, "p1", "p1Kind", "p1Version");
        repository.putProductComponent(p1);

        Set<String> result = repository.getAllModelTypeImplementationClasses();

        assertThat(result, containsInAnyOrder(
                IpsModel.getProductCmptType(p1).getPolicyCmptType().getJavaClass().getName(),
                IpsModel.getProductCmptType(p1).getJavaClass().getName(),
                IpsModel.getProductCmptType(p1).getPolicyCmptType().getSuperType().getJavaClass().getName(),
                IpsModel.getProductCmptType(p1).getSuperType().getJavaClass().getName(),
                IpsModel.getProductCmptType(TestProductComponent.class).getJavaClass().getName()));
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
    public void testPutProductComponentGenerationValidFromNull() {
        TestProductCmptGeneration gen1 = new TestProductCmptGeneration(a);

        repository.putProductCmptGeneration(gen1);
        repository.putProductCmptGeneration(gen1);
    }

    @Test(expected = NullPointerException.class)
    public void testPutProductComponentGenerationValidFromNullPointer() {
        TestProductCmptGeneration gen1 = new TestProductCmptGeneration(a);
        TestProductCmptGeneration gen2 = new TestProductCmptGeneration(a);

        repository.putProductCmptGeneration(gen1);
        repository.putProductCmptGeneration(gen2);
    }

    @Test
    public void testPutProductComponentGenerationSameValidFromDate() {
        GregorianCalendar date = new GregorianCalendar(2005, 0, 1);
        TestProductCmptGeneration gen1 = new TestProductCmptGeneration(a);
        gen1.setValidFrom(DateTime.createDateOnly(date));
        TestProductCmptGeneration gen2 = new TestProductCmptGeneration(a);
        gen2.setValidFrom(DateTime.createDateOnly(date));

        repository.putProductCmptGeneration(gen1);
        repository.putProductCmptGeneration(gen2);
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveProductComponent_Null() {
        repository.removeProductComponent(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveProductComponent_NullId() {
        repository.removeProductComponent(mock(IProductComponent.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveProductComponent_NoId() {
        repository.removeProductComponent(new TestProductComponent(repository, "", "", ""));
    }

    @Test
    public void testRemoveProductComponent() {
        TestProductComponent productComponent = new TestProductComponent(repository, "P1", "P", "1");
        repository.putProductComponent(productComponent);

        assertTrue(repository.removeProductComponent(productComponent));
        assertFalse(repository.removeProductComponent(productComponent));
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveProductCmptGeneration_Null() {
        repository.removeProductCmptGeneration(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveProductCmptGeneration_NullId() {
        IProductComponent productComponent = mock(IProductComponent.class);
        when(productComponent.getId()).thenReturn(null);

        IProductComponentGeneration generation = mock(IProductComponentGeneration.class);
        when(generation.getProductComponent()).thenReturn(productComponent);

        repository.removeProductCmptGeneration(generation);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveProductCmptGeneration_NoId() {
        IProductComponent productComponent = mock(IProductComponent.class);
        when(productComponent.getId()).thenReturn("");

        IProductComponentGeneration generation = mock(IProductComponentGeneration.class);
        when(generation.getProductComponent()).thenReturn(productComponent);

        repository.removeProductCmptGeneration(generation);
    }

    @Test
    public void testRemoveProductCmptGeneration() {
        TestProductComponent productComponent = new TestProductComponent(repository, "P1", "P", "1");

        IProductComponentGeneration generation = mock(IProductComponentGeneration.class);
        when(generation.getProductComponent()).thenReturn(productComponent);

        repository.putProductCmptGeneration(generation);

        assertTrue(repository.removeProductCmptGeneration(generation));
        assertFalse(repository.removeProductCmptGeneration(generation));
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

    @Test(expected = NullPointerException.class)
    public void testRemoveIpsTestCase_Null() {
        repository.removeIpsTestCase(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveIpsTestCase_EmptyQualifiedName() {
        IpsTestCaseBase testCase = mock(IpsTestCaseBase.class);
        when(testCase.getQualifiedName()).thenReturn("");
        repository.removeIpsTestCase(testCase);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveIpsTestCase_NullQualifiedName() {
        IpsTestCaseBase testCase = mock(IpsTestCaseBase.class);
        when(testCase.getQualifiedName()).thenReturn(null);
        repository.removeIpsTestCase(testCase);
    }

    @Test
    public void testRemoveIpsTestCase_NotFound() {
        IpsTestCaseBase testCase = mock(IpsTestCaseBase.class);
        when(testCase.getQualifiedName()).thenReturn("testCase1");

        assertFalse(repository.removeIpsTestCase(testCase));
    }

    @Test
    public void testRemoveIpsTestCase() {
        TestPremiumCalculation testPremiumCalculation = new TestPremiumCalculation("ipsTest");
        repository.putIpsTestCase(testPremiumCalculation);

        assertTrue(repository.removeIpsTestCase(testPremiumCalculation));
        assertFalse(repository.removeIpsTestCase(testPremiumCalculation));
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
        List<TestProductComponent> list = repository.getAllProductComponents(TestProductComponent.class);
        assertEquals(3, list.size());
        assertTrue(list.contains(a));
        assertTrue(list.contains(b));
        assertTrue(list.contains(c));
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

        c = new TestProductComponent(repository, "c", "cKind", "cVersion");
        c.setValidFrom(new DateTime(2006, 0, 1));
        repository.putProductComponent(c);

        assertThat(repository.getNumberOfProductComponentGenerations(a), is(3));
        assertThat(repository.getNumberOfProductComponentGenerations(b), is(1));
        assertThat(repository.getNumberOfProductComponentGenerations(c), is(0));
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

        IpsTestSuite ipsTestSuite = repository.getIpsTestSuite("pack");
        List<IpsTest2> tests = ipsTestSuite.getTests();
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
        IIpsXmlAdapter<?, TestEnumValue> xmlAdapter = new TestXmlAdapter();
        repository.addEnumXmlAdapter(xmlAdapter);
        List<IIpsXmlAdapter<?, ?>> xmlBindingSupport = repository.getAllInternalEnumXmlAdapters(repository);
        assertEquals(1, xmlBindingSupport.size());
        assertEquals(xmlAdapter, xmlBindingSupport.get(0));

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

    @IpsTableStructure(name = "tables.TestTable", type = TableStructureKind.SINGLE_CONTENT, columns = { "company",
            "Gender", "rate" })
    class TestSingleContentTable2 extends TestSingleContentTable {
        // another table class
    }

    @IpsTableStructure(name = "tables.TestTable", type = TableStructureKind.MULTIPLE_CONTENTS, columns = { "company",
            "Gender", "rate" })
    class TestMultiContentTable2 extends TestTable {
        // another table class
        public TestMultiContentTable2(String qName) {
            super(qName);
        }
    }

    private static class TestXmlAdapter implements IIpsXmlAdapter<String, TestEnumValue> {

        @Override
        public String marshal(TestEnumValue value) throws Exception {
            return value.getEnumValueId();
        }

        @Override
        public TestEnumValue unmarshal(String value) throws Exception {
            return new TestEnumValue(value);
        }

    }

    private static class TestEnumValue {

        private final String id;

        public TestEnumValue(String id) {
            this.id = id;
        }

        public String getEnumValueId() {
            return id;
        }
    }

    @Test
    public void testPutByType() {
        class MyRuntimeObject extends RuntimeObject {
            // another class
        }
        MyRuntimeObject myRuntimeObject = new MyRuntimeObject();
        String ipsObjectQualifiedName = "MyRuntimeObjectId";
        repository.putCustomRuntimeObject(MyRuntimeObject.class, ipsObjectQualifiedName, myRuntimeObject);
        assertEquals(myRuntimeObject, repository.getCustomRuntimeObject(MyRuntimeObject.class, ipsObjectQualifiedName));
    }

    @Test
    public void testRemoveCustomRuntimeObject_WhenObjectExists() {
        class MyRuntimeObject extends RuntimeObject {
            // another class
        }
        MyRuntimeObject myRuntimeObject = new MyRuntimeObject();
        String ipsObjectQualifiedName = "MyRuntimeObjectId";
        repository.putCustomRuntimeObject(MyRuntimeObject.class, ipsObjectQualifiedName, myRuntimeObject);
        assertEquals(myRuntimeObject, repository.getCustomRuntimeObject(MyRuntimeObject.class, ipsObjectQualifiedName));
        assertTrue(repository.removeCustomRuntimeObject(myRuntimeObject.getClass(), ipsObjectQualifiedName));
        assertNull(repository.getCustomRuntimeObject(MyRuntimeObject.class, ipsObjectQualifiedName));

    }

    @Test
    public void testRemoveCustomRuntimeObject_WhenObjectDoesNotExists_InvalidType() {
        class MyRuntimeObject extends RuntimeObject {
            // another class
        }
        MyRuntimeObject myRuntimeObject = new MyRuntimeObject();
        String ipsObjectQualifiedName = "MyRuntimeObjectId";
        assertFalse(repository.removeCustomRuntimeObject(myRuntimeObject.getClass(), ipsObjectQualifiedName));

    }

    @Test
    public void testRemoveCustomRuntimeObject_WhenObjectDoesNotExists_InvalidId() {
        class MyRuntimeObject extends RuntimeObject {
            // another class
        }
        MyRuntimeObject myRuntimeObject = new MyRuntimeObject();
        String ipsObjectQualifiedName = "MyRuntimeObjectId";
        repository.putCustomRuntimeObject(MyRuntimeObject.class, ipsObjectQualifiedName, myRuntimeObject);
        assertEquals(myRuntimeObject, repository.getCustomRuntimeObject(MyRuntimeObject.class, ipsObjectQualifiedName));
        assertFalse(repository.removeCustomRuntimeObject(myRuntimeObject.getClass(), "MyOtherRuntimeObjectId"));
    }

    @Test
    public void testGetByType() {
        repository.putCustomRuntimeObject(DummyRuntimeObject.class, "dummy.DummyRuntimeObject",
                new DummyRuntimeObject());
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

    @IpsProductCmptType(name = "TestConfiguringProduct_1")
    @IpsConfigures(TestConfiguredPolicy1.class)
    @IpsDocumented(bundleName = "org.faktorips.runtime.model.type.InMemoryRuntimeRepositoryTest", defaultLocale = "de")
    public class TestConfiguringProduct1 extends ProductComponent implements IToXmlSupport {

        public TestConfiguringProduct1(IRuntimeRepository repository, String id, String kindId, String versionId) {
            super(repository, id, kindId, versionId);
        }

        @Override
        public boolean isChangingOverTime() {
            return false;
        }

        public TestConfiguredPolicy1 createConfiguredPolicy() {
            return new TestConfiguredPolicy1(this);
        }

        @Override
        public TestConfiguredPolicy1 createPolicyComponent() {
            return createConfiguredPolicy();
        }

        @Override
        public void writePropertiesToXml(Element element) {
            // not implemented, but overwrites the super-implementation that throws an exception to
            // allow testing of other parts written to XML.

        }

    }

    @IpsProductCmptType(name = "TestConfiguringProduct_2")
    @IpsConfigures(TestConfiguredPolicy2.class)
    @IpsDocumented(bundleName = "org.faktorips.runtime.model.type.InMemoryRuntimeRepositoryTest", defaultLocale = "de")
    public class TestConfiguringProduct2 extends ProductComponent implements IToXmlSupport {

        public TestConfiguringProduct2(IRuntimeRepository repository, String id, String kindId, String versionId) {
            super(repository, id, kindId, versionId);
        }

        @Override
        public boolean isChangingOverTime() {
            return false;
        }

        public TestConfiguredPolicy2 createConfiguredPolicy() {
            return new TestConfiguredPolicy2(this);
        }

        @Override
        public TestConfiguredPolicy2 createPolicyComponent() {
            return createConfiguredPolicy();
        }

        @Override
        public void writePropertiesToXml(Element element) {
            // not implemented, but overwrites the super-implementation that throws an exception to
            // allow testing of other parts written to XML.

        }

    }

    @IpsPolicyCmptType(name = "TestConfiguredPolicy_1")
    @IpsConfiguredBy(TestConfiguringProduct1.class)
    @IpsDocumented(bundleName = "org.faktorips.runtime.model.type.InMemoryRuntimeRepository", defaultLocale = "de")
    public class TestConfiguredPolicy1 extends AbstractModelObject implements IConfigurableModelObject {

        private ProductConfiguration productConfiguration;

        public TestConfiguredPolicy1(TestConfiguringProduct1 productCmpt) {
            super();
            productConfiguration = new ProductConfiguration(productCmpt);
        }

        public TestConfiguringProduct1 getConfiguringProduct() {
            return (TestConfiguringProduct1)getProductComponent();
        }

        @Override
        public IProductComponent getProductComponent() {
            return productConfiguration.getProductComponent();
        }

        @Override
        public void setProductComponent(IProductComponent productComponent) {
            productConfiguration.setProductComponent(productComponent);
        }

        @Override
        public Calendar getEffectiveFromAsCalendar() {
            return null;
        }

        @Override
        public void initialize() {
            // initialize
        }

    }

    @IpsPolicyCmptType(name = "TestConfiguredPolicy_2")
    @IpsConfiguredBy(TestConfiguringProduct2.class)
    @IpsDocumented(bundleName = "org.faktorips.runtime.model.type.InMemoryRuntimeRepository", defaultLocale = "de")
    public class TestConfiguredPolicy2 extends AbstractModelObject implements IConfigurableModelObject {

        private ProductConfiguration productConfiguration;

        public TestConfiguredPolicy2(TestConfiguringProduct2 productCmpt) {
            super();
            productConfiguration = new ProductConfiguration(productCmpt);
        }

        public TestConfiguringProduct2 getConfiguringProduct() {
            return (TestConfiguringProduct2)getProductComponent();
        }

        @Override
        public IProductComponent getProductComponent() {
            return productConfiguration.getProductComponent();
        }

        @Override
        public void setProductComponent(IProductComponent productComponent) {
            productConfiguration.setProductComponent(productComponent);
        }

        @Override
        public Calendar getEffectiveFromAsCalendar() {
            return null;
        }

        @Override
        public void initialize() {
            // initialize
        }

    }

    @IpsPolicyCmptType(name = "Policy")
    @IpsConfiguredBy(Product.class)
    public class Policy extends AbstractPolicy {

        private Product product;

        public Policy(Product product) {
            this.product = product;
        }

        @Override
        public IProductComponent getProductComponent() {
            return product;
        }

        @Override
        public void setProductComponent(IProductComponent productComponent) {
            product = (Product)productComponent;
        }

        @Override
        public Calendar getEffectiveFromAsCalendar() {
            return null;
        }

        @Override
        public void initialize() {
            // initialize
        }

    }

    @IpsProductCmptType(name = "Product")
    @IpsConfigures(Policy.class)
    public class Product extends AbstractProduct {

        public Product(IRuntimeRepository repository, String id, String productKindId, String versionId) {
            super(repository, id, productKindId, versionId);
        }

        @Override
        public boolean isChangingOverTime() {
            return false;
        }

        @Override
        public IConfigurableModelObject createPolicyComponent() {
            return new Policy(this);
        }
    }

    @IpsPolicyCmptType(name = "AbstractPolicy")
    @IpsConfiguredBy(AbstractProduct.class)
    public abstract class AbstractPolicy extends AbstractModelObject implements IConfigurableModelObject {
        // empty
    }

    @IpsProductCmptType(name = "AbstractProduct")
    @IpsConfigures(AbstractPolicy.class)
    public abstract class AbstractProduct extends ProductComponent {

        public AbstractProduct(IRuntimeRepository repository, String id, String productKindId, String versionId) {
            super(repository, id, productKindId, versionId);
        }

    }
}
