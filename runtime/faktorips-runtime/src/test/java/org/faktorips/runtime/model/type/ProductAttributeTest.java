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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IProductComponentLink;
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
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsEnumType;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.faktorips.runtime.model.annotation.IpsPublishedInterface;
import org.faktorips.values.Decimal;
import org.faktorips.valueset.IntegerRange;
import org.faktorips.valueset.OrderedValueSet;
import org.faktorips.valueset.StringLengthValueSet;
import org.faktorips.valueset.UnrestrictedValueSet;
import org.faktorips.valueset.ValueSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.w3c.dom.Element;

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
        assertThat(productCmptType.getAttribute("attr2").getDatatype().isPrimitive(), is(false));
        assertThat(productCmptType.getAttribute("attrGen").getDatatype(), is(equalTo((Object)String.class)));
        assertThat(productCmptType.getAttribute("multiString").getDatatype(), is(equalTo((Object)String.class)));
        assertThat(productCmptType.getAttribute("multiEnum").getDatatype(),
                is(equalTo((Object)AbstractEnumType.class)));
        assertThat(productCmptType.getAttribute("primitiveIntAttr").getDatatype(), is(equalTo((Object)int.class)));
        assertThat(int.class.isAssignableFrom(productCmptType.getAttribute("primitiveIntAttr").getDatatype()),
                is(true));
        assertThat(productCmptType.getAttribute("primitiveIntAttr").getDatatype().isPrimitive(), is(true));
        assertThat(productCmptType.getAttribute("multiPrimitiveIntAttr").getDatatype(), is(equalTo((Object)int.class)));
        assertThat(int.class.isAssignableFrom(productCmptType.getAttribute("multiPrimitiveIntAttr").getDatatype()),
                is(true));
        assertThat(productCmptType.getAttribute("multiPrimitiveIntAttr").getDatatype().isPrimitive(), is(true));
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
        assertThat(message.getText(), containsString("String length ≤ 10"));
        assertThat(message.getInvalidObjectProperties().size(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(attribute1));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(), is(ProductAttribute.PROPERTY_VALUE));
    }

    @Test
    public void testValidate_DuplicateValue() {
        Produkt productComponent = new Produkt(repository);
        productComponent.setMultiPrimitiveIntAttr(Arrays.asList(1, 2, 1));
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Produkt.class);
        ProductAttribute attribute1 = productCmptType.getAttribute("multiPrimitiveIntAttr");
        MessageList ml = new MessageList();
        IValidationContext context = new ValidationContext(Locale.ENGLISH);

        attribute1.validate(ml, context, productComponent, effectiveDate);

        assertThat(ml.isEmpty(), is(false));
        Message message = ml.getMessageByCode(ProductAttribute.MSGCODE_DUPLICATE_VALUE);
        assertThat(message, is(not(nullValue())));
        assertThat(message.getSeverity(), is(Severity.ERROR));
        assertThat(message.getText(), containsString("1, 2, 1"));
        assertThat(message.getInvalidObjectProperties().size(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(attribute1));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(), is(ProductAttribute.PROPERTY_VALUE));
    }

    @Test
    public void testValidate_GenerationWithInterface_publishedWithoutAdj() {
        IProductWithUnAndPublishedAttributes product = new ProductWithUnAndPublishedAttributes(repository);
        ProductCmptType productCmptType = IpsModel.getProductCmptType(IProductWithUnAndPublishedAttributes.class);
        IValidationContext context = new ValidationContext(Locale.ENGLISH);
        MessageList ml = new MessageList();
        ProductAttribute attribute1 = productCmptType.getAttribute("publishedAttribute");

        attribute1.validate(ml, context, product, effectiveDate);

        assertThat(ml.isEmpty(), is(true));
    }

    @Test
    public void testValidate_GenerationWithInterface_publishedWithoutAdj_validationError() {
        IProductWithUnAndPublishedAttributes product = new ProductWithUnAndPublishedAttributes(repository);
        ((ProductWithUnAndPublishedAttributes)product).setPublishedAttribute(Decimal.valueOf(100));
        ProductCmptType productCmptType = IpsModel.getProductCmptType(IProductWithUnAndPublishedAttributes.class);
        IValidationContext context = new ValidationContext(Locale.ENGLISH);
        MessageList ml = new MessageList();
        ProductAttribute attribute1 = productCmptType.getAttribute("publishedAttribute");

        attribute1.validate(ml, context, product, effectiveDate);

        assertThat(ml.containsErrorMsg(), is(true));
        Message message = ml.getMessageByCode(ProductAttribute.MSGCODE_VALUE_NOT_IN_VALUE_SET);
        assertThat(message, is(notNullValue()));
        assertThat(message.getSeverity(), is(Severity.ERROR));
        assertThat(message.getText(), containsString("publishedAttribute"));
        assertThat(message.getText(), containsString("100"));
        assertThat(message.getText(), containsString("[1.0, 1.5, 2]"));
        assertThat(message.getInvalidObjectProperties().size(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(attribute1));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(), is(ProductAttribute.PROPERTY_VALUE));
    }

    @Test
    public void testValidate_GenerationWithInterface_publishedWithAdj() {
        IProductWithUnAndPublishedAttributes product = new ProductWithUnAndPublishedAttributes(repository);
        ProductCmptType productCmptType = IpsModel.getProductCmptType(IProductWithUnAndPublishedAttributes.class);
        IValidationContext context = new ValidationContext(Locale.ENGLISH);
        MessageList ml = new MessageList();
        ProductAttribute attribute1 = productCmptType.getAttribute("publishedAttributeAdj");

        attribute1.validate(ml, context, product, effectiveDate);

        assertThat(ml.isEmpty(), is(true));
    }

    @Test
    public void testValidate_GenerationWithInterface_publishedWithAdj_validationError() {
        IProductWithUnAndPublishedAttributes product = new ProductWithUnAndPublishedAttributes(repository);
        IProductComponentGeneration productGen = product.getLatestProductComponentGeneration();
        ((ProductWithUnAndPublishedAttributesAdj)productGen).setPublishedAttributeAdj(100);
        ProductCmptType productCmptType = IpsModel.getProductCmptType(IProductWithUnAndPublishedAttributes.class);
        IValidationContext context = new ValidationContext(Locale.ENGLISH);
        MessageList ml = new MessageList();
        ProductAttribute attribute1 = productCmptType.getAttribute("publishedAttributeAdj");

        attribute1.validate(ml, context, product, effectiveDate);

        assertThat(ml.containsErrorMsg(), is(true));
        Message message = ml.getMessageByCode(ProductAttribute.MSGCODE_VALUE_NOT_IN_VALUE_SET);
        assertThat(message, is(notNullValue()));
        assertThat(message.getSeverity(), is(Severity.ERROR));
        assertThat(message.getText(), containsString("publishedAttributeAdj"));
        assertThat(message.getText(), containsString("100"));
        assertThat(message.getText(), containsString("0-10, 2"));
        assertThat(message.getInvalidObjectProperties().size(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(attribute1));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(), is(ProductAttribute.PROPERTY_VALUE));
    }

    @Test
    public void testValidate_GenerationWithInterface_unPublishedWithoutAdj() {
        IProductWithUnAndPublishedAttributes product = new ProductWithUnAndPublishedAttributes(repository);
        ProductCmptType productCmptType = IpsModel.getProductCmptType(IProductWithUnAndPublishedAttributes.class);
        IValidationContext context = new ValidationContext(Locale.ENGLISH);
        MessageList ml = new MessageList();
        ProductAttribute attribute1 = productCmptType.getAttribute("unpublishedAttribute");

        attribute1.validate(ml, context, product, effectiveDate);

        assertThat(ml.isEmpty(), is(true));
    }

    @Test
    public void testValidate_GenerationWithInterface_unPublishedWithoutAdj_validationError() {
        IProductWithUnAndPublishedAttributes product = new ProductWithUnAndPublishedAttributes(repository);
        ((ProductWithUnAndPublishedAttributes)product).setUnpublishedAttribute(Decimal.valueOf(100));
        ProductCmptType productCmptType = IpsModel.getProductCmptType(IProductWithUnAndPublishedAttributes.class);
        IValidationContext context = new ValidationContext(Locale.ENGLISH);
        MessageList ml = new MessageList();
        ProductAttribute attribute1 = productCmptType.getAttribute("unpublishedAttribute");

        attribute1.validate(ml, context, product, effectiveDate);

        assertThat(ml.containsErrorMsg(), is(true));
        Message message = ml.getMessageByCode(ProductAttribute.MSGCODE_VALUE_NOT_IN_VALUE_SET);
        assertThat(message, is(notNullValue()));
        assertThat(message.getSeverity(), is(Severity.ERROR));
        assertThat(message.getText(), containsString("unpublishedAttribute"));
        assertThat(message.getText(), containsString("100"));
        assertThat(message.getText(), containsString("[2, 2.5, 3]"));
        assertThat(message.getInvalidObjectProperties().size(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(attribute1));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(), is(ProductAttribute.PROPERTY_VALUE));
    }

    @Test
    public void testValidate_GenerationWithInterface_unPublishedWithAdj() {
        IProductWithUnAndPublishedAttributes product = new ProductWithUnAndPublishedAttributes(repository);
        ProductCmptType productCmptType = IpsModel.getProductCmptType(IProductWithUnAndPublishedAttributes.class);
        IValidationContext context = new ValidationContext(Locale.ENGLISH);
        MessageList ml = new MessageList();
        ProductAttribute attribute1 = productCmptType.getAttribute("unpublishedAttributeAdj");

        attribute1.validate(ml, context, product, effectiveDate);

        assertThat(ml.isEmpty(), is(true));
    }

    @Test
    public void testValidate_GenerationWithInterface_unPublishedWithAdj_validationError() {
        IProductWithUnAndPublishedAttributes product = new ProductWithUnAndPublishedAttributes(repository);
        IProductComponentGeneration productGen = product.getLatestProductComponentGeneration();
        ((ProductWithUnAndPublishedAttributesAdj)productGen).setUnpublishedAttributeAdj(100);
        ProductCmptType productCmptType = IpsModel.getProductCmptType(IProductWithUnAndPublishedAttributes.class);
        IValidationContext context = new ValidationContext(Locale.ENGLISH);
        MessageList ml = new MessageList();
        ProductAttribute attribute1 = productCmptType.getAttribute("unpublishedAttributeAdj");

        attribute1.validate(ml, context, product, effectiveDate);

        assertThat(ml.containsErrorMsg(), is(true));
        Message message = ml.getMessageByCode(ProductAttribute.MSGCODE_VALUE_NOT_IN_VALUE_SET);
        assertThat(message, is(notNullValue()));
        assertThat(message.getSeverity(), is(Severity.ERROR));
        assertThat(message.getText(), containsString("publishedAttributeAdj"));
        assertThat(message.getText(), containsString("100"));
        assertThat(message.getText(), containsString("10-20, 2"));
        assertThat(message.getInvalidObjectProperties().size(), is(1));
        assertThat(message.getInvalidObjectProperties().get(0).getObject(), is(attribute1));
        assertThat(message.getInvalidObjectProperties().get(0).getProperty(), is(ProductAttribute.PROPERTY_VALUE));
    }

    @IpsProductCmptType(name = "ProductXYZ")
    @IpsChangingOverTime(ProduktGen.class)
    @IpsAttributes({ "attr1", "attr2", "multiString", "attrGen", "multiEnum", "deprecatedAttribute",
            "primitiveIntAttr", "multiPrimitiveIntAttr" })
    private static class Produkt extends ProductComponent {

        @IpsDefaultValue("attr1")
        public static final String DEFAULT_ATTR1 = "bar";

        @IpsAllowedValues("attr1")
        public static final ValueSet<String> ALLOWED_VALUES_ATTR1 = new StringLengthValueSet(10);

        private final ProduktGen produktGen = new ProduktGen(this);
        private String attr1 = "foo";
        private Integer attr2 = 42;
        private List<String> multiString = Arrays.asList("hello", "world");
        private int primitiveIntAttr = 23;
        private List<Integer> multiPrimitiveIntAttr = Arrays.asList(1, 2, 3);

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

        @IpsAttribute(name = "primitiveIntAttr", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues, primitive = true)
        public int getPrimitiveIntAttr() {
            return primitiveIntAttr;
        }

        @IpsAttributeSetter("primitiveIntAttr")
        public void setPrimitiveIntAttr(int newValue) {
            primitiveIntAttr = newValue;
        }

        @IpsAttribute(name = "multiPrimitiveIntAttr", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues, primitive = true)
        public List<Integer> getMultiPrimitiveIntAttr() {
            return multiPrimitiveIntAttr;
        }

        @IpsAttributeSetter("multiPrimitiveIntAttr")
        public void setMultiPrimitiveIntAttr(List<Integer> newValue) {
            multiPrimitiveIntAttr = newValue;
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

    @IpsPublishedInterface(implementation = ProductWithUnAndPublishedAttributes.class)
    @IpsProductCmptType(name = "ProductWithUnAndPublishedAttributes")
    @IpsAttributes({ "publishedAttributeAdj", "unpublishedAttributeAdj", "publishedAttribute", "unpublishedAttribute" })
    @IpsChangingOverTime(IProductWithUnAndPublishedAttributesAdj.class)
    @IpsDocumented(bundleName = "test.model-label-and-descriptions", defaultLocale = "en")
    private interface IProductWithUnAndPublishedAttributes extends IProductComponent {

        String PROPERTY_PUBLISHEDATTRIBUTE = "publishedAttribute";

        @IpsAllowedValues("publishedAttribute")
        OrderedValueSet<Decimal> MAX_ALLOWED_VALUES_FOR_PUBLISHED_ATTRIBUTE = new OrderedValueSet<>(
                false, Decimal.NULL, Decimal.valueOf("1.0"), Decimal.valueOf("1.5"), Decimal.valueOf("2"));

        @IpsDefaultValue("publishedAttribute")
        Decimal DEFAULT_VALUE_FOR_PUBLISHED_ATTRIBUTE = Decimal.valueOf("1");

        @IpsAttribute(name = "publishedAttribute", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.Enum)
        Decimal getPublishedAttribute();

        Decimal getUnpublishedAttribute();

        IProductWithUnAndPublishedAttributesAdj getProductWithUnAndPublishedAttributesAdj(
                Calendar effectiveDate);
    }

    @IpsPublishedInterface(implementation = ProductWithUnAndPublishedAttributesAdj.class)
    private interface IProductWithUnAndPublishedAttributesAdj extends IProductComponentGeneration {

        String PROPERTY_PUBLISHEDATTRIBUTEADJ = "publishedAttributeAdj";

        @IpsAllowedValues("publishedAttributeAdj")
        IntegerRange MAX_ALLOWED_RANGE_FOR_PUBLISHED_ATTRIBUTE_ADJ = IntegerRange
                .valueOf(Integer.valueOf("0"), Integer.valueOf(10), Integer.valueOf(2), false);

        @IpsDefaultValue("publishedAttributeAdj")
        Integer DEFAULT_VALUE_FOR_PUBLISHED_ATTRIBUTE_ADJ = Integer.valueOf(2);

        @IpsAttribute(name = "publishedAttributeAdj", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.Range)
        Integer getPublishedAttributeAdj();

        Integer getUnpublishedAttributeAdj();

        IProductWithUnAndPublishedAttributes getProductWithUnAndPublishedAttributes();
    }

    private static class ProductWithUnAndPublishedAttributes extends ProductComponent
            implements IProductWithUnAndPublishedAttributes {
        public static final String PROPERTY_UNPUBLISHEDATTRIBUTE = "unpublishedAttribute";

        @IpsAllowedValues("unpublishedAttribute")
        public static final OrderedValueSet<Decimal> MAX_ALLOWED_VALUES_FOR_UNPUBLISHED_ATTRIBUTE = new OrderedValueSet<>(
                false, Decimal.NULL, Decimal.valueOf("2"), Decimal.valueOf("2.5"), Decimal.valueOf("3"));

        @IpsDefaultValue("unpublishedAttribute")
        public static final Decimal DEFAULT_VALUE_FOR_UNPUBLISHED_ATTRIBUTE = Decimal.valueOf("2");

        private Decimal publishedAttribute = DEFAULT_VALUE_FOR_PUBLISHED_ATTRIBUTE;

        private Decimal unpublishedAttribute = DEFAULT_VALUE_FOR_UNPUBLISHED_ATTRIBUTE;

        private IProductWithUnAndPublishedAttributesAdj adjustment = new ProductWithUnAndPublishedAttributesAdj(this);

        public ProductWithUnAndPublishedAttributes(IRuntimeRepository repository) {
            super(repository, "id", "kindId", "versionId");
        }

        @Override
        public IProductWithUnAndPublishedAttributesAdj getProductWithUnAndPublishedAttributesAdj(
                Calendar effectiveDate) {
            return (IProductWithUnAndPublishedAttributesAdj)getRepository().getProductComponentGeneration(getId(),
                    effectiveDate);
        }

        @Override
        public boolean isChangingOverTime() {
            return true;
        }

        @Override
        public Decimal getPublishedAttribute() {
            return publishedAttribute;
        }

        public void setPublishedAttribute(Decimal newValue) {
            publishedAttribute = newValue;
        }

        @IpsAttribute(name = "unpublishedAttribute", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.Enum)
        @Override
        public Decimal getUnpublishedAttribute() {
            return unpublishedAttribute;
        }

        @IpsAttributeSetter("unpublishedAttribute")
        public void setUnpublishedAttribute(Decimal newValue) {
            unpublishedAttribute = newValue;
        }

        @Override
        protected void doInitPropertiesFromXml(Map<String, Element> configMap) {
            // empty
        }

        @Override
        public IConfigurableModelObject createPolicyComponent() {
            throw new UnsupportedOperationException(
                    "This product component type does not configure a policy component type.");
        }

        @Override
        public IProductComponentGeneration getGenerationBase(Calendar effectiveDate) {
            return adjustment;
        }

        @Override
        public IProductComponentGeneration getLatestProductComponentGeneration() {
            return adjustment;
        }
    }

    private static class ProductWithUnAndPublishedAttributesAdj extends ProductComponentGeneration
            implements IProductWithUnAndPublishedAttributesAdj {

        public static final String PROPERTY_UNPUBLISHEDATTRIBUTEADJ = "unpublishedAttributeAdj";

        @IpsAllowedValues("unpublishedAttributeAdj")
        public static final IntegerRange MAX_ALLOWED_RANGE_FOR_UNPUBLISHED_ATTRIBUTE_ADJ = IntegerRange
                .valueOf(Integer.valueOf(10), Integer.valueOf(20), Integer.valueOf(2), false);

        @IpsDefaultValue("unpublishedAttributeAdj")
        public static final Integer DEFAULT_VALUE_FOR_UNPUBLISHED_ATTRIBUTE_ADJ = Integer.valueOf(12);

        private Integer publishedAttributeAdj = DEFAULT_VALUE_FOR_PUBLISHED_ATTRIBUTE_ADJ;

        private Integer unpublishedAttributeAdj = DEFAULT_VALUE_FOR_UNPUBLISHED_ATTRIBUTE_ADJ;

        public ProductWithUnAndPublishedAttributesAdj(ProductWithUnAndPublishedAttributes productCmpt) {
            super(productCmpt);
        }

        @Override
        public Integer getPublishedAttributeAdj() {
            return publishedAttributeAdj;
        }

        public void setPublishedAttributeAdj(Integer newValue) {
            publishedAttributeAdj = newValue;
        }

        @IpsAttribute(name = "unpublishedAttributeAdj", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.Range)
        @Override
        public Integer getUnpublishedAttributeAdj() {
            return unpublishedAttributeAdj;
        }

        @IpsAttributeSetter("unpublishedAttributeAdj")
        public void setUnpublishedAttributeAdj(Integer newValue) {
            unpublishedAttributeAdj = newValue;
        }

        @Override
        public IProductWithUnAndPublishedAttributes getProductWithUnAndPublishedAttributes() {
            return (IProductWithUnAndPublishedAttributes)getProductComponent();
        }

        @Override
        protected void doInitPropertiesFromXml(Map<String, Element> configMap) {
            // empty
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
            return new ArrayList<>();
        }
    }
}
