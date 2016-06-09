package org.faktorips.runtime.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.internal.TestTable;
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.faktorips.runtime.model.annotation.IpsPublishedInterface;
import org.faktorips.runtime.model.table.TableModel;
import org.faktorips.runtime.modeltype.IModelType;
import org.junit.Test;

public class ModelsTest {

    @Test
    public void testGetTableModel() {
        TableModel model = Models.getTableModel(TestTable.class);

        assertNotNull(model);
        assertEquals("tables.TestTable", model.getName());
    }

    @Test
    public void testGetTableModelByInstance() {
        TableModel model = Models.getTableModel(new TestTable());

        assertNotNull(model);
        assertEquals("tables.TestTable", model.getName());
    }

    @Test
    public void testGetModelType_onPublishedInterface() {
        IModelType model = Models.getModelType(IMyPolicy.class);

        assertNotNull(model);
        assertEquals("MyPolicy", model.getName());
    }

    @Test
    public void testGetModelType_onImplementingClass() {
        IModelType model = Models.getModelType(MyPolicy.class);

        assertNotNull(model);
        assertEquals("MyPolicy", model.getName());
    }

    @Test
    public void testGetPolicyModel_onPublishedInterface() {
        IModelType model = Models.getPolicyModel(IMyPolicy.class);

        assertNotNull(model);
        assertEquals("MyPolicy", model.getName());
    }

    @Test
    public void testGetPolicyModel_onImplementingClass() {
        IModelType model = Models.getPolicyModel(MyPolicy.class);

        assertNotNull(model);
        assertEquals("MyPolicy", model.getName());
    }

    @Test
    public void testGetProductModel_onPublishedInterface() {
        IModelType model = Models.getProductModel(IMyProduct.class);

        assertNotNull(model);
        assertEquals("MyProduct", model.getName());
    }

    @Test
    public void testGetProductModel_onImplementingClass() {
        IModelType model = Models.getProductModel(MyProduct.class);

        assertNotNull(model);
        assertEquals("MyProduct", model.getName());
    }

    @IpsPolicyCmptType(name = "MyPolicy")
    @IpsPublishedInterface(implementation = MyPolicy.class)
    @IpsDocumented(bundleName = "org.faktorips.runtime.model", defaultLocale = "de")
    private static interface IMyPolicy extends IModelObject {
    }

    private static class MyPolicy implements IMyPolicy {

        @Override
        public MessageList validate(IValidationContext context) {
            return null;
        }

    }

    @IpsProductCmptType(name = "MyProduct")
    @IpsPublishedInterface(implementation = MyProduct.class)
    @IpsDocumented(bundleName = "org.faktorips.runtime.model", defaultLocale = "de")
    private static interface IMyProduct extends IProductComponent {
    }

    private static class MyProduct extends ProductComponent implements IMyProduct {

        public MyProduct(IRuntimeRepository repository, String id, String productKindId, String versionId) {
            super(repository, id, productKindId, versionId);
        }

        @Override
        public IConfigurableModelObject createPolicyComponent() {
            return null;
        }

        @Override
        public boolean isChangingOverTime() {
            return false;
        }

    }

}
