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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.Severity;
import org.faktorips.runtime.ValidationContext;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.internal.ProductComponentGeneration;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.annotation.IpsAllowedValues;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsAttributeSetter;
import org.faktorips.runtime.model.annotation.IpsAttributes;
import org.faktorips.runtime.model.annotation.IpsChangingOverTime;
import org.faktorips.runtime.model.annotation.IpsDefaultValue;
import org.faktorips.runtime.model.annotation.IpsEnumType;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.faktorips.valueset.StringLengthValueSet;
import org.faktorips.valueset.UnrestrictedValueSet;
import org.faktorips.valueset.ValueSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ProductAttributeTest {

    @Mock
    private IRuntimeRepository repository;

    private static final Calendar effectiveDate = new GregorianCalendar(1999, 1, 1);

    @Test
    public void testIsProductRelevant() {
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Produkt.class);
        for (ProductAttribute modelTypeAttribute : productCmptType.getAttributes()) {
            assertThat(modelTypeAttribute.isProductRelevant(), is(true));
        }
        productCmptType = IpsModel.getProductCmptType(SubProdukt.class);
        for (ProductAttribute modelTypeAttribute : productCmptType.getAttributes()) {
            assertThat(modelTypeAttribute.isProductRelevant(), is(true));
        }
    }

    @Test
    public void testGetValue() {
        Produkt productComponent = new Produkt(repository);
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Produkt.class);
        assertThat(productCmptType.getAttribute("attr1").getValue(productComponent, effectiveDate),
                is(equalTo((Object)"foo")));
        assertThat(productCmptType.getAttribute("attr2").getValue(productComponent, effectiveDate),
                is(equalTo((Object)42)));
        assertThat(productCmptType.getAttribute("attrGen").getValue(productComponent, effectiveDate),
                is(equalTo((Object)"foobar")));
        assertThat(productCmptType.getAttribute("multiString").getValue(productComponent, effectiveDate),
                is(equalTo((Object)Arrays.asList("hello", "world"))));
        productComponent = new SubProdukt(repository);
        productCmptType = IpsModel.getProductCmptType(SubProdukt.class);
        assertThat(productCmptType.getAttribute("attr1").getValue(productComponent, effectiveDate),
                is(equalTo((Object)"foo")));
        assertThat(productCmptType.getAttribute("multiString").getValue(productComponent, effectiveDate),
                is(equalTo((Object)Arrays.asList("hello", "world"))));
        assertThat(productCmptType.getAttribute("attr2").getValue(productComponent, effectiveDate),
                is(equalTo((Object)23)));
        assertThat(productCmptType.getAttribute("attr3").getValue(productComponent, effectiveDate),
                is(equalTo((Object)"foofoo")));
        assertThat(productCmptType.getAttribute("attrGen").getValue(productComponent, effectiveDate),
                is(equalTo((Object)"foobaz")));
        assertThat(
                productCmptType.getAttribute("attrGen").getValue(productComponent, new GregorianCalendar(1999, 1, 2)),
                is(equalTo((Object)"2ndGen")));
    }

    @Test
    public void testGetValue_noEffectiveDate() {
        Produkt productComponent = new Produkt(repository);
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Produkt.class);
        assertThat(productCmptType.getAttribute("attr1").getValue(productComponent, null), is(equalTo((Object)"foo")));
        assertThat(productCmptType.getAttribute("attr2").getValue(productComponent, null), is(equalTo((Object)42)));
        assertThat(productCmptType.getAttribute("attrGen").getValue(productComponent, null),
                is(equalTo((Object)"foobar")));
        assertThat(productCmptType.getAttribute("multiString").getValue(productComponent, null),
                is(equalTo((Object)Arrays.asList("hello", "world"))));

        productComponent = new SubProdukt(repository);
        productCmptType = IpsModel.getProductCmptType(SubProdukt.class);
        assertThat(productCmptType.getAttribute("attr1").getValue(productComponent, null), is(equalTo((Object)"foo")));
        assertThat(productCmptType.getAttribute("multiString").getValue(productComponent, null),
                is(equalTo((Object)Arrays.asList("hello", "world"))));
        assertThat(productCmptType.getAttribute("attr2").getValue(productComponent, null), is(equalTo((Object)23)));
        assertThat(productCmptType.getAttribute("attr3").getValue(productComponent, null),
                is(equalTo((Object)"foofoo")));
        assertThat(productCmptType.getAttribute("attrGen").getValue(productComponent, null),
                is(equalTo((Object)"foobaz")));
    }

    @Test
    public void testSetValue() {
        Produkt productComponent = new Produkt(repository);
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Produkt.class);
        productCmptType.getAttribute("attr1").setValue(productComponent, effectiveDate, "newValue");
        productCmptType.getAttribute("attr2").setValue(productComponent, effectiveDate, 1);
        productCmptType.getAttribute("attrGen").setValue(productComponent, effectiveDate, "newValue");
        productCmptType.getAttribute("multiString").setValue(productComponent, effectiveDate,
                Arrays.asList("new", "value"));

        assertThat(productCmptType.getAttribute("attr1").getValue(productComponent, effectiveDate),
                is(equalTo((Object)"newValue")));
        assertThat(productCmptType.getAttribute("attr2").getValue(productComponent, effectiveDate),
                is(equalTo((Object)1)));
        assertThat(productCmptType.getAttribute("attrGen").getValue(productComponent, effectiveDate),
                is(equalTo((Object)"newValue")));
        assertThat(productCmptType.getAttribute("multiString").getValue(productComponent, effectiveDate),
                is(equalTo((Object)Arrays.asList("new", "value"))));
    }

    @Test
    public void testSetValue_noEffectiveDate() {
        Produkt productComponent = new Produkt(repository);
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Produkt.class);
        productCmptType.getAttribute("attr1").setValue(productComponent, null, "newValue");
        productCmptType.getAttribute("attr2").setValue(productComponent, null, 1);
        productCmptType.getAttribute("attrGen").setValue(productComponent, null, "newValue");
        productCmptType.getAttribute("multiString").setValue(productComponent, null,
                Arrays.asList("new", "value"));

        assertThat(productCmptType.getAttribute("attr1").getValue(productComponent, null),
                is(equalTo((Object)"newValue")));
        assertThat(productCmptType.getAttribute("attr2").getValue(productComponent, null),
                is(equalTo((Object)1)));
        assertThat(productCmptType.getAttribute("attrGen").getValue(productComponent, null),
                is(equalTo((Object)"newValue")));
        assertThat(productCmptType.getAttribute("multiString").getValue(productComponent, null),
                is(equalTo((Object)Arrays.asList("new", "value"))));
    }

    @Test
    public void testIsChangingOverTime() {
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Produkt.class);
        assertThat(productCmptType.getAttribute("attr1").isChangingOverTime(), is(false));
        assertThat(productCmptType.getAttribute("attr2").isChangingOverTime(), is(false));
        assertThat(productCmptType.getAttribute("multiString").isChangingOverTime(), is(false));
        assertThat(productCmptType.getAttribute("multiEnum").isChangingOverTime(), is(false));
        assertThat(productCmptType.getAttribute("attrGen").isChangingOverTime(), is(true));
        productCmptType = IpsModel.getProductCmptType(SubProdukt.class);
        assertThat(productCmptType.getAttribute("attr1").isChangingOverTime(), is(false));
        assertThat(productCmptType.getAttribute("attr2").isChangingOverTime(), is(false));
        assertThat(productCmptType.getAttribute("multiString").isChangingOverTime(), is(false));
        assertThat(productCmptType.getAttribute("multiEnum").isChangingOverTime(), is(false));
        assertThat(productCmptType.getAttribute("attr3").isChangingOverTime(), is(false));
        assertThat(productCmptType.getAttribute("attrGen").isChangingOverTime(), is(true));
    }

    @Test
    public void testIsMultiValue() {
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Produkt.class);
        assertThat(productCmptType.getAttribute("attr1").isMultiValue(), is(false));
        assertThat(productCmptType.getAttribute("attr2").isMultiValue(), is(false));
        assertThat(productCmptType.getAttribute("multiString").isMultiValue(), is(true));
        assertThat(productCmptType.getAttribute("multiEnum").isMultiValue(), is(true));
        assertThat(productCmptType.getAttribute("attrGen").isMultiValue(), is(false));
        productCmptType = IpsModel.getProductCmptType(SubProdukt.class);
        assertThat(productCmptType.getAttribute("attr1").isMultiValue(), is(false));
        assertThat(productCmptType.getAttribute("attr2").isMultiValue(), is(false));
        assertThat(productCmptType.getAttribute("multiString").isMultiValue(), is(true));
        assertThat(productCmptType.getAttribute("attr3").isMultiValue(), is(false));
        assertThat(productCmptType.getAttribute("multiEnum").isMultiValue(), is(true));
        assertThat(productCmptType.getAttribute("attrGen").isMultiValue(), is(false));
    }

    @Test
    public void testGetDatatype() {
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Produkt.class);
        assertThat(productCmptType.getAttribute("attr1").getDatatype(), is(equalTo((Object)String.class)));
        assertThat(productCmptType.getAttribute("attr2").getDatatype(), is(equalTo((Object)Integer.class)));
        assertThat(productCmptType.getAttribute("attrGen").getDatatype(), is(equalTo((Object)String.class)));
        assertThat(productCmptType.getAttribute("multiString").getDatatype(), is(equalTo((Object)String.class)));
        assertThat(productCmptType.getAttribute("multiEnum").getDatatype(),
                is(equalTo((Object)AbstractEnumType.class)));
        productCmptType = IpsModel.getProductCmptType(SubProdukt.class);
        assertThat(productCmptType.getAttribute("attr1").getDatatype(), is(equalTo((Object)String.class)));
        assertThat(productCmptType.getAttribute("attr2").getDatatype(), is(equalTo((Object)Integer.class)));
        assertThat(productCmptType.getAttribute("attrGen").getDatatype(), is(equalTo((Object)String.class)));
        assertThat(productCmptType.getAttribute("multiString").getDatatype(), is(equalTo((Object)String.class)));
        assertThat(productCmptType.getAttribute("attr3").getDatatype(), is(equalTo((Object)String.class)));
        assertThat(productCmptType.getAttribute("multiEnum").getDatatype(),
                is(equalTo((Object)ConcreteEnumType.class)));
    }

    @Test
    public void testIsDeprecated() throws Exception {
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Produkt.class);
        assertThat(productCmptType.getAttribute("attr1").isDeprecated(), is(false));
        assertThat(productCmptType.getAttribute("deprecatedAttribute").isDeprecated(), is(true));
    }

    @Test
    public void testGetDeprecation() throws Exception {
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Produkt.class);
        assertThat(productCmptType.getAttribute("attr1").getDeprecation().isPresent(), is(false));
        Optional<Deprecation> deprecation = productCmptType.getAttribute("deprecatedAttribute").getDeprecation();
        assertThat(deprecation.isPresent(), is(true));
        assertThat(deprecation.get().getSinceVersion().isPresent(), is(false));
        assertThat(deprecation.get().isMarkedForRemoval(), is(false));
    }

    @Test
    public void testGetDefaultValueFromModel() {
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Produkt.class);
        ProductAttribute attribute = productCmptType.getAttribute("attr1");

        assertThat(attribute.getDefaultValueFromModel(), is("bar"));
    }

    @Test
    public void testGetDefaultValueFromModel_NoField() {
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Produkt.class);
        ProductAttribute attribute = productCmptType.getAttribute("attr2");

        assertThat(attribute.getDefaultValueFromModel(), is(nullValue()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetDefaultValueFromModel_ListNoField() {
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Produkt.class);
        ProductAttribute attribute = productCmptType.getAttribute("multiString");

        assertThat(attribute.getDefaultValueFromModel(), is(notNullValue()));
        assertThat(((List<String>)attribute.getDefaultValueFromModel()).size(), is(1));
        assertThat(((List<String>)attribute.getDefaultValueFromModel()), hasItem(IpsStringUtils.EMPTY));
    }

    @Test
    public void testGetValueSetFromModel() {
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Produkt.class);
        ProductAttribute attribute = productCmptType.getAttribute("attr1");

        assertThat(attribute.getValueSetFromModel(), is(new StringLengthValueSet(10)));
    }

    @Test
    public void testGetValueSetFromModelNoField() {
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Produkt.class);
        ProductAttribute attribute = productCmptType.getAttribute("attr2");

        assertThat(attribute.getValueSetFromModel(), is(new UnrestrictedValueSet<>()));
    }

    @Test
    public void testGetDefaultValueFromModel_Gen() {
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Produkt.class);
        ProductAttribute attribute = productCmptType.getAttribute("attrGen");

        assertThat(attribute.getDefaultValueFromModel(), is("foobar"));
    }

    @Test
    public void testGetValueSetFromModel_Gen() {
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Produkt.class);
        ProductAttribute attribute = productCmptType.getAttribute("attrGen");

        assertThat(attribute.getValueSetFromModel(), is(new StringLengthValueSet(10)));
    }

    @Test
    public void testValidate() {
        Produkt productComponent = new Produkt(repository);
        productComponent.setAttr1("123456789");
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Produkt.class);
        ProductAttribute attribute1 = productCmptType.getAttribute("attr1");
        MessageList ml = new MessageList();
        IValidationContext context = new ValidationContext(Locale.ENGLISH);

        attribute1.validate(ml, context, productComponent, effectiveDate);

        assertThat(ml.isEmpty(), is(true));
    }

    @Test
    public void testValidate_LongerThanAllowed() {
        Produkt productComponent = new Produkt(repository);
        productComponent.setAttr1("12345678910");
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Produkt.class);
        ProductAttribute attribute1 = productCmptType.getAttribute("attr1");
        MessageList ml = new MessageList();
        IValidationContext context = new ValidationContext(Locale.ENGLISH);

        attribute1.validate(ml, context, productComponent, effectiveDate);

        assertThat(ml.isEmpty(), is(false));
        Message message = ml.getMessageByCode(ProductAttribute.MSGCODE_VALUE_NOT_IN_VALUE_SET);
        assertThat(message, is(not(nullValue())));
        assertThat(message.getSeverity(), is(Severity.ERROR));
        assertThat(message.getText(), containsString("attr1"));
        assertThat(message.getText(), containsString("12345678910"));
        assertThat(message.getText(), containsString("StringLengthValueSet (10)"));
    }

    @IpsProductCmptType(name = "ProductXYZ")
    @IpsChangingOverTime(ProduktGen.class)
    @IpsAttributes({ "attr1", "attr2", "multiString", "attrGen", "multiEnum", "deprecatedAttribute" })
    private static class Produkt extends ProductComponent {

        @IpsDefaultValue("attr1")
        public static final String DEFAULT_ATTR1 = "bar";

        @IpsAllowedValues("attr1")
        public static final ValueSet<String> ALLOWED_VALUES_ATTR1 = new StringLengthValueSet(10);

        private final ProduktGen produktGen = new ProduktGen(this);
        private String attr1 = "foo";
        private Integer attr2 = 42;
        private List<String> multiString = Arrays.asList("hello", "world");

        public Produkt(IRuntimeRepository repository) {
            super(repository, "id", "kindId", "versionId");
        }

        @IpsAttribute(name = "attr1", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)
        public String getAttr1() {
            return attr1;
        }

        @IpsAttributeSetter("attr1")
        public void setAttr1(String newValue) {
            attr1 = newValue;
        }

        @IpsAttribute(name = "attr2", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)
        public Integer getAttr2() {
            return attr2;
        }

        @IpsAttributeSetter("attr2")
        public void setAttr2(Integer newValue) {
            attr2 = newValue;
        }

        @IpsAttribute(name = "multiString", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)
        public List<String> getMultiString() {
            return multiString;
        }

        @IpsAttributeSetter("multiString")
        public void setMultiString(List<String> newValue) {
            multiString = newValue;
        }

        @IpsAttribute(name = "multiEnum", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)
        public List<? extends AbstractEnumType> getMultiEnum() {
            return Collections.emptyList();
        }

        @IpsAttribute(name = "deprecatedAttribute", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)
        @Deprecated
        public int getDeprecatedAttribute() {
            return -1;
        }

        @Override
        public IConfigurableModelObject createPolicyComponent() {
            return null;
        }

        @Override
        public boolean isChangingOverTime() {
            return true;
        }

        @Override
        public IProductComponentGeneration getGenerationBase(Calendar effectiveDate) {
            return produktGen;
        }

        @Override
        public IProductComponentGeneration getLatestProductComponentGeneration() {
            return produktGen;
        }

    }

    private static class ProduktGen extends ProductComponentGeneration {

        @IpsAllowedValues("attrGen")
        public static final ValueSet<String> ALLOWED_VALUES_ATTRGEN = new StringLengthValueSet(10);

        @IpsDefaultValue("attrGen")
        public static final String DEFAULT_ATTRGEN = "foobar";

        private String attrGen = DEFAULT_ATTRGEN;

        public ProduktGen(ProductComponent productCmpt) {
            super(productCmpt);
        }

        @IpsAttribute(name = "attrGen", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)
        public String getAttrGen() {
            return attrGen;
        }

        @IpsAttributeSetter("attrGen")
        public void setAttrGen(String newValue) {
            attrGen = newValue;
        }

    }

    @IpsProductCmptType(name = "SubProductXYZ")
    @IpsChangingOverTime(SubProduktGen.class)
    @IpsAttributes({ "attr2", "attrGen", "attr3", "multiEnum" })
    private static class SubProdukt extends Produkt {

        private final SubProduktGen subProduktGen = new SubProduktGen(this);
        private final List<ConcreteEnumType> multiEnum = Arrays.asList(new ConcreteEnumType());

        public SubProdukt(IRuntimeRepository repository) {
            super(repository);
        }

        @Override
        @IpsAttribute(name = "attr2", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)
        public Integer getAttr2() {
            return 23;
        }

        @IpsAttribute(name = "attr3", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)
        public String getAttr3() {
            return "foofoo";
        }

        @Override
        @IpsAttribute(name = "multiEnum", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)
        public List<ConcreteEnumType> getMultiEnum() {
            return multiEnum;
        }

        @Override
        public IConfigurableModelObject createPolicyComponent() {
            return null;
        }

        @Override
        public boolean isChangingOverTime() {
            return true;
        }

        @Override
        public IProductComponentGeneration getGenerationBase(Calendar effectiveDate) {
            if (effectiveDate != ProductAttributeTest.effectiveDate) {
                return new SubProduktGen(this) {

                    @Override
                    public String getAttrGen() {
                        return "2ndGen";
                    }

                };
            }
            return subProduktGen;
        }

        @Override
        public IProductComponentGeneration getLatestProductComponentGeneration() {
            return subProduktGen;
        }

    }

    private static class SubProduktGen extends ProduktGen {

        public SubProduktGen(ProductComponent productCmpt) {
            super(productCmpt);
        }

        @Override
        @IpsAttribute(name = "attrGen", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)
        public String getAttrGen() {
            return "foobaz";
        }

    }

    @IpsEnumType(name = "AbstractEnumType", attributeNames = { "" })
    private interface AbstractEnumType {
        // abstract
    }

    @IpsEnumType(name = "ConcreteEnumType", attributeNames = { "" })
    private static class ConcreteEnumType implements AbstractEnumType {
        // concrete
    }

}
