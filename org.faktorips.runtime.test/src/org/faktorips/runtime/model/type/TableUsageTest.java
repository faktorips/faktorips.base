package org.faktorips.runtime.model.type;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.ITable;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.internal.ProductComponentGeneration;
import org.faktorips.runtime.internal.Table;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.annotation.IpsChangingOverTime;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.faktorips.runtime.model.annotation.IpsTableStructure;
import org.faktorips.runtime.model.annotation.IpsTableUsage;
import org.faktorips.runtime.model.annotation.IpsTableUsages;
import org.faktorips.runtime.model.table.TableStructureKind;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
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
        List<String> tableNames = new ArrayList<String>();
        for (TableUsage tableUsageModel : productCmptType.getTableUsages()) {
            tableNames.add(tableUsageModel.getName());
        }

        assertThat(tableNames.get(0), is("table3"));
        assertThat(tableNames.get(1), is("table1"));
        assertThat(tableNames.get(2), is("table2"));
    }

    @Test
    public void testGetTableStructure() {
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Product.class);

        assertThat(productCmptType.getTableUsage("table1").getTableStructure(),
                is(IpsModel.getTableStructure(FooTable.class)));
        assertThat(productCmptType.getTableUsage("table2").getTableStructure(),
                is(IpsModel.getTableStructure(BarTable.class)));
    }

    @Test
    public void testGetTableStructure_MultipleStructures() {
        TableUsage tableUsage = IpsModel.getProductCmptType(Product.class).getTableUsage("multitable");
        assertNotNull(tableUsage);
        try {
            tableUsage.getTableStructure();
            fail("Expected " + UnsupportedOperationException.class + " because there are multiple table structures");
        } catch (UnsupportedOperationException e) {
            // expected
        } catch (Throwable t) {
            fail("Expected " + UnsupportedOperationException.class
                    + " because there are multiple table structures, but got " + t);
        }
    }

    @Test
    public void testGetDeclaredTableUsages() {
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Product.class);
        List<String> tableNames = new ArrayList<String>();
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

    public void testHasDeclaredTableUsage() {
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Product.class);

        assertThat(productCmptType.hasDeclaredTableUsage("table1"), is(true));
        assertThat(productCmptType.hasDeclaredTableUsage("table2"), is(true));
        assertThat(productCmptType.hasDeclaredTableUsage("Table1"), is(true));
        assertThat(productCmptType.hasDeclaredTableUsage("Table2"), is(true));
        assertThat(productCmptType.hasDeclaredTableUsage("undefined"), is(false));
    }

    public void testHasDeclaredTableUsage_Child() {
        ProductCmptType productCmptType = IpsModel.getProductCmptType(ChildProduct.class);

        assertThat(productCmptType.getTableUsage("table1"), is(not(nullValue())));
        assertThat(productCmptType.hasDeclaredTableUsage("table1"), is(false));
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
    }

    @IpsProductCmptType(name = "MyProduct")
    @IpsTableUsages({ "table1", "table2", "multitable", "tableGen" })
    @IpsChangingOverTime(ProductGen.class)
    private class Product extends ProductComponent {

        public final FooTable TABLE1 = new FooTable();
        public final BarTable TABLE2 = new BarTable();

        public Product() {
            super(repository, "id", "kindId", "versionId");
        }

        @IpsTableUsage(name = "table1")
        public FooTable getTable1() {
            return TABLE1;
        }

        @IpsTableUsage(name = "table2")
        public BarTable getTable2() {
            return TABLE2;
        }

        @IpsTableUsage(name = "multitable")
        public ITable<?> getMultitable() {
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

    private class ProductGen extends ProductComponentGeneration {

        public final BarTable TABLE_GEN = new BarTable();

        public ProductGen(Product product) {
            super(product);
        }

        @IpsTableUsage(name = "tableGen")
        public BarTable getTableGen() {
            return TABLE_GEN;
        }
    }

    @IpsProductCmptType(name = "MyChildProduct")
    @IpsTableUsages({ "table3" })
    private class ChildProduct extends Product {

        public final FooTable TABLE3 = new FooTable();

        @IpsTableUsage(name = "table3")
        public FooTable getTable3() {
            return TABLE3;
        }
    }
}
