/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
import org.faktorips.runtime.model.annotation.IpsEnumType;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.faktorips.runtime.model.annotation.IpsPublishedInterface;
import org.faktorips.runtime.model.enumtype.EnumType;
import org.faktorips.runtime.model.table.TableStructure;
import org.faktorips.runtime.model.type.PolicyCmptType;
import org.faktorips.runtime.model.type.ProductCmptType;
import org.faktorips.runtime.model.type.Type;
import org.junit.Test;

public class IpsModelTest {

    @Test
    public void testGetTableStructure() {
        TableStructure model = IpsModel.getTableStructure(TestTable.class);

        assertThat(model, is(notNullValue()));
        assertThat(model.getName(), is(equalTo("tables.TestTable")));
    }

    @Test
    public void testGetTableStructure_isReturningCachedInstance() {
        TableStructure model = IpsModel.getTableStructure(TestTable.class);

        assertThat(IpsModel.getTableStructure(TestTable.class), is(sameInstance(model)));
    }

    @Test
    public void testGetTableStructure_byInstance() {
        TableStructure model = IpsModel.getTableStructure(new TestTable());

        assertThat(model, is(notNullValue()));
        assertThat(model.getName(), is(equalTo("tables.TestTable")));
    }

    @Test
    public void testGetPolicyCmptType() {
        PolicyCmptType model = IpsModel.getPolicyCmptType(TestPolicy.class);

        assertThat(model, is(notNullValue()));
        assertThat(model.getName(), is(equalTo("MyPolicy")));
    }

    @Test
    public void testGetPolicyCmptType_byInstance() {
        PolicyCmptType model = IpsModel.getPolicyCmptType(new TestPolicy());

        assertThat(model, is(notNullValue()));
        assertThat(model.getName(), is(equalTo("MyPolicy")));
    }

    @Test
    public void testGetPolicyCmptType_isReturningCachedInstance() {
        PolicyCmptType model = IpsModel.getPolicyCmptType(new TestPolicy());

        assertThat(IpsModel.getPolicyCmptType(TestPolicy.class), is(sameInstance(model)));
    }

    @Test
    public void testGetPolicyCmptType_isReturningCachedInstanceForInterfaceAndImplementation() {
        assertThat(IpsModel.getPolicyCmptType(MyPolicy.class),
                is(sameInstance(IpsModel.getPolicyCmptType(IMyPolicy.class))));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPolicyCmptType_noAnnotation() {
        IpsModel.getPolicyCmptType(TestPolicyWithoutAnnotation.class);
    }

    @Test
    public void testGetProductCmptType() {
        ProductCmptType model = IpsModel.getProductCmptType(TestProduct.class);

        assertThat(model, is(notNullValue()));
        assertThat(model.getName(), is(equalTo("MyProduct")));
    }

    @Test
    public void testGetProductCmptType_byInstance() {
        ProductCmptType model = IpsModel.getProductCmptType(new TestProduct());

        assertThat(model, is(notNullValue()));
        assertThat(model.getName(), is(equalTo("MyProduct")));
    }

    @Test
    public void testGetProductCmptType_isReturningCachedInstance() {
        ProductCmptType model = IpsModel.getProductCmptType(new TestProduct());

        assertThat(IpsModel.getProductCmptType(TestProduct.class), is(sameInstance(model)));
    }

    @Test
    public void testGetProductCmptType_isReturningCachedInstanceForInterfaceAndImplementation() {
        assertThat(IpsModel.getProductCmptType(MyProduct.class),
                is(sameInstance(IpsModel.getProductCmptType(IMyProduct.class))));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetProductCmptType_noAnnotation() {
        IpsModel.getProductCmptType(TestProductWithoutAnnotation.class);
    }

    @Test
    public void testGetType_policy() {
        Type model = IpsModel.getType(TestPolicy.class);

        assertThat(model, is(instanceOf(PolicyCmptType.class)));
    }

    @Test
    public void testGetType_product() {
        Type model = IpsModel.getType(TestProduct.class);

        assertThat(model, is(instanceOf(ProductCmptType.class)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetType_noType() {
        IpsModel.getType(String.class);
    }

    @Test
    public void testGetEnumType() {
        EnumType model = IpsModel.getEnumType(TestEnum.class);

        assertThat(model, is(notNullValue()));
        assertThat(model.getName(), is(equalTo("my.TestEnum")));
    }

    @Test
    public void testGetEnumType_isReturningCachedInstance() {
        EnumType model = IpsModel.getEnumType(TestEnum.class);

        assertThat(IpsModel.getEnumType(TestEnum.class), is(sameInstance(model)));
    }

    @Test
    public void testGetEnumType_byInstance() {
        EnumType model = IpsModel.getEnumType(new TestEnum());

        assertThat(model, is(notNullValue()));
        assertThat(model.getName(), is(equalTo("my.TestEnum")));
    }

    @Test
    public void testGetEnumType_byEnumWithOverrideMethod() {
        EnumType model = IpsModel.getEnumType(TestJava5Enum.Test1);

        assertThat(model, is(notNullValue()));
        assertThat(model.getName(), is(equalTo("my.TestJava5Enum")));
    }

    @IpsEnumType(name = "my.TestEnum", attributeNames = {})
    private static class TestEnum {
    }

    @IpsEnumType(name = "my.TestJava5Enum", attributeNames = {})
    private enum TestJava5Enum {
        Test1() {
            @Override
            public void test() {
            }
        },
        Test2() {
            @Override
            public void test() {
            }
        };

        public abstract void test();
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

        @Override
        public String getDescription(Locale locale) {
            return null;
        }

        @Override
        public boolean isVariant() {
            return false;
        }

        @Override
        public IProductComponent getVariedBase() {
            return null;
        }

        @Override
        public boolean isValidationRuleActivated(String ruleName) {
            return false;
        }

        @Override
        public void setValidationRuleActivated(String ruleName, boolean active) {
            // nothing to do
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
    public void testGetType_onPublishedInterface() {
        Type model = IpsModel.getType(IMyPolicy.class);

        assertNotNull(model);
        assertEquals("MyPolicy", model.getName());
    }

    @Test
    public void testGetType_onImplementingClass() {
        Type model = IpsModel.getType(MyPolicy.class);

        assertNotNull(model);
        assertEquals("MyPolicy", model.getName());
    }

    @Test
    public void testGetPolicyCmptType_onPublishedInterface() {
        Type model = IpsModel.getPolicyCmptType(IMyPolicy.class);

        assertNotNull(model);
        assertEquals("MyPolicy", model.getName());
    }

    @Test
    public void testGetPolicyCmptType_onImplementingClass() {
        Type model = IpsModel.getPolicyCmptType(MyPolicy.class);

        assertNotNull(model);
        assertEquals("MyPolicy", model.getName());
    }

    @Test
    public void testGetProductCmptType_onPublishedInterface() {
        Type model = IpsModel.getProductCmptType(IMyProduct.class);

        assertNotNull(model);
        assertEquals("MyProduct", model.getName());
    }

    @Test
    public void testGetProductCmptType_onImplementingClass() {
        Type model = IpsModel.getProductCmptType(MyProduct.class);

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
