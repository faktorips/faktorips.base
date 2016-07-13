package org.faktorips.runtime.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IProductComponentLink;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.DateTime;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.internal.TestTable;
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsEnum;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.faktorips.runtime.model.annotation.IpsPublishedInterface;
import org.faktorips.runtime.model.enumtype.EnumModel;
import org.faktorips.runtime.model.table.TableModel;
import org.faktorips.runtime.modeltype.IModelType;
import org.faktorips.runtime.modeltype.IPolicyModel;
import org.faktorips.runtime.modeltype.IProductModel;
import org.junit.Test;

public class ModelsTest {

    @Test
    public void testGetTableModel() {
        TableModel model = Models.getTableModel(TestTable.class);

        assertThat(model, is(notNullValue()));
        assertThat(model.getName(), is(equalTo("tables.TestTable")));
    }

    @Test
    public void testGetTableModel_isReturningCachedInstance() {
        TableModel model = Models.getTableModel(TestTable.class);

        assertThat(Models.getTableModel(TestTable.class), is(sameInstance(model)));
    }

    @Test
    public void testGetTableModel_byInstance() {
        TableModel model = Models.getTableModel(new TestTable());

        assertThat(model, is(notNullValue()));
        assertThat(model.getName(), is(equalTo("tables.TestTable")));
    }

    @Test
    public void testGetPolicyModel() {
        IPolicyModel model = Models.getPolicyModel(TestPolicy.class);

        assertThat(model, is(notNullValue()));
        assertThat(model.getName(), is(equalTo("MyPolicy")));
    }

    @Test
    public void testGetPolicyModel_byInstance() {
        IPolicyModel model = Models.getPolicyModel(new TestPolicy());

        assertThat(model, is(notNullValue()));
        assertThat(model.getName(), is(equalTo("MyPolicy")));
    }

    @Test
    public void testGetPolicyModel_isReturningCachedInstance() {
        IPolicyModel model = Models.getPolicyModel(new TestPolicy());

        assertThat(Models.getPolicyModel(TestPolicy.class), is(sameInstance(model)));
    }

    @Test
    public void testGetPolicyModel_isReturningCachedInstanceForInterfaceAndImplementation() {
        assertThat(Models.getPolicyModel(MyPolicy.class), is(sameInstance(Models.getPolicyModel(IMyPolicy.class))));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPolicyModel_noAnnotation() {
        Models.getPolicyModel(TestPolicyWithoutAnnotation.class);
    }

    @Test
    public void testGetProductModel() {
        IProductModel model = Models.getProductModel(TestProduct.class);

        assertThat(model, is(notNullValue()));
        assertThat(model.getName(), is(equalTo("MyProduct")));
    }

    @Test
    public void testGetProductModel_byInstance() {
        IProductModel model = Models.getProductModel(new TestProduct());

        assertThat(model, is(notNullValue()));
        assertThat(model.getName(), is(equalTo("MyProduct")));
    }

    @Test
    public void testGetProductModel_isReturningCachedInstance() {
        IProductModel model = Models.getProductModel(new TestProduct());

        assertThat(Models.getProductModel(TestProduct.class), is(sameInstance(model)));
    }

    @Test
    public void testGetProductModel_isReturningCachedInstanceForInterfaceAndImplementation() {
        assertThat(Models.getProductModel(MyProduct.class), is(sameInstance(Models.getProductModel(IMyProduct.class))));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetProductModel_noAnnotation() {
        Models.getProductModel(TestProductWithoutAnnotation.class);
    }

    @Test
    public void testGetModelType_policy() {
        IModelType model = Models.getModelType(TestPolicy.class);

        assertThat(model, is(instanceOf(IPolicyModel.class)));
    }

    @Test
    public void testGetModelType_product() {
        IModelType model = Models.getModelType(TestProduct.class);

        assertThat(model, is(instanceOf(IProductModel.class)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetModelType_noType() {
        Models.getModelType(String.class);
    }

    @Test
    public void testGetEnumModel() {
        EnumModel model = Models.getEnumModel(TestEnum.class);

        assertThat(model, is(notNullValue()));
        assertThat(model.getName(), is(equalTo("my.TestEnum")));
    }

    @Test
    public void testGetEnumModel_isReturningCachedInstance() {
        EnumModel model = Models.getEnumModel(TestEnum.class);

        assertThat(Models.getEnumModel(TestEnum.class), is(sameInstance(model)));
    }

    @Test
    public void testGetEnumModel_byInstance() {
        EnumModel model = Models.getEnumModel(new TestEnum());

        assertThat(model, is(notNullValue()));
        assertThat(model.getName(), is(equalTo("my.TestEnum")));
    }

    @IpsEnum(name = "my.TestEnum", attributeNames = {})
    private static class TestEnum {

    }

    @IpsProductCmptType(name = "MyProduct")
    private static class TestProduct implements IProductComponent {

        @Override
        public Set<String> getExtensionPropertyIds() {
            return null;
        }

        @Override
        public Object getExtensionPropertyValue(String propertyId) {
            return null;
        }

        @Override
        public IRuntimeRepository getRepository() {
            return null;
        }

        @Override
        public String getId() {
            return null;
        }

        @Override
        public String getKindId() {
            return null;
        }

        @Override
        public String getVersionId() {
            return null;
        }

        @Override
        public DateTime getValidFrom() {
            return null;
        }

        @Override
        public Date getValidFrom(TimeZone timeZone) {
            return null;
        }

        @Override
        public DateTime getValidTo() {
            return null;
        }

        @Override
        public IProductComponentGeneration getGenerationBase(Calendar effectiveDate) {
            return null;
        }

        @Override
        public IProductComponentGeneration getLatestProductComponentGeneration() {
            return null;
        }

        @Override
        public IConfigurableModelObject createPolicyComponent() {
            return null;
        }

        @Override
        public IProductComponentLink<? extends IProductComponent> getLink(String linkName, IProductComponent target) {
            return null;
        }

        @Override
        public List<IProductComponentLink<? extends IProductComponent>> getLinks() {
            return null;
        }

        @Override
        public boolean isChangingOverTime() {
            return false;
        }

    }

    private abstract static class TestProductWithoutAnnotation implements IProductComponent {
    }

    @IpsPolicyCmptType(name = "MyPolicy")
    private static class TestPolicy implements IModelObject {

        @Override
        public MessageList validate(IValidationContext context) {
            return null;
        }

    }

    private static class TestPolicyWithoutAnnotation implements IModelObject {
        @Override
        public MessageList validate(IValidationContext context) {
            return null;
        }
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
