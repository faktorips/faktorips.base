package org.faktorips.runtime.modeltype.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.ITable;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.internal.Table;
import org.faktorips.runtime.model.Models;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.faktorips.runtime.model.annotation.IpsTableStructure;
import org.faktorips.runtime.model.annotation.IpsTableUsage;
import org.faktorips.runtime.model.annotation.IpsTableUsages;
import org.faktorips.runtime.model.table.TableStructureType;
import org.faktorips.runtime.modeltype.IProductModel;
import org.faktorips.runtime.modeltype.ITableUsageModel;
import org.junit.Test;

public class TableUsageModelTest {

    @Test
    public void testGetTableUsage() throws Exception {
        IProductModel productModel = Models.getProductModel(Product.class);
        Product product = mock(Product.class, CALLS_REAL_METHODS);
        assertThat(productModel.getTableUsage("table1").getTable(product, null), is((ITable)Product.TABLE1));
        assertThat(productModel.getTableUsage("table2").getTable(product, null), is((ITable)Product.TABLE2));
    }

    @Test
    public void testGetDeclaredTableUsages() throws Exception {
        IProductModel productModel = Models.getProductModel(Product.class);
        List<String> tableNames = new ArrayList<String>();
        for (ITableUsageModel tableUsageModel : productModel.getDeclaredTableUsages()) {
            tableNames.add(tableUsageModel.getName());
        }
        assertThat(tableNames.get(0), is("table1"));
        assertThat(tableNames.get(1), is("table2"));
    }

    @Test
    public void testGetTableUsages() throws Exception {
        IProductModel productModel = Models.getProductModel(ChildProduct.class);
        List<String> tableNames = new ArrayList<String>();
        for (ITableUsageModel tableUsageModel : productModel.getTableUsages()) {
            tableNames.add(tableUsageModel.getName());
        }
        assertThat(tableNames.get(0), is("table3"));
        assertThat(tableNames.get(1), is("table1"));
        assertThat(tableNames.get(2), is("table2"));
    }

    @Test
    public void testGetTableModel() throws Exception {
        IProductModel productModel = Models.getProductModel(Product.class);
        assertThat(productModel.getTableUsage("table1").getTableModel(), is(Models.getTableModel(FooTable.class)));
        assertThat(productModel.getTableUsage("table2").getTableModel(), is(Models.getTableModel(BarTable.class)));
    }

    @IpsTableStructure(name = "FooTable", type = TableStructureType.MULTIPLE_CONTENTS, columns = {})
    public static class FooTable extends Table<FooTableRow> {

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

    public static class FooTableRow {
    }

    @IpsTableStructure(name = "BarTable", type = TableStructureType.MULTIPLE_CONTENTS, columns = {})
    public static class BarTable extends Table<BarTableRow> {

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

    public static class BarTableRow {
    }

    @IpsProductCmptType(name = "MyProduct")
    @IpsTableUsages({ "table1", "table2" })
    public static abstract class Product extends ProductComponent {

        public final static FooTable TABLE1 = new FooTable();
        public final static BarTable TABLE2 = new BarTable();

        public Product() {
            super(null, null, null, null);
        }

        @IpsTableUsage(name = "table1")
        public FooTable getTable1() {
            return TABLE1;
        }

        @IpsTableUsage(name = "table2")
        public BarTable getTable2() {
            return TABLE2;
        }

        @Override
        public IConfigurableModelObject createPolicyComponent() {
            // not used
            return null;
        }

        @Override
        public boolean isChangingOverTime() {
            // not used
            return false;
        }
    }

    @IpsProductCmptType(name = "MyChildProduct")
    @IpsTableUsages({ "table3" })
    public static abstract class ChildProduct extends Product {

        public final static FooTable TABLE3 = new FooTable();

        @IpsTableUsage(name = "table3")
        public FooTable getTable3() {
            return TABLE3;
        }
    }
}
