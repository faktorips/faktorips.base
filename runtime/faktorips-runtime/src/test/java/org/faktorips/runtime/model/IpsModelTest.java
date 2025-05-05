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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import org.faktorips.runtime.CardinalityRange;
import org.faktorips.runtime.FormulaExecutionException;
import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IModifiableRuntimeRepository;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IProductComponentLink;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.InMemoryRuntimeRepository;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.runtime.ValidationContext;
import org.faktorips.runtime.internal.DateTime;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.internal.TestTable;
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsEnumType;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.faktorips.runtime.model.annotation.IpsPublishedInterface;
import org.faktorips.runtime.model.enumtype.EnumAttribute;
import org.faktorips.runtime.model.enumtype.EnumAttributeTest.MyEnumForMandatoryTests;
import org.faktorips.runtime.model.enumtype.EnumType;
import org.faktorips.runtime.model.enumtype.EnumTypeTest.Foo;
import org.faktorips.runtime.model.table.TableStructure;
import org.faktorips.runtime.model.type.PolicyCmptType;
import org.faktorips.runtime.model.type.ProductCmptType;
import org.faktorips.runtime.model.type.ProductCmptTypeTest.ConfiguringProduct;
import org.faktorips.runtime.model.type.ProductCmptTypeTest.ConfiguringProductAdj;
import org.faktorips.runtime.model.type.ProductCmptTypeTest.TargetProduct;
import org.faktorips.runtime.model.type.Type;
import org.faktorips.values.InternationalString;
import org.faktorips.valueset.StringLengthValueSet;
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
        // an enum
    }

    @IpsEnumType(name = "my.TestJava5Enum", attributeNames = {})
    private enum TestJava5Enum {
        Test1() {
            @Override
            public void test() {
                // nothing to do
            }
        },
        Test2() {
            @Override
            public void test() {
                // nothing to do
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

        /** to be overwritten in subclass */
        @SuppressWarnings("unused")
        public int computeAFormula() throws FormulaExecutionException {
            return -1;
        }

        @Override
        public void setDescription(Locale locale, String newDescription) {
            // nothing to do
        }

        @Override
        public void setDescription(InternationalString newDescription) {
            // nothing to do
        }

    }

    private abstract static class TestFormulaSubclass extends TestProduct {

        @Override
        public int computeAFormula() throws FormulaExecutionException {
            return 42;
        }
    }

    private abstract static class TestProductWithoutAnnotation implements IProductComponent {
        // an unannotated product
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

    @Test
    public void testGetProductCmptType_onFormulaSubclass() {
        Type model = IpsModel.getProductCmptType(TestFormulaSubclass.class);

        assertNotNull(model);
        assertEquals("MyProduct", model.getName());
        assertSame(IpsModel.getProductCmptType(TestProduct.class), model);
    }

    @Test
    public void testGetProductCmptType_onFormulaSubclassWithInterface() {
        Type model = IpsModel.getProductCmptType(MyProductFormulaSubclass.class);

        assertNotNull(model);
        assertEquals("MyProduct", model.getName());
        assertSame(IpsModel.getProductCmptType(MyProduct.class), model);
    }

    @Test
    public void testValidate() {
        IModifiableRuntimeRepository repository = new InMemoryRuntimeRepository();
        IpsModel.getProductCmptType(ConfiguringProduct.class);
        TargetProduct targetProduct = new TargetProduct(repository, "T 1", "T", "1");
        repository.putProductComponent(targetProduct);
        ConfiguringProduct productComponent = new ConfiguringProduct(repository, "P 1", "P", "1");
        productComponent.setAllowedValuesForStaticPolicyAttribute(new StringLengthValueSet(42));
        productComponent.setStaticProductAttribute("foo");
        productComponent.addStaticProductAssociation(targetProduct, new CardinalityRange(100, 200, 150));
        repository.putProductComponent(productComponent);
        ConfiguringProductAdj configuringProductAdj1 = new ConfiguringProductAdj(productComponent);
        configuringProductAdj1.setValidFrom(new DateTime(2024, 1, 1));
        configuringProductAdj1.setProductAttribute(-1);
        configuringProductAdj1.setDefaultValuePolicyAttribute(99);
        repository.putProductCmptGeneration(configuringProductAdj1);
        Foo e1a = new Foo(1, "Eins", true);
        Foo e1b = new Foo(1, "Eins", true);
        Foo e2 = new Foo(2, "Eins", true);
        repository.putEnumValues(Foo.class, List.of(e1a, e1b, e2));
        repository.putEnumValues(MyEnumForMandatoryTests.class,
                List.of(MyEnumForMandatoryTests.allSet, MyEnumForMandatoryTests.allNulls,
                        MyEnumForMandatoryTests.allEmpty));

        MessageList messages = IpsModel.validate(repository, new ValidationContext());

        assertThat(messages.containsErrorMsg(), is(true));
        assertThat(messages.size(), is(16));
        assertThat(messages.toString(), containsString("200"));
        assertThat(messages.toString(), containsString("1"));
        assertThat(messages.toString(), containsString("foo"));
        assertThat(messages.toString(), containsString("42"));
        assertThat(messages.toString(), containsString("-1"));
        assertThat(messages.toString(), containsString("99"));
        assertThat(messages.stream()
                .filter(m -> m.getCode().equals(EnumAttribute.MSGCODE_MANDATORY_ATTRIBUTE_IS_EMPTY))
                .map(Message::getInvalidObjectProperties)
                .flatMap(List::stream).toList(),
                contains(
                        new ObjectProperty(MyEnumForMandatoryTests.allNulls, "mandatoryString"),
                        new ObjectProperty(MyEnumForMandatoryTests.allNulls, "mandatoryMultilingualString"),
                        new ObjectProperty(MyEnumForMandatoryTests.allNulls, "mandatoryInteger"),
                        new ObjectProperty(MyEnumForMandatoryTests.allNulls, "mandatoryDecimal"),
                        new ObjectProperty(MyEnumForMandatoryTests.allEmpty, "mandatoryString"),
                        new ObjectProperty(MyEnumForMandatoryTests.allEmpty, "mandatoryMultilingualString"),
                        new ObjectProperty(MyEnumForMandatoryTests.allEmpty, "mandatoryInteger"),
                        new ObjectProperty(MyEnumForMandatoryTests.allEmpty, "mandatoryDecimal")));
    }

    @IpsPolicyCmptType(name = "MyPolicy")
    @IpsPublishedInterface(implementation = MyPolicy.class)
    @IpsDocumented(bundleName = "org.faktorips.runtime.model", defaultLocale = "de")
    private interface IMyPolicy extends IModelObject {
        // a policy
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
    private interface IMyProduct extends IProductComponent {

        int computeAFormula() throws FormulaExecutionException;
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

        @Override
        public int computeAFormula() throws FormulaExecutionException {
            return -1;
        }

    }

    private static class MyProductFormulaSubclass extends MyProduct {

        public MyProductFormulaSubclass(IRuntimeRepository repository, String id, String productKindId,
                String versionId) {
            super(repository, id, productKindId, versionId);
        }

        @Override
        public int computeAFormula() throws FormulaExecutionException {
            return 2;
        }

    }

}
