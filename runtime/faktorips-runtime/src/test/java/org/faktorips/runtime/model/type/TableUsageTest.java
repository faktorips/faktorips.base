/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.model.type;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.ITable;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.internal.ProductComponentGeneration;
import org.faktorips.runtime.internal.Table;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.annotation.IpsChangingOverTime;
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.faktorips.runtime.model.annotation.IpsTableStructure;
import org.faktorips.runtime.model.annotation.IpsTableUsage;
import org.faktorips.runtime.model.annotation.IpsTableUsages;
import org.faktorips.runtime.model.table.TableStructure;
import org.faktorips.runtime.model.table.TableStructureKind;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class TableUsageTest {

    @Mock
    private IRuntimeRepository repository;

    private final Calendar effectiveDate = new GregorianCalendar(1999, 1, 1);

    @Test
    public void testGetTableUsage() {
        Product product = new Product();
        ProductCmptType productCmptType = IpsModel.getProductCmptType(product);
        TableUsage table1 = productCmptType.getTableUsage("table1");
        TableUsage table2 = productCmptType.getTableUsage("table2");

        assertThat((FooTable)table1.getTable(product, null), is(product.TABLE1));
        assertThat((BarTable)table2.getTable(product, null), is(product.TABLE2));
    }

    @Test
    public void testGetTableUsage_CapitalizedName() {
        Product product = new Product();
        ProductCmptType productCmptType = IpsModel.getProductCmptType(product);
        TableUsage table1 = productCmptType.getTableUsage("Table1");
        TableUsage table2 = productCmptType.getTableUsage("Table2");

        assertThat((FooTable)table1.getTable(product, null), is(product.TABLE1));
        assertThat((BarTable)table2.getTable(product, null), is(product.TABLE2));
    }

    @Test
    public void testGetTableUsage_Child() {
        ChildProduct product = new ChildProduct();
        ProductCmptType productCmptType = IpsModel.getProductCmptType(product);
        TableUsage parentTableUsage1 = productCmptType.getTableUsage("table1");
        TableUsage parentTableUsage2 = productCmptType.getTableUsage("Table2");
        TableUsage childTableUsage = productCmptType.getTableUsage("table3");

        assertThat((FooTable)parentTableUsage1.getTable(product, null), is(product.TABLE1));
        assertThat((BarTable)parentTableUsage2.getTable(product, null), is(product.TABLE2));
        assertThat((FooTable)childTableUsage.getTable(product, null), is(product.TABLE3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetTableUsage_UnkownName() {
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Product.class);
        productCmptType.getTableUsage("unkown");
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testGetTableUsage_FromGeneration() {
        Product product = new Product();
        ProductGen productGen = new ProductGen(product);
        when(repository.getProductComponentGeneration("id", effectiveDate)).thenReturn(productGen);
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Product.class);

        assertThat(productCmptType.getTableUsage("tableGen").getTable(product, effectiveDate),
                is((ITable)productGen.TABLE_GEN));
    }

    @Test
    public void testGetTableUsage_FromGeneration_NoEffectiveDate() {
        Product product = new Product();
        ProductGen productGen = new ProductGen(product);
        when(repository.getLatestProductComponentGeneration(product)).thenReturn(productGen);
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Product.class);

        assertThat((BarTable)productCmptType.getTableUsage("tableGen").getTable(product, null),
                is(productGen.TABLE_GEN));
    }

    @Test
    public void testGetTableUsages() {
        ProductCmptType productCmptType = IpsModel.getProductCmptType(ChildProduct.class);
        List<String> tableNames = new ArrayList<>();
        for (TableUsage tableUsageModel : productCmptType.getTableUsages()) {
            tableNames.add(tableUsageModel.getName());
        }

        assertThat(tableNames, contains("table3", "table1", "table2", "multitable", "tableGen"));
    }

    @Test
    public void testGetTableStructures() {
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Product.class);

        List<TableStructure> tableStructures1 = productCmptType.getTableUsage("table1").getTableStructures();

        assertThat(tableStructures1.size(), is(1));
        assertThat(tableStructures1, hasItem(IpsModel.getTableStructure(FooTable.class)));

        List<TableStructure> tableStructures2 = productCmptType.getTableUsage("table2").getTableStructures();

        assertThat(tableStructures2, hasSize(1));
        assertThat(tableStructures2, hasItem(IpsModel.getTableStructure(BarTable.class)));
    }

    @Test
    public void testGetTableStructures_MultipleStructures() {
        TableUsage tableUsage = IpsModel.getProductCmptType(Product.class).getTableUsage("multitable");
        assertNotNull(tableUsage);

        List<TableStructure> tableStructures = tableUsage.getTableStructures();

        assertThat(tableStructures, hasSize(2));
        assertThat(tableStructures,
                hasItems(IpsModel.getTableStructure(BarTable.class), IpsModel.getTableStructure(FooTable.class)));
    }

    @Test
    public void testValidate_OptionalTablePresent() {
        MessageList messageList = new MessageList();
        IValidationContext validationContext = mock(IValidationContext.class);
        when(validationContext.getLocale()).thenReturn(Locale.ENGLISH);

        Calendar effectiveDate = new GregorianCalendar(1999, Calendar.FEBRUARY, 1);
        Product product = new Product();
        TableUsage tableUsage = IpsModel.getProductCmptType(product).getTableUsage("table1");

        tableUsage.validate(messageList, validationContext, product, effectiveDate);

        assertThat(messageList.isEmpty(), is(true));
    }

    @Test
    public void testValidate_OptionalTableNotSet() {
        MessageList messageList = new MessageList();
        IValidationContext validationContext = mock(IValidationContext.class);
        when(validationContext.getLocale()).thenReturn(Locale.ENGLISH);

        Calendar effectiveDate = new GregorianCalendar(1999, Calendar.FEBRUARY, 1);
        Product product = new Product();
        product.setTable1Name("");
        TableUsage tableUsage = IpsModel.getProductCmptType(product).getTableUsage("table1");

        tableUsage.validate(messageList, validationContext, product, effectiveDate);

        assertThat(messageList.isEmpty(), is(true));
    }

    @Test
    public void testValidate_RequiredTablePresent() {
        MessageList messageList = new MessageList();
        IValidationContext validationContext = mock(IValidationContext.class);
        when(validationContext.getLocale()).thenReturn(Locale.ENGLISH);

        Calendar effectiveDate = new GregorianCalendar(1999, Calendar.FEBRUARY, 1);
        ProductWithRequireUsageSet product = new ProductWithRequireUsageSet();
        TableUsage tableUsage = IpsModel.getProductCmptType(product).getTableUsage("table1");

        tableUsage.validate(messageList, validationContext, product, effectiveDate);

        assertThat(messageList.isEmpty(), is(true));
    }

    @Test
    public void testValidate_RequiredTableNotSet() {
        MessageList messageList = new MessageList();
        IValidationContext validationContext = mock(IValidationContext.class);
        when(validationContext.getLocale()).thenReturn(Locale.ENGLISH);

        Calendar effectiveDate = new GregorianCalendar(1999, Calendar.FEBRUARY, 1);
        ProductWithRequireUsageNotSet product = new ProductWithRequireUsageNotSet();
        ProductCmptType productCmptType = IpsModel.getProductCmptType(product);

        TableUsage tableUsage = productCmptType.getTableUsage("table1");

        tableUsage.validate(messageList, validationContext, product, effectiveDate);
        assertThat(messageList.getMessage(0).getCode(), is(TableUsage.MSGCODE_REQUIRED_TABLE_IS_NOT_SET));
        assertThat(messageList.getMessage(0).getText(), containsString("Label of tableUsage"));
        assertThat(messageList.getMessage(0).getText(), containsString("id"));
    }

    @Test
    public void testGetDeclaredTableUsages() {
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Product.class);
        List<String> tableNames = new ArrayList<>();
        for (TableUsage tableUsageModel : productCmptType.getDeclaredTableUsages()) {
            tableNames.add(tableUsageModel.getName());
        }

        assertThat(tableNames.get(0), is("table1"));
        assertThat(tableNames.get(1), is("table2"));
    }

    @Test
    public void testGetDeclaredTableUsage() {
        Product product = new Product();
        ProductCmptType productCmptType = IpsModel.getProductCmptType(product);
        TableUsage table1 = productCmptType.getDeclaredTableUsage("table1");
        TableUsage table2 = productCmptType.getDeclaredTableUsage("table2");

        assertThat((FooTable)table1.getTable(product, null), is(product.TABLE1));
        assertThat((BarTable)table2.getTable(product, null), is(product.TABLE2));
    }

    @Test
    public void testGetDeclaredTableUsage_CapitalizedName() {
        Product product = new Product();
        ProductCmptType productCmptType = IpsModel.getProductCmptType(product);
        TableUsage table1 = productCmptType.getDeclaredTableUsage("Table1");
        TableUsage table2 = productCmptType.getDeclaredTableUsage("Table2");

        assertThat((FooTable)table1.getTable(product, null), is(product.TABLE1));
        assertThat((BarTable)table2.getTable(product, null), is(product.TABLE2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDeclaredTableUsage_UnknownName() {
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Product.class);
        productCmptType.getDeclaredTableUsage("undefined");
    }

    @Test
    public void testHasDeclaredTableUsage() {
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Product.class);

        assertThat(productCmptType.hasDeclaredTableUsage("table1"), is(true));
        assertThat(productCmptType.hasDeclaredTableUsage("table2"), is(true));
        assertThat(productCmptType.hasDeclaredTableUsage("Table1"), is(true));
        assertThat(productCmptType.hasDeclaredTableUsage("Table2"), is(true));
        assertThat(productCmptType.hasDeclaredTableUsage("undefined"), is(false));
    }

    @Test
    public void testHasDeclaredTableUsage_Child() {
        ProductCmptType productCmptType = IpsModel.getProductCmptType(ChildProduct.class);

        assertThat(productCmptType.getTableUsage("table1"), is(not(nullValue())));
        assertThat(productCmptType.hasDeclaredTableUsage("table1"), is(false));
    }

    @Test
    public void testGetTable1Name() {
        Product product = new Product();
        ProductCmptType productCmptType = IpsModel.getProductCmptType(product);
        TableUsage tableUsage = productCmptType.getTableUsage("table1");
        assertThat(tableUsage, is(notNullValue()));
        assertThat(tableUsage.getTableName(product, effectiveDate), is("Foo"));
    }

    @Test
    public void testGetTable2Name() {
        Product product = new Product();
        ProductCmptType productCmptType = IpsModel.getProductCmptType(product);
        TableUsage tableUsage = productCmptType.getTableUsage("table2");

        assertThat(tableUsage, is(notNullValue()));
        assertThat(tableUsage.getTableName(product, effectiveDate), is("Bar"));
    }

    @Test
    public void testGetTable3Name() {
        ChildProduct product = new ChildProduct();
        ProductCmptType productCmptType = IpsModel.getProductCmptType(product);
        TableUsage tableUsage = productCmptType.getTableUsage("table3");

        assertThat(tableUsage, is(notNullValue()));
        assertThat(tableUsage.getTableName(product, effectiveDate), is("Foo"));
    }

    @Test
    public void testSetTable1Name() {
        Product product = new Product();
        ProductCmptType productCmptType = IpsModel.getProductCmptType(product);
        TableUsage tableUsage = productCmptType.getTableUsage("table1");

        assertThat(tableUsage, is(notNullValue()));
        tableUsage.setTableName("NotFoo", product, effectiveDate);
        assertThat(product.getTable1Name(), is("NotFoo"));
    }

    @Test
    public void testSetTable2Name() {
        Product product = new Product();
        ProductCmptType productCmptType = IpsModel.getProductCmptType(product);
        TableUsage tableUsage = productCmptType.getTableUsage("table2");

        assertThat(tableUsage, is(notNullValue()));
        tableUsage.setTableName("NotBar", product, effectiveDate);
        assertThat(product.getTable2Name(), is("NotBar"));
    }

    @Test
    public void testSetTable3Name() {
        ChildProduct product = new ChildProduct();
        ProductCmptType productCmptType = IpsModel.getProductCmptType(product);
        TableUsage tableUsage = productCmptType.getTableUsage("table3");

        assertThat(tableUsage, is(notNullValue()));
        tableUsage.setTableName("NotFoo", product, effectiveDate);
        assertThat(product.getTable3Name(), is("NotFoo"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetTableName_withGeneration_exception() {
        Product product = new Product();
        ProductGen gen = new ProductGen(product);
        TableUsage tableUsage = IpsModel.getProductCmptType(product).getTableUsage("tableGen");

        tableUsage.setTableName("SomeName", gen);
    }

    @Test
    public void testGetDocumentation() {
        ProductWithRequireUsageSet product = new ProductWithRequireUsageSet();
        TableUsage tableUsage = IpsModel.getProductCmptType(product).getTableUsage("table1");
        assertThat(tableUsage.getDescription(Locale.GERMAN), is("Description of tableUsage"));
        assertThat(tableUsage.getLabel(Locale.GERMAN), is("Label of tableUsage"));

        ProductWithRequireUsageNotSet productWithRequireUsageNotSet = new ProductWithRequireUsageNotSet();
        tableUsage = IpsModel.getProductCmptType(productWithRequireUsageNotSet).getTableUsage("table1");
        assertThat(tableUsage.getDescription(Locale.GERMAN), is("Description of tableUsage"));
        assertThat(tableUsage.getLabel(Locale.GERMAN), is("Label of tableUsage"));
    }

    @IpsTableStructure(name = "FooTable", type = TableStructureKind.MULTIPLE_CONTENTS, columns = {})
    private class FooTable extends Table<FooTableRow> {

        @Override
        public String getName() {
            return "Foo";
        }

        @Override
        protected void addRow(List<String> columns, IRuntimeRepository productRepository) {
            // not used
        }

        @Override
        protected void initKeyMaps() {
            // not used
        }

    }

    private static class FooTableRow {
        // an empty row
    }

    @IpsTableStructure(name = "BarTable", type = TableStructureKind.MULTIPLE_CONTENTS, columns = {})
    private static class BarTable extends Table<BarTableRow> {

        @Override
        public String getName() {
            return "Bar";
        }

        @Override
        protected void addRow(List<String> columns, IRuntimeRepository productRepository) {
            // not used
        }

        @Override
        protected void initKeyMaps() {
            // not used
        }

    }

    private static class BarTableRow {
        // another empty row
    }

    @IpsProductCmptType(name = "MyProduct")
    @IpsTableUsages({ "table1", "table2", "multitable", "tableGen" })
    @IpsChangingOverTime(ProductGen.class)
    private class Product extends ProductComponent {

        public final FooTable TABLE1 = new FooTable();
        public final BarTable TABLE2 = new BarTable();

        private String table1Name = "Foo";
        private String table2Name = "Bar";

        public Product() {
            super(repository, "id", "kindId", "versionId");
        }

        @IpsTableUsage(name = "table1", tableClasses = { FooTable.class })
        public FooTable getTable1() {
            return TABLE1;
        }

        @SuppressWarnings("unused")
        public String getTable1Name() {
            return table1Name;
        }

        @SuppressWarnings("unused")
        public void setTable1Name(String name) {
            table1Name = name;
        }

        @IpsTableUsage(name = "table2", tableClasses = { BarTable.class })
        public BarTable getTable2() {
            return TABLE2;
        }

        @SuppressWarnings("unused")
        public String getTable2Name() {
            return table2Name;
        }

        @SuppressWarnings("unused")
        public void setTable2Name(String name) {
            table2Name = name;
        }

        @IpsTableUsage(name = "multitable", tableClasses = { FooTable.class, BarTable.class })
        public ITable<?> getMultitable() {
            return null;
        }

        @SuppressWarnings("unused")
        public String getMultitableName() {
            return null;
        }

        @Override
        public IConfigurableModelObject createPolicyComponent() {
            // not used
            return null;
        }

        @Override
        public boolean isChangingOverTime() {
            return true;
        }
    }

    @IpsProductCmptType(name = "MyProduct")
    @IpsTableUsages({ "table1" })
    @IpsDocumented(bundleName = "org.faktorips.runtime.model.tableUsage.test", defaultLocale = "de")
    private class ProductWithRequireUsageNotSet extends ProductComponent {

        private String table1Name = null;

        public ProductWithRequireUsageNotSet() {
            super(repository, "id", "kindId", "versionId");
        }

        @IpsTableUsage(name = "table1", required = true, tableClasses = { FooTable.class })
        public FooTable getTable1() {
            return null;
        }

        @SuppressWarnings("unused")
        public String getTable1Name() {
            return table1Name;
        }

        @SuppressWarnings("unused")
        public void setTable1Name(String name) {
            table1Name = name;
        }

        @Override
        public IConfigurableModelObject createPolicyComponent() {
            // not used
            return null;
        }

        @Override
        public boolean isChangingOverTime() {
            return true;
        }
    }

    @IpsProductCmptType(name = "MyProduct")
    @IpsTableUsages({ "table1" })
    @IpsDocumented(bundleName = "org.faktorips.runtime.model.tableUsage.test", defaultLocale = "de")
    private class ProductWithRequireUsageSet extends ProductComponent {

        public final FooTable TABLE1 = new FooTable();

        private String table1Name = "Foo";

        public ProductWithRequireUsageSet() {
            super(repository, "id", "kindId", "versionId");
        }

        @IpsTableUsage(name = "table1", required = true, tableClasses = { FooTable.class })
        public FooTable getTable1() {
            return TABLE1;
        }

        @SuppressWarnings("unused")
        public String getTable1Name() {
            return table1Name;
        }

        @SuppressWarnings("unused")
        public void setTable1Name(String name) {
            table1Name = name;
        }

        @Override
        public IConfigurableModelObject createPolicyComponent() {
            // not used
            return null;
        }

        @Override
        public boolean isChangingOverTime() {
            return true;
        }
    }

    private class ProductGen extends ProductComponentGeneration {

        public final BarTable TABLE_GEN = new BarTable();

        private String tableGenName = "Bar";

        public ProductGen(Product product) {
            super(product);
        }

        @IpsTableUsage(name = "tableGen", tableClasses = { BarTable.class })
        public BarTable getTableGen() {
            return TABLE_GEN;
        }

        @SuppressWarnings("unused")
        public String getTableGenName() {
            return tableGenName;
        }
    }

    @IpsProductCmptType(name = "MyChildProduct")
    @IpsTableUsages({ "table3" })
    private class ChildProduct extends Product {

        public final FooTable TABLE3 = new FooTable();

        private String table3Name = "Foo";

        @IpsTableUsage(name = "table3", tableClasses = { FooTable.class })
        public FooTable getTable3() {
            return TABLE3;
        }

        @SuppressWarnings("unused")
        public String getTable3Name() {
            return table3Name;
        }

        @SuppressWarnings("unused")
        public void setTable3Name(String name) {
            table3Name = name;
        }
    }
}
